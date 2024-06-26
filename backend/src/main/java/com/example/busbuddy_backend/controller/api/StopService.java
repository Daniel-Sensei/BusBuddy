package com.example.busbuddy_backend.controller.api;

import com.example.busbuddy_backend.persistence.model.Route;
import com.example.busbuddy_backend.persistence.model.Schedule;
import com.example.busbuddy_backend.persistence.model.Stop;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.example.busbuddy_backend.controller.api.Time.getCurrentDate;
import static com.example.busbuddy_backend.controller.api.Utility.checkDelaysIntegrity;
import static java.awt.geom.Point2D.distance;

import static com.example.busbuddy_backend.controller.api.Utility.getDocumentById;


@RestController
@CrossOrigin("*")
public class StopService {
    private final String STOPS_COLLECTION = "stops";
    private final String ROUTES_COLLECTION = "routes";

    /**
     * Get a stop by its ID.
     *
     * @param id The ID of the stop.
     * @return The stop object wrapped in a ResponseEntity.
     * @throws InterruptedException if the retrieval of the document is interrupted.
     * @throws ExecutionException if there is an error retrieving the document.
     */
    @GetMapping("/stop")
    public ResponseEntity<Stop> getStop(@RequestParam String id) {
        // Get the Firestore instance and the "stops" collection reference
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference stops = db.collection(STOPS_COLLECTION);

        try {
            // Get the document by its ID
            DocumentSnapshot document = getDocumentById(stops, id);
            if (document.exists()) {
                // Convert the document to a Stop object
                Stop stop = document.toObject(Stop.class);

                // Return the stop with a success response
                return new ResponseEntity<>(stop, HttpStatus.OK);
            } else {
                // If the stop is not found, return a not found response
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (InterruptedException | ExecutionException e) {
            // If there is an error, throw a runtime exception
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves all stops within a given radius from a specific latitude and longitude.
     * The stops are ordered by distance from the specified coordinates.
     *
     * @param latitude The latitude of the center point.
     * @param longitude The longitude of the center point.
     * @param radius The radius within which to search for stops (in meters).
     * @return A list of stops within the specified radius, ordered by distance from the specified coordinates.
     * @throws InterruptedException if the retrieval of the documents is interrupted.
     * @throws ExecutionException if there is an error retrieving the documents.
     */
    @GetMapping("/stopsWithinRadius")
    public ResponseEntity<List<Stop>> getStopsWithinRadius(
            @RequestParam double latitude, @RequestParam double longitude, @RequestParam double radius) {
        // Get the Firestore instance and the "stops" collection reference
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference stops = db.collection(STOPS_COLLECTION);

        try {
            // Get all stops and filter out those outside the specified radius
            List<Stop> allStops = stops.get().get().toObjects(Stop.class);
            List<Stop> inRadiusStops = new ArrayList<>();
            for (Stop stop : allStops) {
                if (isWithinRadius(latitude, longitude, radius, stop)) {
                    inRadiusStops.add(stop);
                }
            }

            // Order stops by distance from the specified coordinates
            inRadiusStops.sort((s1, s2) -> {
                double d1 = distance(latitude, longitude, s1.getCoords().getLatitude(), s1.getCoords().getLongitude());
                double d2 = distance(latitude, longitude, s2.getCoords().getLatitude(), s2.getCoords().getLongitude());
                return Double.compare(d1, d2);
            });

            // Return the list of stops with a success response
            return new ResponseEntity<>(inRadiusStops, HttpStatus.OK);
        } catch (InterruptedException | ExecutionException e) {
            // If there is an error, throw a runtime exception
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if a given stop is within a specified radius of a given set of coordinates.
     *
     * @param latitude The latitude of the center point.
     * @param longitude The longitude of the center point.
     * @param radius The radius within which to search for stops (in meters).
     * @param stop The stop to check.
     * @return True if the stop is within the specified radius, false otherwise.
     */
    private boolean isWithinRadius(double latitude, double longitude, double radius, Stop stop) {
        // Convert coordinates to radians
        double lat1 = Math.toRadians(latitude);  // latitude of the center point
        double lon1 = Math.toRadians(longitude); // longitude of the center point
        double lat2 = Math.toRadians(stop.getCoords().getLatitude());  // latitude of the stop
        double lon2 = Math.toRadians(stop.getCoords().getLongitude());  // longitude of the stop

        // Calculate the difference between the coordinates
        double dLat = lat2 - lat1;  // difference in latitude
        double dLon = lon2 - lon1;  // difference in longitude

        // Haversine formula to calculate the distance
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = 6371000 * c; // Mean radius of Earth in meters

        // Check if the distance is within the specified radius
        return distance <= radius;
    }

    /**
     * Get the next buses for a given stop.
     *
     * @param stopId The ID of the stop.
     * @return A response entity containing a map of route codes to lists of next bus times, or a not found response if the stop does not exist.
     */
    @GetMapping("/nextBuses")
    public ResponseEntity<Map<String, List<String>>> getNextBusesByStop(@RequestParam String stopId) {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference stops = db.collection(STOPS_COLLECTION);

        try {
            // Save the reference of the document as DocumentReference
            DocumentSnapshot document = getDocumentById(stops, stopId);
            if (document.exists()) {
                DocumentReference stopRef = document.getReference();
                List<DocumentReference> routesRefs = (List<DocumentReference>) document.get("routes");
                System.out.println(routesRefs);
                System.out.println("Number of routes: " + routesRefs.size());


                // Create an empty map
                Map<String, List<String>> nextBuses = new HashMap<>();
                List<DocumentReference> stopsReferenceForward = null;
                List<DocumentReference> stopsReferenceBack = null;
                for( DocumentReference routeRef : routesRefs) {
                    DocumentSnapshot routeDocument = routeRef.get().get();
                    Route route = routeDocument.toObject(Route.class);
                    System.out.println(route.getCode());
                    // FORWARD
                    stopsReferenceForward = (List<DocumentReference>) routeDocument.get("stops.forward");
                    Integer indexForward = null;
                    if(stopsReferenceForward.contains(stopRef)) {
                        indexForward = stopsReferenceForward.indexOf(stopRef);
                    }
                    // BACK
                    stopsReferenceBack = (List<DocumentReference>) routeDocument.get("stops.back");
                    Integer indexBack = null;
                    if(stopsReferenceBack.contains(stopRef)) {
                        indexBack = stopsReferenceBack.indexOf(stopRef);
                    }


                    // Check in the route.history map with key "currentDate" in the format "dd-MM-yyyy"
                    // Then move to "sunday" if the day of the week is Sunday, otherwise move to "week"
                    // If the map is not empty, then get the list of forward times with key indexForward
                    // Also get the list of back times with key indexBack
                    Map<String, Schedule> history = route.getHistory();
                    if (history != null) {
                        // Get the Date object for the current date
                        String today = getCurrentDate();
                        Schedule todayData = history.get(today);

                        Schedule schedule = route.getDelays();
                        if(!checkDelaysIntegrity(schedule)) {
                            System.out.println("Delays are NOT ok");
                            schedule = route.getTimetable();
                        }

                        if (todayData != null) {
                            // Get the list of times for the stop
                            List<String> forwardDay = todayData.getForward().get(String.valueOf(indexForward));
                            List<String> backDay = todayData.getBack().get(String.valueOf(indexBack));

                            int startIndexForward = 0;
                            for(String time : forwardDay) {
                                if(time == null || !time.equals("-")) {
                                    startIndexForward += 1;
                                }
                            }

                            int startIndexBack = 0;
                            if(backDay != null) {
                                for (String time : backDay) {
                                    if (time == null || !time.equals("-")) {
                                        startIndexBack += 1;
                                    }
                                }
                            }

                            //schedule = route.getTimetable();

                            List<String> forward =schedule.getForward().get(String.valueOf(indexForward));
                            List<String> back =schedule.getBack().get(String.valueOf(indexBack));

                            if(backDay != null) {
                                String destination = route.getCode().split("_")[1];
                                // Reverse the destination by splitting it with "-"
                                // Example: "A-B" becomes "B-A"
                                // The destination may have multiple "-"
                                // So split the array and reverse everything
                                String[] destinationArray = destination.split(" - ");
                                String invertedDestination = "";
                                for (int i = destinationArray.length - 1; i >= 0; i--) {
                                    invertedDestination += destinationArray[i] + " - ";
                                }
                                invertedDestination = invertedDestination.substring(0, invertedDestination.length() - 3);

                                nextBuses.put(route.getCode().split("_")[0] + "_" + destination, forward.subList(startIndexForward, forward.size()));
                                nextBuses.put(route.getCode().split("_")[0] + "_" + invertedDestination, back.subList(startIndexBack, back.size()));
                            } else {
                                nextBuses.put(route.getCode(), forward.subList(startIndexForward, forward.size()));
                            }

                        }
                        else{
                            // If the current day in the history is empty, then get all the forward and back times
                            // from the timetable without considering the indices

                            //schedule = route.getTimetable();

                            List<String> forward =schedule.getForward().get(String.valueOf(indexForward));
                            List<String> back =schedule.getBack().get(String.valueOf(indexBack));

                            // Fill the nextBuses map with forward and back times
                            // Reverse the destination if the back list is not empty

                            if(back != null) {
                                String destination = route.getCode().split("_")[1];
                                String[] destinationArray = destination.split(" - ");
                                String invertedDestination = "";
                                for (int i = destinationArray.length - 1; i >= 0; i--) {
                                    invertedDestination += destinationArray[i] + " - ";
                                }
                                invertedDestination = invertedDestination.substring(0, invertedDestination.length() - 3);

                                nextBuses.put(route.getCode().split("_")[0] + "_" + destination, forward);
                                nextBuses.put(route.getCode().split("_")[0] + "_" + invertedDestination, back);
                            } else {
                                nextBuses.put(route.getCode(), forward);
                            }

                        }
                    }


                }

                return new ResponseEntity<>(nextBuses, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
