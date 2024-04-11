package com.openclassrooms.tourguide.model;

/**
 * Classe représentant une attraction touristique proche.
 * Cette classe contient les informations sur une attraction touristique à proximité, y compris son nom,
 * sa latitude et sa longitude, ainsi que la latitude et la longitude de l'utilisateur, la distance entre
 * l'utilisateur et l'attraction, et les points de récompense pour avoir visité l'attraction.
 */

public class NearbyAttraction {
    // Nom de l'attraction touristique,
    private String name;
    // Latitude/Longitude de l'attraction touristique
    private double attractionLatitude;
    private double attractionLongitude;

    // Latitude/Longitude de l'utilisateur
    private double userLatitude;

    // Distance en miles entre la position de l'utilisateur et chaque attraction
    private double userLongitude;
    // Distance en miles entre la position de l'utilisateur et chaque attraction
    private double distance;
    // Points de récompense pour la visite de l'attraction
    private int rewards;
    //Constructeur de la classe NearbyAttraction.
    public NearbyAttraction(String name,
                            double attractionLatitude,
                            double attractionLongitude,
                            double userLatitude,
                            double userLongitude,
                            double distance,
                            int rewards) {
        this.name = name;
        this.attractionLatitude = attractionLatitude;
        this.attractionLongitude = attractionLongitude;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.distance = distance;
        this.rewards = rewards;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAttractionLatitude() {
        return attractionLatitude;
    }

    public void setAttractionLatitude(double attractionLatitude) {
        this.attractionLatitude = attractionLatitude;
    }

    public double getAttractionLongitude() {
        return attractionLongitude;
    }

    public void setAttractionLongitude(double attractionLongitude) {
        this.attractionLongitude = attractionLongitude;
    }

    public double getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(double userLatitude) {
        this.userLatitude = userLatitude;
    }

    public double getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(double userLongitude) {
        this.userLongitude = userLongitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getRewards() {
        return rewards;
    }

    public void setRewards(int rewards) {
        this.rewards = rewards;
    }
}
