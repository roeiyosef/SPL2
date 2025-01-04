package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ✅ **LiDarWorkerTracker Test Suite**
 * Tests key functionality of LiDarWorkerTracker:
 * 1. Valid Event Processing
 * 2. LiDAR in ERROR State
 * 3. Empty Detected Object List
 */
public class LiDarWorkerTrackerTest {

    private LiDarWorkerTracker lidarWorker;
    private LiDarDataBase lidarDatabase;

    @BeforeEach
    public void setUp() {
        lidarWorker = new LiDarWorkerTracker(1, 2); // LiDAR ID=1, Frequency=2
        lidarDatabase = LiDarDataBase.getInstance(""); // Get Singleton Instance

        // Clear previous data
        lidarDatabase.clear(); // Assuming there’s a clear method for testing purposes

        // Add sample cloud point data
        LinkedList<LinkedList<Double>> cloudPoints = new LinkedList<>();
        cloudPoints.add(new LinkedList<>(Arrays.asList(1.0, 2.0)));
        cloudPoints.add(new LinkedList<>(Arrays.asList(3.0, 4.0)));
        lidarDatabase.addStampedCloudPoints(new StampedCloudPoints("Obj1",2,cloudPoints));
    }

    /**
     * ✅ **Test 1: Valid Event Processing**
     * Pre-condition: A valid DetectObjectsEvent is passed to the method.
     * Post-condition: Returns a valid TrackedObjectsEvent with processed objects.
     */
    @Test
    public void testProcessValidDetectObjectsEvent() {
        // Pre-condition
        LinkedList<DetectedObject> detectedObjects = new LinkedList<>();
        detectedObjects.add(new DetectedObject("Obj1", "Valid Object"));

        StampedDetectedObjects stampedObjects = new StampedDetectedObjects(2, detectedObjects);
        DetectObjectsEvent event = new DetectObjectsEvent(stampedObjects);

        // Action
        TrackedObjectsEvent result = lidarWorker.processDetectObjectsEvent(event);

        // Post-condition
        assertNotNull(result, "TrackedObjectsEvent should not be null for a valid event");
        assertEquals(1, result.getTrackedObjects().size(), "Should track one object");
        assertEquals("Obj1", result.getTrackedObjects().get(0).getId(), "Tracked object ID mismatch");
        assertEquals(2, result.getTrackedObjects().get(0).getCoordinates().size(), "Tracked object should have two cloud points");
    }

    /**
     * ✅ **Test 2: LiDAR in ERROR State**
     * Pre-condition: LiDAR is in ERROR state.
     * Post-condition: Returns null, cannot process events.
     */
    @Test
    public void testProcessEventInErrorState() {
        // Pre-condition
        lidarWorker.markError(); // Set LiDAR to ERROR state

        LinkedList<DetectedObject> detectedObjects = new LinkedList<>();
        detectedObjects.add(new DetectedObject("Obj1", "Valid Object"));

        StampedDetectedObjects stampedObjects = new StampedDetectedObjects(2, detectedObjects);
        DetectObjectsEvent event = new DetectObjectsEvent(stampedObjects);

        // Action
        TrackedObjectsEvent result = lidarWorker.processDetectObjectsEvent(event);

        // Post-condition
        assertNull(result, "Should return null when LiDAR is in ERROR state");
    }

    /**
     * ✅ **Test 3: Empty Detected Object List**
     * Pre-condition: Event contains an empty list of detected objects.
     * Post-condition: Returns null, no objects to process.
     */
    @Test
    public void testProcessEventWithEmptyDetectedObjects() {
        // Pre-condition
        LinkedList<DetectedObject> detectedObjects = new LinkedList<>(); // Empty list

        StampedDetectedObjects stampedObjects = new StampedDetectedObjects(2, detectedObjects);
        DetectObjectsEvent event = new DetectObjectsEvent(stampedObjects);

        // Action
        TrackedObjectsEvent result = lidarWorker.processDetectObjectsEvent(event);

        // Post-condition
        assertNull(result, "Should return null when detected object list is empty");
    }
}