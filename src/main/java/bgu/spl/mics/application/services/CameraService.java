package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public Camera camera;
    private int currentTick;
    public static AtomicInteger CameraCounter = new AtomicInteger(0);

    // Pending events map (Tick → List of Events)
    private final Map<Integer, DetectObjectsEvent> pendingEvents = new HashMap<>();



   // private LinkedList<DetectedObject> recentDetectedObjects;
   private StampedDetectedObjects recentDetectedObjects;

    private AtomicInteger counter = new AtomicInteger(0);

  //  private int latestDetectionTime = 0;
    public CameraService(Camera camera) {
        super("CameraService " + CameraCounter.incrementAndGet() );
        //super("CameraService");
        this.camera = camera;
        this.currentTick = 0;
/*
        this.latestDetectionTime = camera.getLatestDetecetionTime();
*/
        recentDetectedObjects = null;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, tick -> {
            this.currentTick = tick.getTickNumber();
            System.out.println(this.getName() + " Recived TickBrodcast at tick : "  + currentTick);

            StampedDetectedObjects detectedObjects = camera.prepareDetectedObjects(currentTick);
            if (detectedObjects != null) {
                if (detectedObjects.getDetectedObjects().isEmpty()) {
                    counter.incrementAndGet();
                } else {
                    // this.recentDetectedObjects = new LinkedList<>(detectedObjectsTosend);
                    this.recentDetectedObjects = detectedObjects;
                    for (int i = 0; i < detectedObjects.getDetectedObjects().size(); i++) {
                        StatisticalFolder.getInstance().incrementDetectedObjects();
                    }
                    DetectObjectsEvent event = new DetectObjectsEvent(detectedObjects);
                    this.pendingEvents.put(this.currentTick + camera.getFrequency(), event);
                }
            }
            else{
                if (this.camera.getStatus() == STATUS.ERROR){
                    System.out.println("FOUND ERROR !!!!");
                   // camera.markError(detectedObjects.get(errorIndex).getDescription());
                    String faultySensorType = "camera";
                    int faultySensorId = this.camera.getId();
                    String errorMessage = this.camera.getErrorDescription();
                   // String errorMessage = detectedObjects.getDetectedObjects().get(errorIndex).getDescription();
                    //StatisticalFolder.getInstance().setSystemRuntime(this.currentTick);
                    CrashedBroadcast crashed  = new CrashedBroadcast(errorMessage,"Camera" + faultySensorId,this.currentTick);
                    //sendBroadcast(new CrashedBroadcast(errorMessage,"Camera" + faultySensorId,this.currentTick));
                    crashed.getData().addCameraFrames("Camera" + this.camera.getId(),(this.recentDetectedObjects));
                    sendBroadcast(crashed);
                    terminate();

                }
            }

            if (pendingEvents.containsKey(currentTick)) {
                sendEvent(pendingEvents.remove(currentTick));
                counter.incrementAndGet();
                System.out.println("COUNTER OF " + this.getName() + " IS : " + counter + " OF TOTAL OF : " +this.camera.getDetectedObjectsList().size() );
                System.out.println();
                System.out.println("Camera: Sent DetectObjectsEvent at Tick " + currentTick);
            }

            if(counter.get() == this.camera.getDetectedObjectsList().size()){
                camera.markDown();
                sendBroadcast(new TerminatedBroadcast(this.getName()));
                terminate();
            }

          /*  LinkedList<DetectedObject> detectedObjectsTosend = camera.getDetectedObjectsAtTick(currentTick);
          //  System.out.println("DETECTED OBJECTS AT TICK : " + currentTick + " are : " + detectedObjectsTosend.toString());
            if (detectedObjectsTosend!=null) {
                if (detectedObjectsTosend.isEmpty()) {
                    counter.incrementAndGet();
                } else {
                    int errorIndex = camera.ErrorInDetectedObjects(detectedObjectsTosend);
                    if (errorIndex != -1) {
                        System.out.println("FOUND ERROR !!!!");
                        camera.markError();
                        String faultySensorType = "camera";
                        int faultySensorId = this.camera.getId();
                        String errorMessage = detectedObjectsTosend.get(errorIndex).getDescription();
                        //StatisticalFolder.getInstance().setSystemRuntime(this.currentTick);
                        CrashedBroadcast crashed  = new CrashedBroadcast(errorMessage,"Camera" + faultySensorId,this.currentTick);
                        //sendBroadcast(new CrashedBroadcast(errorMessage,"Camera" + faultySensorId,this.currentTick));
                        crashed.getData().addCameraFrames("Camera" + this.camera.getId(),(this.recentDetectedObjects));
                        sendBroadcast(crashed);
                        terminate();

                    } else {
                       // this.recentDetectedObjects = new LinkedList<>(detectedObjectsTosend);
                        this.recentDetectedObjects = this.camera.getStampedDetectedObjectsAtTick(currentTick);
                        //create new DetectObjectEvent
                        DetectObjectsEvent event = new DetectObjectsEvent(camera.getStampedDetectedObjectsAtTick(currentTick));
                        this.pendingEvents.put(this.currentTick + camera.getFrequency(), event);
                        //update counter for following how much of the stampeddetectedobjects in camera we have covered

                        //For statistics
                        for (int i = 0; i < detectedObjectsTosend.size(); i++) {
                            StatisticalFolder.getInstance().incrementDetectedObjects();
                        }

                    }
                }
            }

            if (pendingEvents.containsKey(currentTick)) {
                sendEvent(pendingEvents.remove(currentTick));
                counter.incrementAndGet();
                System.out.println("COUNTER OF " + this.getName() + " IS : " + counter + " OF TOTAL OF : " +this.camera.getDetectedObjectsList().size() );
                System.out.println();
                System.out.println("Camera: Sent DetectObjectsEvent at Tick " + currentTick);
            }

        if(counter.get() == this.camera.getDetectedObjectsList().size()){
            camera.markDown();
            sendBroadcast(new TerminatedBroadcast(this.getName()));
            terminate();
        }*/
        });


        subscribeBroadcast(TerminatedBroadcast.class, terminated -> {
            if (terminated.getName().equals("TimeService")){
                camera.markDown();
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            camera.markDown();
            crashed.getData().addCameraFrames("Camera" + this.camera.getId(),(this.recentDetectedObjects));
            terminate();
        });


    }

  /*  private int ErrorInDetectedObjects(LinkedList<DetectedObject> list) {
        System.out.println("CHECKING ERROR IN " + this.getName() + "  +  LIST : " + list.toString());
        for (int i=0;i<list.size();i++){
            if (list.get(i).getId().equals("ERROR")){
                System.out.println("Found it it is at : " + list.get(i).toString());
                return i;
            }
        }
        //if the list is Valid!
        return -1;
    }*/



}
