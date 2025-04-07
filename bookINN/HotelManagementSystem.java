import java.io.*;
import java.util.*;
import java.time.LocalDate;

// Interface for booking and cancelling
interface Bookable {
    void bookRoom(Customer customer);
}

interface Cancelable {
    void cancelBooking();
}

// Abstract Room class
abstract class Room {
    protected int roomNumber;
    protected boolean isBooked;

    public Room(int roomNumber) {
        this.roomNumber = roomNumber;
        this.isBooked = false;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }

    public abstract double getPrice();
    public abstract String getRoomType();
}

class DeluxeRoom extends Room {
    public DeluxeRoom(int roomNumber) {
        super(roomNumber);
    }

    @Override
    public double getPrice() {
        return 1500.0;
    }

    @Override
    public String getRoomType() {
        return "Deluxe";
    }
}

class SuiteRoom extends Room {
    public SuiteRoom(int roomNumber) {
        super(roomNumber);
    }

    @Override
    public double getPrice() {
        return 2500.0;
    }

    @Override
    public String getRoomType() {
        return "Suite";
    }
}

class Customer {
    String name;
    String phone;
    Address address;

    static class Address {
        String city;
        String state;

        public Address(String city, String state) {
            this.city = city;
            this.state = state;
        }
    }

    public Customer(String name, String phone, String city, String state) {
        this.name = name;
        this.phone = phone;
        this.address = new Address(city, state);
    }
}

class Booking implements Bookable, Cancelable {
    private static int counter = 1;
    private final int bookingId;
    private final Room room;
    private final Customer customer;
    private final LocalDate bookingDate;

    public Booking(int id, Room room, Customer customer, LocalDate bookingDate) {
        this.bookingId = id;
        this.room = room;
        this.customer = customer;
        this.bookingDate = bookingDate;
        counter = Math.max(counter, id + 1);
    }

    public Booking(Room room, Customer customer) {
        this(counter++, room, customer, LocalDate.now());
    }

    public int getBookingId() {
        return bookingId;
    }

    public Room getRoom() {
        return room;
    }

    public Customer getCustomer() {
        return customer;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    @Override
    public void bookRoom(Customer customer) {
        if (!room.isBooked()) {
            room.setBooked(true);
            System.out.println("Room booked successfully.");
        } else {
            System.out.println("Room is already booked.");
        }
    }

    @Override
    public void cancelBooking() {
        room.setBooked(false);
        System.out.println("Booking cancelled successfully.");
    }
}

class Hotel {
    private final List<Room> rooms = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();

    public void addRoom(Room room) {
        for (Room r : rooms) {
            if (r.getRoomNumber() == room.getRoomNumber()) {
                System.out.println("Room number already exists.");
                return;
            }
        }
        rooms.add(room);
        saveRoomsToCSV();
        System.out.println("Room added successfully.");
    }

    public void showAvailableRooms() {
        boolean found = false;
        for (Room room : rooms) {
            if (!room.isBooked()) {
                System.out.println("Room Number: " + room.getRoomNumber() + ", Type: " + room.getRoomType() + ", Price: " + room.getPrice());
                found = true;
            }
        }
        if (!found) System.out.println("No available rooms.");
    }

    public void showAllRooms() {
        for (Room room : rooms) {
            System.out.println("Room Number: " + room.getRoomNumber() + ", Type: " + room.getRoomType() + ", Price: " + room.getPrice() + ", Booked: " + room.isBooked());
        }
    }

    public void bookRoom(int roomNumber, Customer customer) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber && !room.isBooked()) {
                Booking booking = new Booking(room, customer);
                booking.bookRoom(customer);
                bookings.add(booking);
                saveBookingsToCSV();
                return;
            }
        }
        System.out.println("Room not available or already booked.");
    }

    public void cancelBooking(int bookingId) {
        Iterator<Booking> it = bookings.iterator();
        while (it.hasNext()) {
            Booking booking = it.next();
            if (booking.getBookingId() == bookingId) {
                booking.cancelBooking();
                it.remove();
                saveBookingsToCSV();
                return;
            }
        }
        System.out.println("Booking ID not found.");
    }

    public void showAllBookings() {
        for (Booking booking : bookings) {
            System.out.println("Booking ID: " + booking.getBookingId() + ", Room: " + booking.getRoom().getRoomNumber() + ", Customer: " + booking.getCustomer().name + ", Date: " + booking.getBookingDate());
        }
    }

    public void saveBookingsToCSV() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("bookings.csv"))) {
            for (Booking booking : bookings) {
                writer.println(booking.getBookingId() + "," +
                        booking.getRoom().getRoomNumber() + "," +
                        booking.getRoom().getRoomType() + "," +
                        booking.getCustomer().name + "," +
                        booking.getCustomer().phone + "," +
                        booking.getCustomer().address.city + "," +
                        booking.getCustomer().address.state + "," +
                        booking.getBookingDate());
            }
        } catch (IOException e) {
            System.out.println("Error saving bookings: " + e.getMessage());
        }
    }

    public void saveRoomsToCSV() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("rooms.csv"))) {
            for (Room room : rooms) {
                writer.println(room.getRoomNumber() + "," + room.getRoomType() + "," + room.isBooked());
            }
        } catch (IOException e) {
            System.out.println("Error saving rooms: " + e.getMessage());
        }
    }

    public void loadRoomsFromCSV() {
        File file = new File("rooms.csv");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int roomNumber = Integer.parseInt(parts[0]);
                String type = parts[1];
                boolean isBooked = Boolean.parseBoolean(parts[2]);

                Room room = type.equals("Deluxe") ? new DeluxeRoom(roomNumber) : new SuiteRoom(roomNumber);
                room.setBooked(isBooked);
                rooms.add(room);
            }
        } catch (IOException e) {
            System.out.println("Error loading rooms: " + e.getMessage());
        }
    }

    public void loadBookingsFromCSV() {
        File file = new File("bookings.csv");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int bookingId = Integer.parseInt(parts[0]);
                int roomNumber = Integer.parseInt(parts[1]);
                String roomType = parts[2];
                String name = parts[3];
                String phone = parts[4];
                String city = parts[5];
                String state = parts[6];
                LocalDate date = LocalDate.parse(parts[7]);

                Room room = roomType.equals("Deluxe") ? new DeluxeRoom(roomNumber) : new SuiteRoom(roomNumber);
                room.setBooked(true);
                Customer customer = new Customer(name, phone, city, state);
                bookings.add(new Booking(bookingId, room, customer, date));

                boolean exists = false;
                for (Room r : rooms) {
                    if (r.getRoomNumber() == room.getRoomNumber()) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) rooms.add(room);
            }
        } catch (IOException e) {
            System.out.println("Error loading bookings: " + e.getMessage());
        }
    }
}

public class HotelManagementSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Hotel hotel = new Hotel();
        hotel.loadRoomsFromCSV();
        hotel.loadBookingsFromCSV();

        boolean running = true;
        while (running) {
            System.out.println("\nWelcome to Hotel Booking System");
            System.out.println("Please select your role:");
            System.out.println("1. Client");
            System.out.println("2. Hotel Staff");
            System.out.println("3. Exit");

            int role = scanner.nextInt();

            switch (role) {
                case 1 -> clientMenu(scanner, hotel);
                case 2 -> staffMenu(scanner, hotel);
                case 3 -> {
                    hotel.saveBookingsToCSV();
                    hotel.saveRoomsToCSV();
                    System.out.println("Exiting system. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    public static void clientMenu(Scanner scanner, Hotel hotel) {
        boolean running = true;
        while (running) {
            System.out.println("\nClient Menu:");
            System.out.println("1. View Available Rooms");
            System.out.println("2. Book a Room");
            System.out.println("3. Cancel Booking");
            System.out.println("4. Back");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> hotel.showAvailableRooms();
                case 2 -> {
                    System.out.print("Enter room number to book: ");
                    int roomNo = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Enter name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter phone: ");
                    String phone = scanner.nextLine();
                    System.out.print("Enter city: ");
                    String city = scanner.nextLine();
                    System.out.print("Enter state: ");
                    String state = scanner.nextLine();
                    Customer customer = new Customer(name, phone, city, state);
                    hotel.bookRoom(roomNo, customer);
                }
                case 3 -> {
                    System.out.print("Enter booking ID to cancel: ");
                    int id = scanner.nextInt();
                    hotel.cancelBooking(id);
                }
                case 4 -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    public static void staffMenu(Scanner scanner, Hotel hotel) {
        boolean running = true;
        while (running) {
            System.out.println("\nHotel Staff Menu:");
            System.out.println("1. Add Room");
            System.out.println("2. View All Rooms");
            System.out.println("3. View All Bookings");
            System.out.println("4. Back");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter room number: ");
                    int roomNo = scanner.nextInt();
                    System.out.println("Select room type: 1. Deluxe 2. Suite");
                    int type = scanner.nextInt();
                    Room room = (type == 1) ? new DeluxeRoom(roomNo) : new SuiteRoom(roomNo);
                    hotel.addRoom(room);
                }
                case 2 -> hotel.showAllRooms();
                case 3 -> hotel.showAllBookings();
                case 4 -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}