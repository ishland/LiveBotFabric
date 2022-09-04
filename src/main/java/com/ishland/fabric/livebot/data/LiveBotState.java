package com.ishland.fabric.livebot.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.UUID;

public class LiveBotState implements Serializable {

    private static final Logger logger = LogManager.getLogger("LiveBotFabric DataManager");

    private static final long serialVersionUID = -1114033116391063571L;
    private static final File data = new File("./LiveBotFabric/data.dat");
    private static LiveBotState instance = null;

    public UUID followedEntity = null;

    public String dim = "";
    public double x = Double.NEGATIVE_INFINITY;
    public double y = Double.NEGATIVE_INFINITY;
    public double z = Double.NEGATIVE_INFINITY;
    public double yaw = Double.NEGATIVE_INFINITY;
    public double pitch = Double.NEGATIVE_INFINITY;

    private LiveBotState() {
        instance = this;
    }

    public static LiveBotState getInstance() {
        return instance != null ? instance : new LiveBotState();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    public synchronized void load() {
        try {
            if (!data.exists()) save();
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(data));
            instance = (LiveBotState) in.readObject();
            in.close();
        } catch (Throwable throwable) {
            logger.warn("Error occurred while loading data", throwable);
            throw new RuntimeException("Error occurred while loading data", throwable);
        }
    }

    public synchronized void save() {
        try {
            //noinspection ResultOfMethodCallIgnored
            data.getParentFile().mkdirs();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(data));
            out.writeObject(this);
            out.flush();
            out.close();
        } catch (Throwable throwable) {
            logger.warn("Error occurred while saving data", throwable);
            throw new RuntimeException("Error occurred while saving data", throwable);
        }
    }
}
