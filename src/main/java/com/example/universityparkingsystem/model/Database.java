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
        // Suppress MongoDB driver logs for cleaner console output
        Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);

        // Try to get connection string from environment variables first
        CONNECTION_STRING = System.getenv("MONGODB_URI");

        // If not found, fall back to properties file
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

    /**
     * Establishes a connection to the MongoDB database.
     */
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

    /**
     * Disconnects from the MongoDB database.
     */
    public static void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            System.out.println("Disconnected from MongoDB.");
        }
    }

    /**
     * Fetches all slots for a given parking lot.
     * @param parkingLot The name of the parking lot (e.g., "Lot01").
     * @return A list of Slot objects.
     */
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

    /**
     * Fetches a single slot by its number and parking lot.
     * @param slotNo The slot number.
     * @param parkingLot The parking lot name.
     * @return The Slot object or null if not found.
     */
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

    /**
     * Optimized method to find the first available slot across all parking lots.
     * @return An available Slot object or null if no slots are free.
     */
    public static Slot findAvailableSlot() {
        // Prioritize Lot01, then Lot02
        List<String> parkingLots = List.of("Lot01", "Lot02");
        for (String parkingLot : parkingLots) {
            List<Slot> slots = getSlots(parkingLot);
            for (Slot slot : slots) {
                if (slot.isAvailable()) {
                    return slot;
                }
            }
        }
        return null; // No available slot found
    }

    /**
     * Updates an existing slot's data in the database.
     * @param slot The Slot object to update.
     */
    public static void updateSlot(Slot slot) {
        try {
            MongoCollection<Document> collection = database.getCollection(SLOTS_COLLECTION);
            Document updatedDoc = new Document("slotNo", slot.getSlotNo())
                    .append("parkingLot", slot.getParkingLot())
                    .append("available", slot.isAvailable())
                    .append("bookingEndTime", slot.getBookingEndTime() != null ? slot.getBookingEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                    .append("remainingMinutes", slot.getRemainingMinutes());
            collection.replaceOne(Filters.and(
                    Filters.eq("slotNo", slot.getSlotNo()),
                    Filters.eq("parkingLot", slot.getParkingLot())
            ), updatedDoc);
        } catch (Exception e) {
            System.err.println("Error updating slot: " + e.getMessage());
        }
    }

    /**
     * Creates a new booking entry in the database.
     * @param booking The Booking object to save.
     */
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

    /**
     * Fetches a booking by its associated slot.
     * @param slotNo The slot number.
     * @param parkingLot The parking lot name.
     * @return The Booking object or null.
     */
    public static Booking getBookingBySlot(String slotNo, String parkingLot) {
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKINGS_COLLECTION);
            Document doc = collection.find(Filters.and(
                    Filters.eq("slotNo", slotNo),
                    Filters.eq("parkingLot", parkingLot)
            )).first();
            if (doc != null) {
                return mapDocumentToBooking(doc);
            }
        } catch (Exception e) {
            System.err.println("Error fetching booking: " + e.getMessage());
        }
        return null;
    }

    /**
     * Helper method to map a MongoDB Document to a Slot object.
     * Uses default values to prevent NullPointerExceptions.
     */
    private static Slot mapDocumentToSlot(Document doc) {
        String slotNo = doc.getString("slotNo");
        boolean available = doc.getBoolean("available", true);
        int remainingMinutes = doc.getInteger("remainingMinutes", 0);
        String parkingLot = doc.getString("parkingLot");

        // Retrieve booking end time
        String bookingEndTimeStr = doc.getString("bookingEndTime");
        LocalDateTime bookingEndTime = (bookingEndTimeStr != null) ?
                LocalDateTime.parse(bookingEndTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;

        Slot slot = new Slot(slotNo, available, remainingMinutes, parkingLot);
        slot.setBookingEndTime(bookingEndTime); // Set the end time
        return slot;
    }

    /**
     * Helper method to map a MongoDB Document to a Booking object.
     * Uses default values to prevent NullPointerExceptions.
     */
    private static Booking mapDocumentToBooking(Document doc) {
        String slotNo = doc.getString("slotNo");
        String parkingLot = doc.getString("parkingLot");
        String licensePlate = doc.getString("licensePlate");
        int durationMinutes = doc.getInteger("durationMinutes", 0);
        LocalDateTime startTime = LocalDateTime.parse(doc.getString("startTime"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new Booking(slotNo, parkingLot, licensePlate, durationMinutes, startTime);
    }
}
