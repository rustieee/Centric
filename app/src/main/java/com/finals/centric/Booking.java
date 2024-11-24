package com.finals.centric;

import java.util.List;

public class Booking {
    private String check_in_date;
    private String check_out_date;
    private String roomId;
    private String status;
    private String user_id;
    private List<String> companions;  // Add a list to store companion names

    public Booking() {
        // Default constructor required for Firestore
    }

    public Booking(String check_in_date, String check_out_date, String roomId, String status, String user_id, List<String> companions) {
        this.check_in_date = check_in_date;
        this.check_out_date = check_out_date;
        this.roomId = roomId;
        this.status = status;
        this.user_id = user_id;
        this.companions = companions;
    }

    // Getters and setters for the fields

    public String getCheck_in_date() {
        return check_in_date;
    }

    public void setCheck_in_date(String check_in_date) {
        this.check_in_date = check_in_date;
    }

    public String getCheck_out_date() {
        return check_out_date;
    }

    public void setCheck_out_date(String check_out_date) {
        this.check_out_date = check_out_date;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<String> getCompanions() {
        return companions;
    }

    public void setCompanions(List<String> companions) {
        this.companions = companions;
    }
}
