package bgu.spl.mics.application.objects;

/**
 * CloudPoint represents a specific point in a 3D space as detected by the LiDAR.
 * These points are used to generate a point cloud representing objects in the environment.
 */
public class CloudPoint {
    private final double x;
    private final double y;

    public CloudPoint(double _x,double _y){
        this.x = _x;
        this.y = _y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    public String toString() {
        return "CloudPoint{" +  "x=" + x + ", y=" + y + "}";
    }
}
