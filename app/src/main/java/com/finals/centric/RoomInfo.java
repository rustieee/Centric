package com.finals.centric;

public class RoomInfo {
    private String roomId;
    private String status;
    private String checkin;
    private String checkout;
    private Long price;
    private String roomName;
    private String roomType;
    private String details;

    // Default constructor required for Firestore deserialization
    public RoomInfo() {}

    public RoomInfo(String roomId, String status, String checkin, String checkout, Long price, String roomName, String roomType, String details) {
        this.roomId = roomId;
        this.status = status;
        this.price = price;
        this.roomName = roomName;
        this.roomType = roomType;
        this.details = details;
        this.checkin = checkin;
        this.checkout = checkout;
    }

    public String getRoomId() {
        return roomId != null ? roomId : "";
    }
    public String getStatus() { return status != null ? status : "AVAILABLE"; }
    public String getCheckin() { return checkin != null ? checkin : ""; }
    public String getCheckout() { return checkout != null ? checkout : ""; }
    public Long getPrice() { return price != null ? price : 0; }
    public String getRoomName() { return roomName != null ? roomName : "Unknown Room"; }
    public String getRoomType() { return roomType != null ? roomType : "Standard"; }
    public String getDetails() { return details != null ? details : ""; }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setCheckin(String checkin) {
        this.checkin = checkin;
    }

    public void setCheckout(String checkout) {
        this.checkout = checkout;
    }



    public static final int[] roomImagesAvailableReserved = {
            R.drawable.home_room1_avail_reserved,
            R.drawable.home_room2_avail_reserved,
            R.drawable.home_room3_avail_reserved,
            R.drawable.home_room4_avail_reserved
    };

    public static final int[] roomImagesReservedYou = {
            R.drawable.home_room1_reserved_you,
            R.drawable.home_room2_reserved_you,
            R.drawable.home_room3_reserved_you,
            R.drawable.home_room4_reserved_you
    };

    public static final int[] roomImagesOccupiedYou = {
            R.drawable.home_room1_occupied_you,
            R.drawable.home_room2_occupied_you,
            R.drawable.home_room3_occupied_you,
            R.drawable.home_room4_occupied_you
    };

    public static final int[] bookroomImagesAvailableReserved = {
            R.drawable.booking_room,
            R.drawable.booking_room,
            R.drawable.booking_room,
            R.drawable.booking_room
    };

    public static final int[] bookroomImagesReservedYou = {
            R.drawable.booking_room_res,
            R.drawable.booking_room_res,
            R.drawable.booking_room_res,
            R.drawable.booking_room_res
    };

    public static final int[] bookroomImagesOccupiedYou = {
            R.drawable.booking_room_occ,
            R.drawable.booking_room_occ,
            R.drawable.booking_room_occ,
            R.drawable.booking_room_occ
    };
}
