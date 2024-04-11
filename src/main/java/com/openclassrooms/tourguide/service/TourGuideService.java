package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.model.NearbyAttraction;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;

import tripPricer.Provider;
import tripPricer.TripPricer;
/**
 * Cette classe est responsable de la gestion des services de l'application TourGuide.
 * Elle offre des fonctionnalités telles que le suivi de la localisation des utilisateurs, le calcul des récompenses,
 * la recherche d'attractions à proximité, etc.
 */

//L'import com.openclassrooms.tourguide.model.NearbyAttraction a été ajouté.Pour utiliser la classe NearbyAttraction qui
// est maintenant utilisée pour représenter les attractions touristiques proches.
@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	private ExecutorService executorService = Executors.newFixedThreadPool(20);

	@PreDestroy
	public void shutdownExecutorService() {
		executorService.shutdown();
	}

	/**
	 * Constructeur de TourGuideService.
	 *
	 * @param gpsUtil        L'utilitaire GPS utilisé pour obtenir les localisations et attractions.
	 * @param rewardsService Le service de récompenses utilisé pour calculer les récompenses des utilisateurs.
	 */
	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		Locale.setDefault(Locale.US);

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	/**
	 * Méthode permettant d'obtenir les récompenses d'un utilisateur.
	 *
	 * @param user L'utilisateur pour lequel obtenir les récompenses.
	 * @return La liste des récompenses de l'utilisateur.
	 */
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}


	/**
	 * Méthode permettant d'obtenir la localisation d'un utilisateur.
	 *
	 * @param user L'utilisateur pour lequel obtenir la localisation.
	 * @return La localisation de l'utilisateur.
	 */
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user);
		return visitedLocation;
	}


	/**
	 * Méthode permettant d'obtenir un utilisateur par son nom d'utilisateur.
	 *
	 * @param userName Le nom d'utilisateur de l'utilisateur à récupérer.
	 * @return L'utilisateur correspondant au nom d'utilisateur spécifié.
	 */
	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}


	/**
	 * Méthode permettant d'obtenir tous les utilisateurs.
	 *
	 * @return La liste de tous les utilisateurs.
	 */
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	/**
	 * Méthode permettant d'obtenir les offres de voyage pour un utilisateur.
	 *
	 * @param user L'utilisateur pour lequel obtenir les offres de voyage.
	 * @return La liste des offres de voyage disponibles pour l'utilisateur.
	 */
	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}


	/**
	 * Méthode permettant de suivre de manière asynchrone la localisation d'un utilisateur.
	 *
	 * @param user L'utilisateur à suivre.
	 * @return Un CompletableFuture indiquant la fin du suivi de la localisation de l'utilisateur.
	 */
	public CompletableFuture<Void> trackUserLocationAsync(User user) {
		return CompletableFuture.runAsync(() -> trackUserLocation(user), executorService);
	}


	/**
	 * Méthode permettant de suivre la localisation d'un utilisateur.
	 *
	 * @param user L'utilisateur à suivre.
	 * @return La localisation de l'utilisateur.
	 */
	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}


	/**
	 * Méthode permettant d'obtenir les attractions à proximité d'une localisation visitée.
	 *
	 * @param visitedLocation La localisation visitée.
	 * @return La liste des attractions à proximité de la localisation visitée.
	 */
	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> nearbyAttractions = new ArrayList<>();
		for (Attraction attraction : gpsUtil.getAttractions()) {
			if (rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
		}

		return nearbyAttractions;
	}

	/**
	 * Méthode permettant d'obtenir les cinq attractions les plus proches d'une localisation visitée par un utilisateur.
	 *
	 * @param visitedLocation La localisation visitée par l'utilisateur.
	 * @param user            L'utilisateur.
	 * @return La liste des cinq attractions les plus proches.
	 */

	//La méthode getFiveNearestAttractions() a été ajoutée Pour répondre aux spécifications fonctionnelles qui
	// exigent que la méthode retourne les cinq attractions les plus proches
	public List<NearbyAttraction> getFiveNearestAttractions(VisitedLocation visitedLocation, User user){
		List<Attraction> allAttractions = gpsUtil.getAttractions();
		return allAttractions
				.stream()
				//sort the tourist attractions the nearest to the furthest
				.sorted(Comparator.comparingDouble( attraction -> rewardsService.getDistance(visitedLocation.location, attraction))
				)
				.map(attraction -> new NearbyAttraction(
						attraction.attractionName,
						attraction.latitude,
						attraction.longitude,
						visitedLocation.location.latitude,
						visitedLocation.location.longitude,
						rewardsService.getDistance(attraction, visitedLocation.location),
						rewardsService.getRewardPoints(attraction, user)
				))
				//take the first 5
				.limit(5)
				//stream back to list
				.collect(Collectors.toList());
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
					new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

}
