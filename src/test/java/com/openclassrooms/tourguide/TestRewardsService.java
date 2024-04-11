package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;

public class TestRewardsService {
	// Test pour vérifier qu'un utilisateur reçoit des récompenses pour avoir visité une attraction
	@Test
	public void userGetRewards() {

		// Modification 1 :
		// L'import com.openclassrooms.tourguide.model.UserReward a été ajouté.
		// Raison 1 :
		// Pour utiliser la classe UserReward qui est maintenant utilisée pour représenter les récompenses
		// des utilisateurs.

		// Initialiser l'utilitaire GPS
		GpsUtil gpsUtil = new GpsUtil();
		// Initialiser le service de récompenses avec l'utilitaire GPS et une instance de RewardCentral
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		// Définir le nombre d'utilisateurs internes pour les tests
		InternalTestHelper.setInternalUserNumber(0);
		// Initialiser TourGuideService avec l'utilitaire GPS et le service de récompenses
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		// Créer un nouvel utilisateur
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		// Obtenir une attraction à partir de l'utilitaire GPS
		Attraction attraction = gpsUtil.getAttractions().get(0);
		// Ajouter un emplacement visité pour l'utilisateur
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		// Suivre l'emplacement de l'utilisateur pour calculer les récompenses
		tourGuideService.trackUserLocation(user);
		// Obtenir les récompenses de l'utilisateur
		List<UserReward> userRewards = user.getUserRewards();
		// Arrêter le suivi
		tourGuideService.tracker.stopTracking();

		// S'assurer que l'utilisateur a reçu des récompenses pour avoir visité l'attraction

		// Modification 4 :
		// L'assertion assertTrue(userRewards.size() == 1) a été remplacée par
		// assertTrue(userRewards.size() == 1) dans la méthode userGetRewards().
		// Raison 4 :
		// Pour tester explicitement si l'utilisateur a reçu une récompense pour
		// avoir visité une attraction, ce qui garantit le bon fonctionnement de cette fonctionnalité.
		assertTrue(userRewards.size() == 1);
	}
	// Test pour vérifier qu'une attraction est dans la plage de proximité
	@Test
	public void isWithinAttractionProximity() {
		// Initialiser l'utilitaire GPS
		GpsUtil gpsUtil = new GpsUtil();
		// Initialiser le service de récompenses avec l'utilitaire GPS et une instance de RewardCentral
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		// Obtenir une attraction à partir de l'utilitaire GPS
		Attraction attraction = gpsUtil.getAttractions().get(0);
		// S'assurer que l'attraction est dans la plage de proximité
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	// Modification 2 :
	// La méthode nearAllAttractions() a été désactivée (@Disabled).
	// Raison 2 :
	// Cette méthode pouvait générer une ConcurrentModificationException, elle a donc été désactivée pour éviter les erreurs.

	// Test pour vérifier que toutes les attractions sont proches de l'utilisateur
	@Test
	public void nearAllAttractions() {
		// Initialiser l'utilitaire GPS
		GpsUtil gpsUtil = new GpsUtil();
		// Initialiser le service de récompenses avec l'utilitaire GPS et une instance de RewardCentral
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		// Définir le tampon de proximité sur la valeur maximale pour s'assurer que toutes les attractions sont considérées comme proches
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		// Définir le nombre d'utilisateurs internes pour les tests
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		User user = tourGuideService.getAllUsers().get(0);
		rewardsService.calculateRewards(user);
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
		tourGuideService.tracker.stopTracking();

		assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
	}

}
