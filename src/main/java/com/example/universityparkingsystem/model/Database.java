package com.example.universityparkingsystem.model;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Database {
    private static String CONNECTION_STRING;
    private static final String DATABASE_NAME = "parkingSystem";
    private static final String SLOTS_COLLECTION = "slots";
    private static final String BOOKINGS_COLLECTION = "bookings";

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    // Static block to load connection string
    static {
        CONNECTION_STRING = System.getenv("MONGODB_URI");

        if (CONNECTION_STRING == null || CONNECTION_STRING.isEmpty()) {
            try (InputStream input = Database.class.getClassLoader().getResourceAsStream("application.properties")) {
                Properties prop = new Properties();
                if (input != null) {
                    prop.load(input);
                    CONNECTION_STRING = prop.getProperty("mongodb.uri");
                } else {
                    System.err.println("application.properties not found in resources.");
                }
            } catch (Exception e) {
                System.err.println("Failed to load connection string from properties: " + e.getMessage());
            }
        }

        if (CONNECTION_STRING == null || CONNECTION_STRING.isEmpty()) {
            System.err.println("MongoDB connection string is not set. Please check environment variable or application.properties.");
        } else {
            System.out.println("MongoDB URI loaded: " + CONNECTION_STRING); // Debug
        }
    }

    public static void connect() {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
            System.out.println("✅ Connected to MongoDB Atlas successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error connecting to MongoDB Atlas: " + e.getMessage());
        }
    }

    public static void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    public static List<Slot> getAllSlots() {
        List<Slot> slots = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection(SLOTS_COLLECTION);

            for (Document doc : collection.find()) {
                int slotNo = doc.getInteger("slotNo");
                boolean available = doc.getBoolean("available");
                int remainingMinutes = doc.getInteger("remainingMinutes", 0);
                String parkingLot = doc.getString("parkingLot");

                slots.add(new Slot(slotNo, available, remainingMinutes, parkingLot));
            }
        } catch (Exception e) {
            System.err.println("Error fetching slots: " + e.getMessage());
        }
        return slots;
    }

    public static void updateSlot(Slot slot) {
        try {
            MongoCollection<Document> collection = database.getCollection(SLOTS_COLLECTION);

            Document filter = new Document("slotNo", slot.getSlotNo())
                    .append("parkingLot", slot.getParkingLot());
            Document update = new Document("$set",
                    new Document("available", slot.isAvailable())
                            .append("remainingMinutes", slot.getRemainingMinutes()));

            collection.updateOne(filter, update);
        } catch (Exception e) {
            System.err.println("Error updating slot: " + e.getMessage());
        }
    }

    public static void createBooking(Booking booking) {
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKINGS_COLLECTION);

            Document doc = new Document()
                    .append("slotNo", booking.getSlotNo())
                    .append("parkingLot", booking.getParkingLot())
                    .append("licensePlate", booking.getLicensePlate())
                    .append("durationMinutes", booking.getDurationMinutes())
                    .append("startTime", booking.getStartTime().toString());

            collection.insertOne(doc);
        } catch (Exception e) {
            System.err.println("Error creating booking: " + e.getMessage());
        }
    }

    public static Booking getBookingBySlot(int slotNo, String parkingLot) {
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKINGS_COLLECTION);
            Document doc = collection.find(
                    Filters.and(
                            Filters.eq("slotNo", slotNo),
                            Filters.eq("parkingLot", parkingLot)
                    )).first();

            if (doc != null) {
                int durationMinutes = doc.getInteger("durationMinutes");
                String licensePlate = doc.getString("licensePlate");
                String startTimeStr = doc.getString("startTime");

                LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                return new Booking(
                        slotNo,
                        parkingLot,
                        licensePlate,
                        durationMinutes,
                        startTime
                );
            }
        } catch (Exception e) {
            System.err.println("Error fetching booking: " + e.getMessage());
        }
        return null;
    }
}
