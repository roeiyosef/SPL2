# SPL225 - Assignment 2: GurionRock Pro Max Ultra Over 9000

> **Concurrent Perception & Mapping Simulation Framework in Java**

## 🚀 Overview

This project simulates the perception and mapping system of a smart robotic vacuum cleaner, modeled after real-world SLAM (Simultaneous Localization and Mapping) systems. The system is built in Java using **concurrent microservices** communicating via an event-driven **MessageBus** framework. Sensors like **Cameras**, **LiDAR**, **GPS**, and **IMU** operate in parallel and contribute data for environment mapping.

This project was implemented as part of the SPL225 course at Ben-Gurion University.

## 🧠 Core Concepts

- **Java Concurrency**: Threads, synchronization, lambdas, callbacks.
- **MicroServices Architecture**: Event-based message passing with support for broadcast and future resolution.
- **Sensor Fusion & SLAM**: Combines data from multiple sensors to build a world map.
- **Event-Driven Simulation**: Time-driven system using broadcasts, event queues, and worker threads.

## 🛠️ Features

- ✅ Custom `Future<T>` implementation.
- ✅ Thread-safe `MessageBus` with round-robin event distribution.
- ✅ Camera and LiDAR workers simulate real-time detection and tracking.
- ✅ `FusionSLAM` component integrates sensor data into a global map.
- ✅ Error handling for faulty sensors and crash propagation.
- ✅ JSON-based configuration and input/output.
- ✅ Maven project structure with JUnit test coverage.

## 📁 Project Structure

```text
.
├── src/
│   ├── main/
│   │   └── java/
│   │       └── bgu/spl/mics/         # MicroServices framework
│   │       └── bgu/spl/components/   # Camera, LiDAR, FusionSLAM, etc.
│   │       └── bgu/spl/messages/     # Events and Broadcasts
│   │       └── bgu/spl/util/         # Utility classes (Pose, Landmark, etc.)
│
│   └── test/
│       └── java/
│           └── bgu/spl/tests/        # JUnit test classes
│
├── pom.xml                           # Maven configuration
```

 

## 📦 Input Files

The program accepts a **configuration JSON file** as its main input, which includes:

- ✅ Camera configuration and detection data path
- ✅ LiDAR configuration and cloud point path
- ✅ Pose data path
- ✅ Tick time and simulation duration

Additional JSON files describe:

- 🖼️ Camera-detected objects
- 📡 LiDAR-detected cloud points
- 📍 Robot poses at every tick

> See the `input/` folder for example files (not included in the repo due to assignment policies).

## 📤 Output

A single `output_file.json` is generated, including:

- 📊 Runtime statistics (e.g., # of objects detected/tracked)
- 🗺️ Final world map with global coordinates
- ⚠️ Error report (if a sensor fails)

## 🧪 Testing

Unit tests are implemented using **JUnit 5** for:

- `MessageBusImpl` (thread-safe event handling)
- Camera/LiDAR services (data prep)
- FusionSLAM (coordinate transformation logic)

To run tests:

```bash
mvn test
```

## 🧱 Build and Run Instructions

✅ Prerequisites
Java 8+
Maven

## 📦 Build
mvn clean install

## ▶️ Run
java -jar target/assignment2.jar path/to/config.json
Make sure to run on a UNIX machine.

🔍 Coordinate Transformation

## Sensor data from LiDAR is in the robot’s local frame and must be converted to the global frame: 🧮 Formula
```text
double yawRad = Math.toRadians(yaw);
double x_global = Math.cos(yawRad) * x_local - Math.sin(yawRad) * y_local + robot_x;
double y_global = Math.sin(yawRad) * x_local + Math.cos(yawRad) * y_local + robot_y;
Used to rotate and translate cloud points based on the robot's pose at detection time.
```
## 📚 Libraries Used
GSON – for parsing JSON

JUnit 5 – for testing

## 👨‍💻 Authors
[Roei Yosef]

## 
Course: SPL225 – Systems Programming Lab
Institution: Ben-Gurion University of the Negev
Year: 2025
