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

.
├── src/
│ ├── main/
│ │ ├── java/
│ │ │ └── bgu/spl/mics/ # MicroServices framework
│ │ │ └── bgu/spl/components/ # Camera, LiDAR, FusionSLAM, etc.
│ │ │ └── bgu/spl/messages/ # Events and Broadcasts
│ │ │ └── bgu/spl/util/ # Utility classes (Pose, Landmark, etc.)
│ └── test/
│ └── java/
│ └── bgu/spl/tests/ # JUnit test classes
├── pom.xml # Maven configuration

markdown
Copy
Edit

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

Unit tests were written using **JUnit** for:

- `MessageBusImpl`
- Camera and LiDAR services
- SLAM fusion calculations

To run tests:

```bash
mvn test
🧱 Build and Run Instructions
✅ Prerequisites
Java 8+

Maven

📦 Build
bash
Copy
Edit
mvn clean install
▶️ Run
bash
Copy
Edit
java -jar target/assignment2.jar path/to/config.json
🔍 Coordinate Transformation
LiDAR data is initially in the robot’s local frame and must be converted to global coordinates using the robot's pose (x, y, yaw). The transformation includes:

Rotation by yaw

Translation by the robot’s position

java
Copy
Edit
double x_global = cos(yaw) * x_local - sin(yaw) * y_local + robot_x;
double y_global = sin(yaw) * x_local + cos(yaw) * y_local + robot_y;
For more details, see Appendix B in the provided documents.

📚 Libraries Used
GSON - JSON serialization/deserialization

JUnit 5 - Testing framework

🧑‍💻 Authors
[Your Name]

[Partner's Name (if any)]

Course: SPL225 - Systems Programming Lab
Institution: Ben-Gurion University
Year: 2025
