package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class
LiDarDataBase {
    private static LiDarDataBase instance = null;
    private LinkedList<StampedCloudPoints> cloudPoints = new LinkedList<>();
    private String filePath;
    private boolean initialized = false;


    AtomicInteger counter = new AtomicInteger(0);

    /**
     * Private constructor to prevent direct instantiation.
     */
    private LiDarDataBase() {}

    /**
     * Returns the singleton instance of LiDarDataBase, initializing it with the given file path on the first call.
     *
     * @param filePath The file path containing LiDAR data (used only during the first initialization).
     * @return The singleton instance of LiDarDataBase.
     */
    public static synchronized LiDarDataBase getInstance(String filePath) {
        if (instance == null) {
            instance = new LiDarDataBase();
            instance.initialize(filePath);
        }
        return instance;
    }


    /**
     * Initializes the database from a JSON file.
     *
     * @param filePath The file path containing LiDAR data.
     */
    private void initialize(String filePath) {
        if (!initialized) {
            this.filePath = filePath;
            try (Reader reader = new FileReader(filePath)) {
                Gson gson = new Gson();
                Type listType = new TypeToken<LinkedList<StampedCloudPoints>>() {}.getType();
                LinkedList<StampedCloudPoints> loadedData = gson.fromJson(reader, listType);
                if (loadedData != null) {
                    cloudPoints.addAll(loadedData);
                }
                initialized = true;
                System.out.println("LiDarDataBase initialized from file: " + filePath);
            } catch (FileNotFoundException e) {
                System.out.println("LiDarDataBase file not found: " + filePath + ". Starting with an empty database.");
            } catch (IOException e) {
                System.out.println("Error reading LiDarDataBase file: " + e.getMessage());
            }
        }
    }

    /**
     * Adds a new StampedCloudPoints entry to the database.
     *
     * @param points The StampedCloudPoints to add.
     */
    public synchronized void addCloudPoints(StampedCloudPoints points) {
        if (points != null) {
            cloudPoints.add(points);
            saveToFile(); // Persist changes immediately
        }
    }

    /**
     * Saves the current state of the LiDar database back to the file.
     */
    public synchronized void saveToFile() {
        if (filePath == null) {
            throw new IllegalStateException("Cannot save: File path is not set.");
        }

        try (Writer writer = new FileWriter(filePath)) {
            Gson gson = new Gson();
            gson.toJson(cloudPoints, writer);
            System.out.println("LiDarDataBase saved to file: " + filePath);
        } catch (IOException e) {
            System.out.println("Error writing LiDarDataBase to file: " + e.getMessage());
        }
    }

    /**
     * Retrieves cloud points corresponding to a specific tick (time).
     *
     * @param time The timestamp to query cloud points.
     * @return A LinkedList of StampedCloudPoints for the given time, or null if not found.
     */
    public synchronized StampedCloudPoints getCloudPointsByIdAndTime(String id, int time) {
        for (StampedCloudPoints scp : cloudPoints) {
            if (scp.getId().equals(id) && scp.getTime() == time) {
                return scp;
            }
        }
        return null;
    }

    public LinkedList<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }

    @Override
    public String toString() {
        return "LiDarDataBase{" +
                "cloudPoints=" + cloudPoints +
                ", filePath='" + filePath + '\'' +
                ", initialized=" + initialized +
                '}';
    }

    public synchronized StampedCloudPoints  getCloudPointsByIDAndTime(String id,int tick) {
        for (StampedCloudPoints scp : cloudPoints) {
            if (scp.getId().equals(id) && scp.getTime()==tick) {
                return scp;
            }
        }
        return null;
    }
    public synchronized LinkedList<StampedCloudPoints>  getCloudPointsByID(String id) {
        LinkedList<StampedCloudPoints> list = new LinkedList<>();
        for (StampedCloudPoints scp : cloudPoints) {
            if (scp.getId().equals(id)) {
                list.add(scp);
            }
        }
        return list;
    }
    public synchronized LinkedList<StampedCloudPoints> getCloudPointsByTime(int tick) {
        LinkedList<StampedCloudPoints> list = new LinkedList<>();
        for (StampedCloudPoints scp : cloudPoints) {
            if (scp.getTime()==tick) {
              list.add(scp);
            }
        }
        return list;
    }


    public AtomicInteger getCounter() {
        return counter;
    }

    public void increaseCounter() {
        this.counter.incrementAndGet();
    }


    //For testing
    public void clear() {
        this.cloudPoints= new LinkedList<>();
    }

    public void addStampedCloudPoints(StampedCloudPoints points) {
        this.cloudPoints.add(points);
    }
}
