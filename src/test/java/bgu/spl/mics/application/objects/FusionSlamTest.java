package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.*;
import java.util.LinkedList;
import static org.junit.jupiter.api.Assertions.*;

public class FusionSlamTest {

    private FusionSlam fusionSlam;

    @BeforeEach
    public void setUp() {
        fusionSlam = FusionSlam.getInstance();
        fusionSlam.getLandmarks().clear();
        fusionSlam.getPoses().clear();
    }

    @AfterEach
    public void tearDown() {
        fusionSlam.getLandmarks().clear();
        fusionSlam.getPoses().clear();
    }

    /**
     * ✅ **Test 1: Coordinate Transformation Accuracy**
     * Pre-condition: A `TrackedObject` with local cloud points and a `Pose` object.
     * Post-condition: Transformed cloud points match expected global coordinates.
     * Invariant: Transformation follows the mathematical formula.
     */
    @Test
    public void testCoordinateTransformationAccuracy() {
        // Pre-condition
        Pose pose = new Pose(5, 10, 30, 1); // x=5, y=10, yaw=30°, time=1
        LinkedList<CloudPoint> localPoints = new LinkedList<>();
        localPoints.add(new CloudPoint(2.0, 3.0)); // Local cloud point

        TrackedObject trackedObject = new TrackedObject("obj1", 1 ,"testObject", localPoints);

        // Action
        LinkedList<CloudPoint> globalPoints = fusionSlam.transformToGlobalCoordinates(trackedObject, pose);

        // Post-condition
        assertNotNull(globalPoints, "Global points should not be null");
        assertEquals(1, globalPoints.size(), "There should be one global point");
        assertEquals(5.232, globalPoints.get(0).getX(), 0.001, "X coordinate mismatch");
        assertEquals(13.598, globalPoints.get(0).getY(), 0.001, "Y coordinate mismatch");
    }

    /**
     * ✅ **Test 2: Tracked Objects to Landmarks Integration**
     * Pre-condition: A list of `TrackedObject` instances with associated `Pose`.
     * Post-condition:
     *    - New landmarks are added correctly.
     *    - Existing landmarks are updated/refined properly.
     * Invariant: Landmark IDs are unique, and coordinates are updated accurately.
     */
    @Test
    public void testTurnTrackedObjectsToLandmarks() {
        // Pre-condition
        Pose pose = new Pose(5, 10, 30, 1); // x=5, y=10, yaw=30°, time=1
        fusionSlam.updatePose(pose);

        LinkedList<CloudPoint> localPoints1 = new LinkedList<>();
        localPoints1.add(new CloudPoint(2.0, 3.0));

        LinkedList<CloudPoint> localPoints2 = new LinkedList<>();
        localPoints2.add(new CloudPoint(1.0, 1.0));

        TrackedObject trackedObject1 = new TrackedObject("landmark1",1, "Tree", localPoints1 );
        TrackedObject trackedObject2 = new TrackedObject("landmark1",1, "Tree", localPoints2); // Same ID for refinement

        LinkedList<TrackedObject> trackedObjects = new LinkedList<>();
        trackedObjects.add(trackedObject1);
        trackedObjects.add(trackedObject2);

        // Action
        fusionSlam.turnTrackedObjectsToLandmarks(trackedObjects, pose);

        // Post-condition: Check landmarks
        assertEquals(1, fusionSlam.getLandmarks().size(), "There should be exactly one landmark");

        LandMark landmark = fusionSlam.getLandmarks().getFirst();
        assertEquals("landmark1", landmark.getId(), "Landmark ID mismatch");
        assertEquals("Tree", landmark.getDescription(), "Landmark description mismatch");

        LinkedList<CloudPoint> refinedCoordinates = landmark.getCoordinates();
        assertEquals(1, refinedCoordinates.size(), "Refined coordinates size mismatch");
        assertEquals(5.299f, refinedCoordinates.get(0).getX(), 0.001f, "Refined X coordinate mismatch");
        assertEquals(12.482f, refinedCoordinates.get(0).getY(), 0.001f, "Refined Y coordinate mismatch");
    }
}
