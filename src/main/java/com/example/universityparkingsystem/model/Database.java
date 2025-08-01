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
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {
    private static String CONNECTION_STRING;
    private static final String DATABASE_NAME = "university_parking_system";
    private static final String SLOTS_COLLECTION = "slots";
    private static final String BOOKINGS_COLLECTION = "bookings";

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    static {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);

        CONNECTION_STRING = System.getenv("MONGODB_URI");

        if (CONNECTION_STRING == null || CONNECTION_STRING.isEmpty()) {
            try (InputStream input = Database.class.getClassLoader().getResourceAsStream("application.properties")) {
                Properties prop = new Properties();
                if (input != null) {
                    prop.load(input);
                    CONNECTION_STRING = prop.getProperty("mongodb.uri");
                }
            } catch (Exception e) {
                System.err.println("Failed to load application.properties or MONGODB_URI not set.");
                e.printStackTrace();
            }
        }
    }

    public static void connect() {
        if (mongoClient == null) {
            try {
                mongoClient = MongoClients.create(CONNECTION_STRING);
                database = mongoClient.getDatabase(DATABASE_NAME);
                System.out.println("Connected to MongoDB database.");
            } catch (Exception e) {
                System.err.println("Failed to connect to MongoDB: " + e.getMessage());
            }
        }
    }

    public static void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            System.out.println("Disconnected from MongoDB.");
        }
    }

    public static List<Slot> getSlots(String parkingLot) {
        List<Slot> slots = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection(SLOTS_COLLECTION);
            for (Document doc : collection.find(Filters.eq("parkingLot", parkingLot))) {
                slots.add(mapDocumentToSlot(doc));
            }
        } catch (Exception e) {
            System.err.println("Error fetching slots: " + e.getMessage());
        }
        return slots;
    }

    public static Slot getSlot(String slotNo, String parkingLot) {
        try {
            MongoCollection<Document> collection = database.getCollection(SLOTS_COLLECTION);
            Document doc = collection.find(Filters.and(
                    Filters.eq("slotNo", slotNo),
                    Filters.eq("parkingLot", parkingLot)
            )).first();
            if (doc != null) {
                return mapDocumentToSlot(doc);
            }
        } catch (Exception e) {
            System.err.println("Error fetching slot: " + e.getMessage());
        }
        return null;
    }

    public static void updateSlot(Slot slot) {
        try {
            MongoCollection<Document> collection = database.getCollection(SLOTS_COLLECTION);
            Document doc = new Document("slotNo", slot.getSlotNo())
                    .append("parkingLot", slot.getParkingLot())
                    .append("available", slot.isAvailable())
                    .append("bookingEndTime", slot.getBookingEndTime() != null ?
                            slot.getBookingEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                    .append("remainingMinutes", slot.getRemainingMinutes());

            collection.replaceOne(Filters.and(
                    Filters.eq("slotNo", slot.getSlotNo()),
                    Filters.eq("parkingLot", slot.getParkingLot())
            ), doc);
        } catch (Exception e) {
            System.err.println("Error updating slot: " + e.getMessage());
        }
    }

    public static void createBooking(Booking booking) {
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKINGS_COLLECTION);
            Document doc = new Document("slotNo", booking.getSlotNo())
                    .append("parkingLot", booking.getParkingLot())
                    .append("licensePlate", booking.getLicensePlate())
                    .append("durationMinutes", booking.getDurationMinutes())
                    .append("startTime", booking.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            collection.insertOne(doc);
        } catch (Exception e) {
            System.err.println("Error creating booking: " + e.getMessage());
        }
    }

    public static void deleteBooking(String slotNo, String parkingLot) {
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKINGS_COLLECTION);
            collection.deleteOne(Filters.and(
                    Filters.eq("slotNo", slotNo),
                    Filters.eq("parkingLot", parkingLot)
            ));
        } catch (Exception e) {
            System.err.println("Error deleting booking: " + e.getMessage());
        }
    }

    public static Booking getBookingBySlot(String slotNo, String parkingLot) {
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKINGS_COLLECTION);
            Document doc = collection.find(Filters.and(
                    Filters.eq("slotNo", slotNo),
                    Filters.eq("parkingLot", parkingLot)
            )).first();

            if (doc != null) {
                String licensePlate = doc.getString("licensePlate");
                int durationMinutes = doc.getInteger("durationMinutes");
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

    private static Slot mapDocumentToSlot(Document doc) {
        String slotNo = doc.getString("slotNo");
        String parkingLot = doc.getString("parkingLot");
        boolean available = doc.getBoolean("available");
        int remainingMinutes = doc.getInteger("remainingMinutes", 0);
        String bookingEndTimeStr = doc.getString("bookingEndTime");
        LocalDateTime bookingEndTime = (bookingEndTimeStr != null) ?
                LocalDateTime.parse(bookingEndTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;

        Slot slot = new Slot(slotNo, available, remainingMinutes, parkingLot);
        slot.setBookingEndTime(bookingEndTime);
        return slot;
    }
}
