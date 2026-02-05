# BusBuddy: Your Real-Time Bus Companion

## Introduction
BusBuddy is a revolutionary real-time bus tracking system developed as an undergraduate thesis project in Computer Science at the University of Calabria. This application enhances the public transportation experience with a variety of features aimed at providing users with up-to-date bus information.

The project consists of two main components:
1. **Client app** (this repository)
2. **Tracker app** (private repository)

## Screenshots
### White Theme
<img src="https://github.com/Daniel-Sensei/BusBuddy/assets/132211678/59ecf100-cd45-4af5-b8f5-b442a5063e92" width="250">
<img src="https://github.com/Daniel-Sensei/BusBuddy/assets/132211678/4ec8bbad-879d-44eb-8c79-93307e165e6c" width="250">
<img src="https://github.com/Daniel-Sensei/BusBuddy/assets/132211678/bad86770-622d-4150-b942-b0101bb3d8cb" width="250">

### Dark Theme
<img src="https://github.com/Daniel-Sensei/BusBuddy/assets/132211678/ec5f66ba-2796-4e22-a213-8ec29bad4910" width="250">
<img src="https://github.com/Daniel-Sensei/BusBuddy/assets/132211678/1b0433d2-dc14-4983-88e9-697d29b4fbb8" width="250">
<img src="https://github.com/Daniel-Sensei/BusBuddy/assets/132211678/9625baee-5e9b-4208-9acf-022babe5043a" width="250">


## Features
- **Real-time bus tracking**: Track your bus's current location and estimated arrival time.
- **Stop details**: Check upcoming buses and their schedules for a specific bus stop.
- **Map search**: Search for bus stops on the map to change monitoring zones.
- **Favorites**: Save favorite buses and stops for quick access.
- **Complete bus line list**: Access a comprehensive list of registered bus lines along with their schedules.
- **Bus delays info**: Stay informed about bus delays and receive predictions for upcoming arrivals.

## Technical Details
- **Client App**: Built using Ionic Framework with Angular, enabling multi-platform development for both iOS and Android.
- **Backend Server**: Developed with SpringBoot.
- **Database**: Utilizes Firebase as a non-relational database, suitable for managing large and diverse datasets.

## Installation
To install and run the application, follow these steps:

### Prerequisites
- Node.js (which includes npm)
- Angular CLI
- Ionic CLI
- IntelliJ IDEA (or any IDE of your choice for Java development)

### Steps
1. **Clone the repository**:
```shell
git clone https://github.com/Daniel-Sensei/BusBuddy.git
cd BusBuddy/frontend
```

2. **Install dependencies**:
```
npm install
```
3. **Set up backend**:
- Open the backend project in IntelliJ IDEA.
- Download dependencies specified in `pom.xml` using Maven.

4. **Run the application**:
- **Client app**:
  ```
  ionic serve
  ```
  This command serves the app in the browser for development purposes.
- **Backend server**: 
  - Build and run the SpringBoot application from IntelliJ IDEA.

## Author
- [Curcio Daniel](https://github.com/Daniel-Sensei)

---

Feel free to use and modify this application to suit your transportation needs. For any issues or contributions, please visit the repository and submit your queries or pull requests.
