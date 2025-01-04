package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class CameraTest {

    private Camera camera;

    @BeforeEach
    public void setUp() {
        camera = new Camera(1, 2); // ID: 1, Frequency: 2

        LinkedList<StampedDetectedObjects> objectsList = new LinkedList<>();

        LinkedList<DetectedObject> detectedObjects1 = new LinkedList<>();
        detectedObjects1.add(new DetectedObject("Obj1", "Valid Object"));

        LinkedList<DetectedObject> detectedObjects2 = new LinkedList<>();
        detectedObjects2.add(new DetectedObject("ERROR", "Faulty Object"));

        objectsList.add(new StampedDetectedObjects(2, detectedObjects1));
        objectsList.add(new StampedDetectedObjects(4, detectedObjects2));

        camera.setListOfStampedDetectedObjects(objectsList);
    }

    /**
     * ✅ **Test 1: Valid Tick - Objects Are Prepared**
     * Pre-condition: Valid tick with properly detected objects.
     * Post-condition: Returns StampedDetectedObjects.
     */
    @Test
    public void testPrepareDetectedObjectsValidTick() {
        // Pre-condition: Valid tick
        int validTick = 2;

        // Action
        StampedDetectedObjects result = camera.prepareDetectedObjects(validTick);

        // Post-condition
        assertNotNull(result, "StampedDetectedObjects should not be null for a valid tick");
        assertEquals(1, result.getDetectedObjects().size(), "Should detect one object");
        assertEquals("Obj1", result.getDetectedObjects().get(0).getId(), "Detected object ID mismatch");
    }

    /**
     * ✅ **Test 2: Error Tick - Objects Contain Error**
     * Pre-condition: Tick with an error in detected objects.
     * Post-condition: Returns null, camera marked as ERROR.
     */
    @Test
    public void testPrepareDetectedObjectsErrorTick() {
        // Pre-condition: Error tick
        int errorTick = 4;

        // Action
        StampedDetectedObjects result = camera.prepareDetectedObjects(errorTick);

        // Post-condition
        assertNull(result, "Should return null when error detected");
        assertEquals(STATUS.ERROR, camera.getStatus(), "Camera should be marked as ERROR");
        assertEquals("Faulty Object", camera.getErrorDescription(), "Error description mismatch");
    }

    /**
     * ✅ **Test 3: Invalid Tick - No Objects Detected**
     * Pre-condition: Tick without any objects detected.
     * Post-condition: Returns null, no error set.
     */
    @Test
    public void testPrepareDetectedObjectsInvalidTick() {
        // Pre-condition: Invalid tick
        int invalidTick = 5;

        // Action
        StampedDetectedObjects result = camera.prepareDetectedObjects(invalidTick);

        // Post-condition
        assertNull(result, "Should return null when no objects are detected");
        assertEquals(STATUS.UP, camera.getStatus(), "Camera status should remain UP");
    }
}