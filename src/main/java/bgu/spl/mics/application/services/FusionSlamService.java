package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 *
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    private FusionSlam fusionSlam;

    private Pose currentPose; // Store the latest pose from PoseEvent

    int currentTick = 0;

    private final int  totalCameraSensors;
    private final int totalLiDarSensors;


    private Set<String> terminatedSensors = new HashSet<>();

    private  LinkedList<TrackedObjectsEvent> pendingEvents = new LinkedList<>();

    public FusionSlamService(FusionSlam fusionSlam,int totalCameraSensors,int totalLiDarSensors) {
        super("FusionSlam");
        this.fusionSlam = fusionSlam;
        this.totalLiDarSensors =totalLiDarSensors;
        this.totalCameraSensors = totalCameraSensors;
        LinkedList<CloudPoint> l = new LinkedList<>();
        l.add(new CloudPoint(2,3));
      //  LinkedList<CloudPoint> l2 =  transformToGlobalCoordinates(new TrackedObject("ewr",0,"ewrewr",l),new Pose(5,10,30,0));
       // System.out.println("TEST ### : " + l2.toString());
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, tick -> {
           // currentTick++; // Increment the tick on every broadcast
           // currentTick = tick.getTickNumber(); // Directly synchronize with TickBroadcast
            this.currentTick = tick.getTickNumber();
            System.out.println("FusionSlamService - Recived TickBrodcast at tick : "  + currentTick);
            if (!pendingEvents.isEmpty() && this.currentPose != null) {
                Iterator<TrackedObjectsEvent> iterator = pendingEvents.iterator();
                while (iterator.hasNext()) {
                    TrackedObjectsEvent event = iterator.next();
                    proccessTrackedObjectEvent(event);
                    iterator.remove(); // Safely remove processed event
                }
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, terminated -> {
            if (terminated.getName().equals("TimeService")){
                GurionRockRunner.isTerminatedEarly=new AtomicBoolean(true);
                terminate();
            }
            else {
                terminatedSensors.add(terminated.getName());
                if (terminatedSensors.size() == this.totalCameraSensors + this.totalLiDarSensors) {
                    System.out.println("terminatedsensors.size is " + terminatedSensors.size());
                    System.out.println("SystemRunTime is " + StatisticalFolder.getInstance().getSystemRuntime());
                    generateOutputFile();
                    sendBroadcast(new TerminatedBroadcast(this.getName()));
                    //this is for TimeService to terminate and not run for nothing
                    GurionRockRunner.isTerminatedEarly = new AtomicBoolean(true);
                    System.out.println("FusionSlamService terminated at : " + this.currentTick);
                    terminate();
                }
            }

        });

        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            GurionRockRunner.isTerminatedEarly=new AtomicBoolean(true);
           // generateErrorFile(crashed);
            // Wait until all services have contributed their data
           /* while (!crashed.getData().isComplete(this.totalCameraSensors, this.totalLiDarSensors)) {
                try {
                    //hread.sleep(100); // Small delay to prevent busy waiting
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }*/
            generateErrorReport(crashed.getData());
            terminate();
        });

        subscribeEvent(PoseEvent.class, event -> {
            currentPose = event.getPose();
            fusionSlam.updatePose(currentPose); // Assuming FusionSlam has this method
            complete(event, true); // Signal event completion
        });



        //DOCUS HERE!!!!
        subscribeEvent(TrackedObjectsEvent.class, trackedEvent -> {
            System.out.println(getName() + " received TrackedObjectsEvent with "
                    + trackedEvent.getTrackedObjects().size() + " objects.");

            if (currentPose != null) {
                proccessTrackedObjectEvent(trackedEvent);
        }
        else{
            pendingEvents.add(trackedEvent);
        }
        });
    }

    private void generateErrorReport(CrashData crashData) {
        try (FileWriter writer = new FileWriter("error_output.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            JsonObject errorReport = new JsonObject();

            // Add statistics
            JsonElement statistics = gson.toJsonTree(StatisticalFolder.getInstance());
            errorReport.add("statistics", statistics);

            // Add crash data
            JsonElement crashDataJson = gson.toJsonTree(crashData);
            errorReport.add("crashData", crashDataJson);

            // Write the final JSON object to the file
            gson.toJson(errorReport, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void proccessTrackedObjectEvent(TrackedObjectsEvent e){
        fusionSlam.turnTrackedObjectsToLandmarks(e.getTrackedObjects(),this.currentPose);
        complete(e, true);
    }

    private void generateOutputFile() {
        try (FileWriter writer = new FileWriter("output.json")) {
            Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
            // Build the output structure
            Map<String, Object> output = new HashMap<>();
            output.put("systemRuntime",StatisticalFolder.getInstance().getSystemRuntime()); // Adjust runtime dynamically if needed
            output.put("numDetectedObjects", StatisticalFolder.getInstance().getNumDetectedObjects());
            output.put("numTrackedObjects", StatisticalFolder.getInstance().getNumTrackedObjects());
            output.put("numLandmarks", StatisticalFolder.getInstance().getNumLandmarks());

            // Convert List<Landmark> into a Map<String, Object>
            Map<String, Map<String, Object>> landmarksMap = new HashMap<>();
            for (LandMark landmark : this.getFusionSlam().getLandmarks()) {
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
        }
    }




    public int getCurrentTick() {
        return currentTick;
    }

    public FusionSlam getFusionSlam() {
        return fusionSlam;
    }

    public Pose getCurrentPose() {
        return currentPose;
    }
}