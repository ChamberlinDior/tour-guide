package com.openclassrooms.tourguide.controller;

import java.util.List;

import com.openclassrooms.tourguide.model.NearbyAttraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gpsUtil.location.VisitedLocation;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;

import tripPricer.Provider;
//L'import NearbyAttraction qui est maintenant utilisée pour représenter les attractions touristiques proches.
@RestController
public class TourGuideController {

    // Injection de dépendance du service TourGuideService
	@Autowired
	TourGuideService tourGuideService;


    // Constructeur par défaut

    /**
     * Classe de contrôleur gérant les endpoints de l'API TourGuide.
     * Cette classe est responsable de l'exposition des fonctionnalités de l'application TourGuide
     * via des endpoints REST.
     */

    // Endpoint pour la racine de l'API
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    // Endpoint pour obtenir la position d'un utilisateur par son nom d'utilisateur
    @RequestMapping("/getLocation") 
    public VisitedLocation getLocation(@RequestParam String userName) {
    	return tourGuideService.getUserLocation(getUser(userName));
    }
    // Endpoint pour obtenir les attractions à proximité d'un utilisateur par son nom d'utilisateur
    @RequestMapping("/getNearbyAttractions") 
    public List<NearbyAttraction> getNearbyAttractions(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
    	return tourGuideService.getFiveNearestAttractions(visitedLocation, getUser(userName));
    }
    // Endpoint pour obtenir les récompenses d'un utilisateur par son nom d'utilisateur

    //Pour utiliser la classe NearbyAttraction qui est maintenant utilisée pour représenter les attractions touristiques proches.
    @RequestMapping("/getRewards") 
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return tourGuideService.getUserRewards(getUser(userName));
    }
    // Endpoint pour obtenir les offres de voyage pour un utilisateur par son nom d'utilisateur
    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
    	return tourGuideService.getTripDeals(getUser(userName));
    }
    // Méthode utilitaire pour obtenir un utilisateur par son nom d'utilisateur
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
   

}