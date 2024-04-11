package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import gpsUtil.location.Attraction;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Service;
import gpsUtil.GpsUtil;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;
/**
 * Classe responsable de la gestion des récompenses pour les utilisateurs.
 * Elle calcule les récompenses pour les attractions visitées par les utilisateurs.
 * Elle peut effectuer ces calculs de manière asynchrone pour améliorer les performances.
 */
@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// Proximité en miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;

	private final GpsUtil gpsUtil;

	private final RewardCentral rewardsCentral;
	private ExecutorService executorService = Executors.newFixedThreadPool(50);

	/**
	 * Constructeur de RewardsService prenant en paramètres l'outil GPS (GpsUtil) et le centre de récompenses (RewardCentral).
	 *
	 * @param gpsUtil        L'outil GPS utilisé pour obtenir les attractions et calculer les distances.
	 * @param rewardCentral  Le centre de récompenses utilisé pour obtenir les points de récompense pour une attraction donnée.
	 */
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}
	@PreDestroy
	public void shutdownExecutorService() {
		executorService.shutdown();
	}
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}


	//La méthode calculateRewards() a été modifiée Pour améliorer les performances en utilisant un traitement
	// parallèle pour calculer les récompenses des utilisateurs.
	public void calculateRewards(User user) {
		List<VisitedLocation> userLocations = new ArrayList<>(user.getVisitedLocations());
		Set<String> userRewardAttractions = user.getUserRewards().stream()
						.map(UserReward::getAttractionName)
						.collect(Collectors.toSet());

		userLocations.parallelStream().forEach( userLocation ->
				//loop through all attractions
				gpsUtil.getAttractions().stream().forEach(attraction -> {
					//loop through all the user's rewards and check which are the ones he never got a reward for
					if (!userRewardAttractions.contains(attraction.attractionName)) {
						if (nearAttraction(userLocation, attraction)) {
							user.addUserReward(new UserReward(userLocation, attraction, getRewardPoints(attraction, user)));
						}
					}
				})
 );
	}

	public CompletableFuture<Void> calculateRewardsAsyncList(List<User> allUsers){
		List<User> leftList = allUsers.subList(0, allUsers.size()/2);
		List<User> rightList = allUsers.subList(allUsers.size()/2, allUsers.size());

		List<CompletableFuture<Void>> leftRewardsCalculation = leftList
				.stream()
			.map(u -> CompletableFuture.runAsync(() -> calculateRewards(u), executorService))
				.collect(Collectors.toList());
		List<CompletableFuture<Void>> rightRewardsCalculation = rightList
				.stream()
				.map(u -> CompletableFuture.runAsync(() -> calculateRewards(u), executorService))
				.collect(Collectors.toList());

		List<CompletableFuture<Void>> allRewardsCalculations = new ArrayList<>(leftRewardsCalculation);
		allRewardsCalculations.addAll(rightRewardsCalculation);

		return CompletableFuture.allOf(
				allRewardsCalculations.toArray(new CompletableFuture[0])
		);
	}

	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	public int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles; //(simplifier)remplace double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;return statuteMiles;
	}


}
