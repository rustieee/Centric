package com.finals.centric;

public class Bill {
    private String bookingId;
    private String roomId;
    private String userId;
    private String checkinDate;
    private String checkoutDate;
    private double price;
    private String paymentStatus;
    private String paymentMethod;
    private String paymentReceiptUrl;
    private long createdAt;
    private long updatedAt;

    // Constructor
    public Bill(String bookingId, String roomId, String userId, String checkinDate, String checkoutDate,
                double price, String paymentStatus, String paymentMethod, String paymentReceiptUrl,
                long createdAt, long updatedAt) {
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.userId = userId;
        this.checkinDate = checkinDate;
        this.checkoutDate = checkoutDate;
        this.price = price;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paymentReceiptUrl = paymentReceiptUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getuserId(){return userId != null ? userId : "";}
    public String getBookingId(){return bookingId != null ? bookingId : "";}
    public String getRoomId(){return roomId != null ? roomId : "";}
    public String getCheckinDate(){return checkinDate != null ? checkinDate : "";}
    public String getCheckoutDate(){return checkoutDate != null ? checkoutDate : "";}
    public double getPrice(){return price;}
    public String getPaymentStatus(){return paymentStatus != null ? paymentStatus : "";}
    public String getPaymentMethod(){return paymentMethod != null ? paymentMethod : "";}
    public String getPaymentReceiptUrl(){return paymentReceiptUrl != null ? paymentReceiptUrl : "";}
    public long getCreatedAt(){return createdAt;}
    public long getUpdatedAt(){return updatedAt;}

}

