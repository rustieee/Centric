package com.finals.centric;

public class userData {
    private String username, phone, firstname, lastname, birthdate, address, id;

    // Default constructor for Firebase to map the data
    public userData() {
        // Firebase requires an empty constructor to map the data
    }

    // Constructor to initialize from Firebase
    public userData(String username, String phone, String firstname, String lastname,
                    String birthdate, String address, String id) {
        this.username = username;
        this.phone = phone;
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthdate = birthdate;
        this.address = address;
        this.id = id;
    }

    // Getters
    public String getUsername() { return username; }
    public String getPhone() { return phone; }
    public String getFirstname() { return firstname; }
    public String getLastname() { return lastname; }
    public String getBirthdate() { return birthdate; }
    public String getAddress() { return address; }
    public String getId() { return id; }

    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }
    public void setAddress(String address) { this.address = address; }
    public void setId(String id) { this.id = id; }
}
