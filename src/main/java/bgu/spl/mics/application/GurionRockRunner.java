package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {
    private static final Object lock = new Object();
    public static AtomicBoolean isTerminatedEarly = new AtomicBoolean(false);

    public static int NumOfCameras=0;

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");
        // TODO: Parse configuration file.
        if (args.length == 0) {
            System.out.println("Error: Please provide the path to the configuration file as an argument.");
            return;
        }
        String cameraDataPath ="";
        String lidarDataPath="";
        String poseDataPath = "";


        String configFilePath = args[0];
        try (Reader reader = new FileReader(configFilePath)) {
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(reader, JsonObject.class);


            // Parsing Global Parameters
            int tickTime = config.get("TickTime").getAsInt();
            int duration = config.get("Duration").getAsInt();

            // Parsing Cameras

            JsonObject camerasObj = config.getAsJsonObject("Cameras");
            LinkedList<Camera> cameras = parseCameras(camerasObj);
            // Check if "CamerasConfigurations" exists and is a JsonArray

            // Parsing LiDAR Workers
            JsonObject lidarsObj;
            if(config.getAsJsonObject("LiDarWorkers")!=null) {
                lidarsObj= config.getAsJsonObject("LiDarWorkers");
            }
            else{
                 lidarsObj = config.getAsJsonObject("LidarWorkers");
            }
            System.out.println(configFilePath);
            LinkedList<LiDarWorkerTracker> lidarWorkers = parseLidarWorkers(lidarsObj);

            // Check if "CamerasConfigurations" exists and is a JsonArray

            // Parsing GPS-IMU
            GPSIMU gpsIMU = new GPSIMU(0); // Starting at tick 0

                    // Initialize FusionSLAM
            FusionSlam fusionSlam = new FusionSlam();


            // Extract the base path from the configuration file
            String basePath = configFilePath.substring(0, configFilePath.lastIndexOf(File.separator) + 1);

// Retrieve relative paths from the JSON configuration
            poseDataPath = config.get("poseJsonFile").getAsString();
            cameraDataPath = camerasObj.get("camera_datas_path").getAsString();
            lidarDataPath = lidarsObj.get("lidars_data_path").getAsString();

// Build absolute paths using Paths.get()
            poseDataPath = Paths.get(basePath, poseDataPath).normalize().toString();
            cameraDataPath = Paths.get(basePath, cameraDataPath).normalize().toString();
            lidarDataPath = Paths.get(basePath, lidarDataPath).normalize().toString();


                    // Print parsed configuration for validation
                    System.out.println("Tick Time: " + tickTime);
                    System.out.println("Duration: " + duration);
                    System.out.println("Loaded " + cameras.size() + " cameras.");
                    System.out.println("Loaded " + lidarWorkers.size() + " LiDAR workers.");
                    System.out.println("GPS-IMU initialized.");
                    System.out.println("FusionSLAM initialized.");
                    System.out.println("Camera Data Path: " + cameraDataPath);
                    System.out.println("LiDAR Data Path: " + lidarDataPath);
                    System.out.println("Pose Data Path: " + poseDataPath);
               //     System.out.println("Output Data Path: " + outputDataPath);
            System.out.println(cameraDataPath + " this is the camera data path ");
            Map<String, LinkedList<StampedDetectedObjects>> cameraDataMap = parseCameraData(cameraDataPath);

            /*// Print each camera's data
            for (Map.Entry<String, LinkedList<StampedDetectedObjects>> entry : cameraDataMap.entrySet()) {
                System.out.println("Camera: " + entry.getKey());
                for (StampedDetectedObjects stamped : entry.getValue()) {
                    System.out.println(stamped.toString());
                }
            }*/
            for (Camera camera : cameras) {
                String cameraKey = "camera" + camera.getId(); // Assuming camera_key follows "camera1", "camera2"
                if (cameraDataMap.containsKey(cameraKey)) {
                    camera.setListOfStampedDetectedObjects(cameraDataMap.get(cameraKey));
                    System.out.println("Updated Camera " + cameraKey + " with detected objects.");
                } else {
                    System.out.println("Warning: No detected objects found for " + cameraKey);
                }
            }
            System.out.println("TEST THAT ALL WENT WELL UPDATING THE CAMERAS DETECTEDOBJECTS FIELD");
            System.out.println("BUG - Camers size is : " + cameras.size());
            for (Camera camera : cameras) {
                System.out.println("BUG - camera getDetectedList size is : " +camera.getDetectedObjectsList().size());
                for (StampedDetectedObjects stamped : camera.getDetectedObjectsList()) {
                    System.out.println(stamped.toString());
                }
            }

            //LinkedList<StampedCloudPoints> lidarData = parseLidarData(lidarDataPath);
            LiDarDataBase instance = LiDarDataBase.getInstance(lidarDataPath);
           /* LinkedList<StampedCloudPoints> lidarData = instance.getCloudPoints();
            System.out.println("\n--- LiDAR Data ---");
            for (StampedCloudPoints entry : lidarData) {
                System.out.println("ID: " + entry.getId() + ", Time: " + entry.getTime());
                for (LinkedList<Double> point : entry.getCloudPoints()) {
                    System.out.println("  Point: (" + point.get(0) + ", " + point.get(1) + ")");
                }
            }*/



            /*System.out.println("WOAH");
            for (StampedCloudPoints scp : lidarData) {
                System.out.println("ID: " + scp.getId() + ", Time: " + scp.getTime());
                for (LinkedList<Double> point : scp.getCloudPoints()) {
                    System.out.println("Point: " + point);
                }
            }*/



            LinkedList<Pose> poseData = parsePoseData(poseDataPath);
            System.out.println("\n--- Pose Data ---");
            for (Pose pose : poseData) {
                System.out.println("Time: " + pose.getTime() + ", X: " + pose.getX() + ", Y: " + pose.getY() + ", Yaw: " + pose.getYaw());
            }


            // TODO: Initialize system components and services.
// Initialize Time Service
            TimeService timeService = new TimeService(tickTime, duration);


// Initialize Pose Service
            PoseService poseService = new PoseService(new GPSIMU(0));
            poseService.getGpsimu().setPoseList(parsePoseData(poseDataPath));

// Initialize Camera Services
            LinkedList<CameraService> cameraServices = new LinkedList<>();
            for (Camera camera : cameras) {
                cameraServices.add(new CameraService(camera));
               /* for (Map.Entry<String, LinkedList<StampedDetectedObjects>> entry : cameraDataMap.entrySet()) {
                    camera.setListOfStampedDetectedObjects(entry.getValue());
                }*/
            }
            System.out.println("SECOND IMPROTATNTT TEST");
            for (CameraService camertas : cameraServices) {
                for (StampedDetectedObjects stamped : camertas.camera.getDetectedObjectsList()) {
                    System.out.println(stamped.toString());
                }
            }
            NumOfCameras = cameraServices.size();
            System.out.println("NUM OF CAMERS FIELD is: " + NumOfCameras);


// Initialize LiDAR Services
            LinkedList<LiDarService> lidarServices = new LinkedList<>();
            for (LiDarWorkerTracker lidar : lidarWorkers) {
                LiDarService lidarService = new LiDarService(lidar);
                lidarServices.add(lidarService);
            }

            // Initialize Fusion Slam Service
            FusionSlamService fusionSlamService = new FusionSlamService(new FusionSlam(),cameraServices.size(),lidarServices.size());

            // TODO: Start the simulation.
          //  startSimulation(configFilePath);
            /*AtomicInteger readyServices = new AtomicInteger(0);
            int TOTAL_SERVICES = 1 + 1 + 1 + lidarServices.size() + cameraServices.size();
            System.out.println("TOTAL SERVICES NUM IS : " + TOTAL_SERVICES);

            Thread fusionSlamThread = new Thread(() -> {
                synchronized (lock) {
                    System.out.println("FusionSlam Service is ready.");
                    readyServices.getAndIncrement();
                    lock.notifyAll();
               }
                fusionSlamService.run();

            });
            Thread poseThread = new Thread(() -> {
                 synchronized (lock) {
                System.out.println("PoseThread Service is ready.");
                readyServices.getAndIncrement();
                   lock.notifyAll();
                 }
                poseService.run();
            });

            LinkedList<Thread> cameraThreads = new LinkedList<>();
            for (CameraService cameraService : cameraServices) {
                Thread camThread = new Thread(() -> {
                    synchronized (lock) {
                    readyServices.getAndIncrement();
                       lock.notifyAll();
                     }
                    cameraService.run();
                });
                cameraThreads.add(camThread);
            }
            System.out.println("camThread Services are ready.");

            LinkedList<Thread> lidarThreads = new LinkedList<>();
            for (LiDarService lidarService : lidarServices) {
                Thread lidarThread = new Thread(() -> {
                     synchronized (lock) {
                    readyServices.getAndIncrement();
                       lock.notifyAll();
                     }
                    lidarService.run();
                });
                lidarThreads.add(lidarThread);            }
            System.out.println("lidarThread Services are ready.");

            Thread timeThread = new Thread(() -> {
                synchronized (lock) {
                    try {
                        while (readyServices.get()  != TOTAL_SERVICES - 1) {
                            System.out.println("TimeService is waiting for services to be ready...");
                            System.out.println("the ready services num is " + readyServices);
                            lock.wait();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("TimeService is ready!!!" + "the num of ready Sercices is : 1 + " + readyServices);
                timeService.run();
            });

            // Start All Threads
            timeThread.start();
            fusionSlamThread.start();
            System.out.println('r');
            poseThread.start();
            System.out.println('t');

            for (Thread cam : cameraThreads) cam.start();
            for (Thread lidar : lidarThreads) lidar.start();
            //timeThread.start();




            try {
                timeThread.join();
                fusionSlamThread.join();
                poseThread.join();
                for (Thread cam : cameraThreads) cam.join();
                for (Thread lidar : lidarThreads) lidar.join();
            } catch (InterruptedException e) {
                System.out.println("Simulation interrupted: " + e.getMessage());
                Thread.currentThread().interrupt(); // Restore interrupt flag

            }*/
            // Define a latch for synchronization
            int TOTAL_SERVICES = 1 +  1 + 1 + lidarServices.size() + cameraServices.size();
            CountDownLatch latch = new CountDownLatch(TOTAL_SERVICES -1);

// FusionSlam Service Thread
            Thread fusionSlamThread = new Thread(() -> {
                System.out.println("FusionSlam Service is ready.");
                latch.countDown(); // Signal readiness
                fusionSlamService.run();
            });

// Pose Service Thread
            Thread poseThread = new Thread(() -> {
                System.out.println("PoseThread Service is ready.");
                latch.countDown(); // Signal readiness
                poseService.run();
            });

// Camera Services Threads
            LinkedList<Thread> cameraThreads = new LinkedList<>();
            for (CameraService cameraService : cameraServices) {
                Thread camThread = new Thread(() -> {
                    System.out.println("Camera Service is ready.");
                    latch.countDown(); // Signal readiness
                    cameraService.run();
                });
                cameraThreads.add(camThread);
            }

// LiDAR Services Threads
            LinkedList<Thread> lidarThreads = new LinkedList<>();
            for (LiDarService lidarService : lidarServices) {
                Thread lidarThread = new Thread(() -> {
                    System.out.println("LiDAR Service is ready.");
                    latch.countDown(); // Signal readiness
                    lidarService.run();
                });
                lidarThreads.add(lidarThread);
            }

// Time Service Thread
            Thread timeThread = new Thread(() -> {
                try {
                    System.out.println("TimeService is waiting for all services to be ready...");
                    latch.await(); // Wait until all services signal readiness
                    System.out.println("TimeService is ready. Starting time ticks...");
                    timeService.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("TimeService was interrupted!");
                }
            });

// Start All Threads
            fusionSlamThread.start();
            poseThread.start();
            for (Thread cam : cameraThreads) cam.start();
            for (Thread lidar : lidarThreads) lidar.start();
            timeThread.start();

// Wait for all threads to complete
            try {
                fusionSlamThread.join();
                poseThread.join();
                for (Thread cam : cameraThreads) cam.join();
                for (Thread lidar : lidarThreads) lidar.join();
                timeThread.join();
            } catch (InterruptedException e) {
                System.out.println("Simulation interrupted: " + e.getMessage());
                Thread.currentThread().interrupt(); // Restore interrupt flag
            }



            System.out.println("Landmarks in FusionSlam:");
            System.out.println(fusionSlamService.getFusionSlam().getLandmarks());

            /*int totalDetectedObjects=0;
            for (CameraService cs :cameraServices){
                totalDetectedObjects = totalDetectedObjects + cs.getNumOfDetectedObjects();
            }

            int totalTrackedObjects=0;
            for (LiDarService ls :lidarServices){
                totalTrackedObjects = totalTrackedObjects + ls.getNumOfTrackedObjects();
            }*/

          /*  try (FileWriter writer = new FileWriter("output.json")) {
                Gson gson2 = new GsonBuilder().setPrettyPrinting().create();

                // Build the output structure
                Map<String, Object> output = new HashMap<>();
                output.put("systemRuntime",StatisticalFolder.getInstance().getSystemRuntime()); // Adjust runtime dynamically if needed
                output.put("numDetectedObjects", StatisticalFolder.getInstance().getNumDetectedObjects());
                output.put("numTrackedObjects", StatisticalFolder.getInstance().getNumTrackedObjects());
                output.put("numLandmarks", StatisticalFolder.getInstance().getNumLandmarks());

                // Convert List<Landmark> into a Map<String, Object>
                Map<String, Map<String, Object>> landmarksMap = new HashMap<>();
                for (LandMark landmark : fusionSlamService.getFusionSlam().getLandmarks()) {
                    Map<String, Object> landmarkDetails = new HashMap<>();
                    landmarkDetails.put("id", landmark.getId());
                    landmarkDetails.put("description", landmark.getDescription());
                    landmarkDetails.put("coordinates", landmark.getCoordinates());

                    landmarksMap.put(landmark.getId(), landmarkDetails);
                }

                output.put("landMarks", landmarksMap);

                gson2.toJson(output, writer);
                System.out.println("Simulation completed. Results saved to output.json");
            } catch (IOException e) {
                System.out.println("Failed to write results: " + e.getMessage());
            }*/






        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    /*private static void startSimulation(String configFilePath) {
        int cameraServiceCount = 0;
        int lidarServiceCount = 0;

        // Parse Configuration
        try (Reader reader = new FileReader(configFilePath)) {
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(reader, JsonObject.class);

            // Parse number of cameras
            JsonObject camerasObj = config.getAsJsonObject("Cameras");
            if (camerasObj.has("CamerasConfigurations") && camerasObj.get("CamerasConfigurations").isJsonArray()) {
                cameraServiceCount = camerasObj.getAsJsonArray("CamerasConfigurations").size();
            }

            // Parse number of LiDAR workers
            JsonObject lidarsObj = config.getAsJsonObject("LidarWorkers");
            if (lidarsObj.has("LidarConfigurations") && lidarsObj.get("LidarConfigurations").isJsonArray()) {
                lidarServiceCount = lidarsObj.getAsJsonArray("LidarConfigurations").size();
            }

        } catch (IOException e) {
            System.out.println("Error reading configuration file: " + e.getMessage());
            return;
        }

        // Calculate total services
        int totalServices = 1 + 1 + cameraServiceCount + lidarServiceCount; // FusionSLAM + PoseService + Cameras + LiDARs
        CountDownLatch readyLatch = new CountDownLatch(totalServices);

    }*/


    private static LinkedList<Camera> parseCameras(JsonObject camerasObj) {
        LinkedList<Camera> cameras = new LinkedList<>();
        if (camerasObj.has("CamerasConfigurations") && camerasObj.get("CamerasConfigurations").isJsonArray()) {
            JsonArray cameraArray = camerasObj.getAsJsonArray("CamerasConfigurations");
            for (JsonElement cam : cameraArray) {
                JsonObject camObj = cam.getAsJsonObject();
                int id = camObj.get("id").getAsInt();
                int frequency = camObj.get("frequency").getAsInt();
                cameras.add(new Camera(id, frequency));
            }
        }
        return cameras;
    }
    private static LinkedList<LiDarWorkerTracker> parseLidarWorkers(JsonObject lidarsObj) {
        LinkedList<LiDarWorkerTracker> lidarWorkers = new LinkedList<>();
        if (lidarsObj.has("LidarConfigurations") && lidarsObj.get("LidarConfigurations").isJsonArray()) {
            JsonArray lidarArray = lidarsObj.getAsJsonArray("LidarConfigurations");
            for (JsonElement lid : lidarArray) {
                JsonObject lidObj = lid.getAsJsonObject();
                int id = lidObj.get("id").getAsInt();
                int frequency = lidObj.get("frequency").getAsInt();
                lidarWorkers.add(new LiDarWorkerTracker(id, frequency));
            }
        }
        return lidarWorkers;
    }
  /*  private static LinkedList<StampedDetectedObjects> parseCameraData(String cameraDataPath) {
        LinkedList<StampedDetectedObjects> cameraData = new LinkedList<>();
        try (Reader reader = new FileReader(cameraDataPath)) {
            Gson gson = new Gson();
            JsonArray cameraArray = gson.fromJson(reader, JsonArray.class);

            for (JsonElement entry : cameraArray) {
                JsonObject cameraObj = entry.getAsJsonObject();
                int time = cameraObj.get("time").getAsInt();
                LinkedList<DetectedObject> detectedObjects = new LinkedList<>();

                JsonArray objectsArray = cameraObj.getAsJsonArray("detectedObjects");
                for (JsonElement obj : objectsArray) {
                    JsonObject detectedObj = obj.getAsJsonObject();
                    String id = detectedObj.get("id").getAsString();
                    String description = detectedObj.get("description").getAsString();
                    detectedObjects.add(new DetectedObject(id, description));
                }

                cameraData.add(new StampedDetectedObjects(time, detectedObjects));
            }

            System.out.println("Loaded Camera Data: " + cameraData.size() + " entries.");

        } catch (IOException e) {
            System.out.println("Error reading camera data: " + e.getMessage());
        }
        return cameraData;
    }*/
  /*public static Map<String, LinkedList<StampedDetectedObjects>> parseCameraData(String cameraDataPath) {
      Map<String, LinkedList<StampedDetectedObjects>> cameraDataMap = new HashMap<>();

      try (Reader reader = new FileReader(cameraDataPath)) {
          Gson gson = new Gson();
          JsonObject cameraData = gson.fromJson(reader, JsonObject.class);

          // Iterate over each camera entry (e.g., camera1, camera2)
          for (Map.Entry<String, JsonElement> entry : cameraData.entrySet()) {
              String cameraKey = entry.getKey(); // camera1, camera2, etc.
              JsonArray cameraObservations = entry.getValue().getAsJsonArray();

              LinkedList<StampedDetectedObjects> detectedObjectsList = new LinkedList<>();

              for (JsonElement observation : cameraObservations) {
                  JsonObject obsObj = observation.getAsJsonObject();

                  int time = obsObj.get("time").getAsInt();
                  JsonArray detectedObjectsArray = obsObj.getAsJsonArray("detectedObjects");

                  LinkedList<DetectedObject> detectedObjects = new LinkedList<>();
                  for (JsonElement obj : detectedObjectsArray) {
                      JsonObject objData = obj.getAsJsonObject();
                      String id = objData.get("id").getAsString();
                      String description = objData.get("description").getAsString();
                      detectedObjects.add(new DetectedObject(id, description));
                  }

                  // Add each StampedDetectedObjects instance to the camera's list
                  detectedObjectsList.add(new StampedDetectedObjects(time, detectedObjects));
              }

              // Add the cameraKey and its detected objects to the map
              cameraDataMap.put(cameraKey, detectedObjectsList);
          }

      } catch (IOException e) {
          System.out.println("Error reading camera data file: " + e.getMessage());
      } catch (JsonSyntaxException e) {
          System.out.println("Error parsing camera data: " + e.getMessage());
      }

      return cameraDataMap;
  }*/
   /* public static Map<String, LinkedList<StampedDetectedObjects>> parseCameraData(String cameraDataPath) {
        Map<String, LinkedList<StampedDetectedObjects>> cameraDataMap = new HashMap<>();

        try (Reader reader = new FileReader(cameraDataPath)) {
            Gson gson = new Gson();
            JsonObject cameraData = gson.fromJson(reader, JsonObject.class);

            for (Map.Entry<String, JsonElement> entry : cameraData.entrySet()) {
                String cameraKey = entry.getKey();
                LinkedList<StampedDetectedObjects> detectedObjectsList = new LinkedList<>();

                JsonElement cameraObservations = entry.getValue();
                JsonArray observationsArray;

                // Handle nested array issue
                if (cameraObservations.isJsonArray()) {
                    // First level array (possible nested array)
                    JsonArray outerArray = cameraObservations.getAsJsonArray();
                    for (JsonElement innerElement : outerArray) {
                        if (innerElement.isJsonArray()) {
                            // Nested array case
                            observationsArray = innerElement.getAsJsonArray();
                        } else {
                            // Standard array case
                            observationsArray = outerArray;
                        }

                        // Process each observation in the array
                        for (JsonElement observation : observationsArray) {
                            JsonObject obsObj = observation.getAsJsonObject();
                            int time = obsObj.get("time").getAsInt();
                            JsonArray detectedObjectsArray = obsObj.getAsJsonArray("detectedObjects");

                            LinkedList<DetectedObject> detectedObjects = new LinkedList<>();
                            for (JsonElement obj : detectedObjectsArray) {
                                JsonObject objData = obj.getAsJsonObject();
                                String id = objData.get("id").getAsString();
                                String description = objData.get("description").getAsString();
                                detectedObjects.add(new DetectedObject(id, description));
                            }

                            detectedObjectsList.add(new StampedDetectedObjects(time, detectedObjects));
                        }
                    }
                }

                cameraDataMap.put(cameraKey, detectedObjectsList);
            }

        } catch (IOException e) {
            System.out.println("Error reading camera data file: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.out.println("Error parsing camera data: " + e.getMessage());
        }

        return cameraDataMap;
    }*/
  public static Map<String, LinkedList<StampedDetectedObjects>> parseCameraData(String cameraDataPath) {
      Map<String, LinkedList<StampedDetectedObjects>> cameraDataMap = new HashMap<>();

      try (Reader reader = new FileReader(cameraDataPath)) {
          Gson gson = new Gson();
          JsonObject cameraData = gson.fromJson(reader, JsonObject.class);

          // Iterate over each camera key (e.g., "camera1")
          for (Map.Entry<String, JsonElement> entry : cameraData.entrySet()) {
              String cameraKey = entry.getKey();
              LinkedList<StampedDetectedObjects> detectedObjectsList = new LinkedList<>();

              JsonArray observationsArray = entry.getValue().getAsJsonArray();

              // Process each observation
              for (JsonElement observation : observationsArray) {
                  JsonObject obsObj = observation.getAsJsonObject();

                  int time = obsObj.get("time").getAsInt();
                  JsonArray detectedObjectsArray = obsObj.getAsJsonArray("detectedObjects");

                  // Parse detected objects into a fresh list
                  LinkedList<DetectedObject> detectedObjects = new LinkedList<>();
                  for (JsonElement obj : detectedObjectsArray) {
                      JsonObject objData = obj.getAsJsonObject();
                      String id = objData.get("id").getAsString();
                      String description = objData.get("description").getAsString();
                      detectedObjects.add(new DetectedObject(id, description));
                  }

                  // Add a new StampedDetectedObjects instance to the list
                  detectedObjectsList.add(new StampedDetectedObjects(time, detectedObjects));
              }

              // Add to the main map
              cameraDataMap.put(cameraKey, detectedObjectsList);
          }

      } catch (IOException e) {
          System.out.println("Error reading camera data file: " + e.getMessage());
      } catch (JsonSyntaxException e) {
          System.out.println("Error parsing camera data: " + e.getMessage());
      }

      return cameraDataMap;
  }

    private static LinkedList<StampedCloudPoints> parseLidarData(String lidarDataPath) {
        LinkedList<StampedCloudPoints> lidarData = new LinkedList<>();
        try (Reader reader = new FileReader(lidarDataPath)) {
            Gson gson = new Gson();
            JsonArray lidarArray = gson.fromJson(reader, JsonArray.class);

            for (JsonElement entry : lidarArray) {
                JsonObject lidarObj = entry.getAsJsonObject();
                String id = lidarObj.get("id").getAsString();
                int time = lidarObj.get("time").getAsInt();
                LinkedList<LinkedList<Double>> cloudPoints = new LinkedList<>();

                JsonArray pointsArray = lidarObj.getAsJsonArray("cloudPoints");
                for (JsonElement point : pointsArray) {
                    JsonArray pointArray = point.getAsJsonArray();
                    double x = pointArray.get(0).getAsDouble();
                    double y = pointArray.get(1).getAsDouble();
                    LinkedList<Double> pointPair = new LinkedList<>();
                    pointPair.add(x);
                    pointPair.add(y);
                    cloudPoints.add(pointPair);
                }

                lidarData.add(new StampedCloudPoints(id, time, cloudPoints));
            }

            System.out.println("Loaded LiDAR Data: " + lidarData.size() + " entries.");

        } catch (IOException e) {
            System.out.println("Error reading LiDAR data: " + e.getMessage());
        }
        return lidarData;
    }
    private static LinkedList<Pose> parsePoseData(String poseDataPath) {
        LinkedList<Pose> poseData = new LinkedList<>();
        try (Reader reader = new FileReader(poseDataPath)) {
            Gson gson = new Gson();
            JsonArray poseArray = gson.fromJson(reader, JsonArray.class);

            for (JsonElement entry : poseArray) {
                JsonObject poseObj = entry.getAsJsonObject();
                int time = poseObj.get("time").getAsInt();
                float x = poseObj.get("x").getAsFloat();
                float y = poseObj.get("y").getAsFloat();
                float yaw = poseObj.get("yaw").getAsFloat();

                poseData.add(new Pose(x,y,yaw,time));
            }

            System.out.println("Loaded Pose Data: " + poseData.size() + " entries.");

        } catch (IOException e) {
            System.out.println("Error reading Pose data: " + e.getMessage());
        }
        return poseData;
    }


}
