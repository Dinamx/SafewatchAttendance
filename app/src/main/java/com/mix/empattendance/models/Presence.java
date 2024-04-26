package com.mix.empattendance.models;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Presence {
    int id;
    int idemploye;
    String image ;
    String type_attendance;
    double latitude;
    double longitude;
    Timestamp dateheure;
    String feedback;

    public String getFeedback() {
        return feedback != null ? feedback : "";
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Presence() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdemploye() {
        return idemploye;
    }

    public void setIdemploye(int idemploye) {
        this.idemploye = idemploye;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType_attendance() {
        return type_attendance;
    }

    public void setType_attendance(String type_attendance) {
        this.type_attendance = type_attendance;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Timestamp getDateheure() {
        System.out.println("dateheure  is " + dateheure);
        return dateheure;
    }
    public String getDateheureFormated() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(new Date(dateheure.getTime()));
        System.out.println("dateheure is " + formattedDate);
        return formattedDate;
    }

    public void setDateheure(Timestamp dateheure) {
        this.dateheure = dateheure;
    }


    public void print() {
//        System.out.println("ID: " + id);
        System.out.println("ID Employe: " + idemploye);
        System.out.println("Image: " + image);
        System.out.println("Type d'attendance: " + type_attendance);
        System.out.println("Latitude: " + latitude);
        System.out.println("Longitude: " + longitude);
        System.out.println("Date et Heure: " + dateheure);
    }


}
