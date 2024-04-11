package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.model.User;
/**
 * Cette classe est responsable du suivi continu de la localisation des utilisateurs.
 */
public class Tracker extends Thread {
	private Logger logger = LoggerFactory.getLogger(Tracker.class);
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final TourGuideService tourGuideService;
	private boolean stop = false;


	/**
	 * Constructeur de Tracker prenant en paramètre le service TourGuide.
	 *
	 * @param tourGuideService Le service TourGuide utilisé pour effectuer le suivi de la localisation des utilisateurs.
	 */
	public Tracker(TourGuideService tourGuideService) {
		this.tourGuideService = tourGuideService;

		executorService.submit(this);
	}

	/**
	 * Méthode permettant d'arrêter le suivi.
	 */
	public void stopTracking() {
		stop = true;
		executorService.shutdownNow();
	}

	@Override
	public void run() {
		StopWatch stopWatch = new StopWatch(); // Crée un chronomètre pour mesurer le temps d'exécution
		while (true) { // Boucle infinie pour exécuter le suivi en continu
			if (Thread.currentThread().isInterrupted() || stop) { // Vérifie si le thread a été interrompu ou si le suivi doit s'arrêter
				logger.debug("Tracker stopping"); // Journalise l'arrêt du suivi
				break; // Sort de la boucle
			}

			List<User> users = tourGuideService.getAllUsers(); // Récupère la liste de tous les utilisateurs
			logger.debug("Begin Tracker. Tracking " + users.size() + " users."); // Journalise le début du suivi avec le nombre d'utilisateurs
			stopWatch.start(); // Démarre le chronomètre
			users.forEach(u -> tourGuideService.trackUserLocation(u)); // Parcourt chaque utilisateur et suit sa localisation
			stopWatch.stop(); // Arrête le chronomètre
			logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); // Journalise le temps écoulé pour le suivi
			stopWatch.reset(); // Réinitialise le chronomètre pour la prochaine itération
			try {
				logger.debug("Tracker sleeping"); // Journalise que le suivi est en pause
				TimeUnit.SECONDS.sleep(trackingPollingInterval); // Met le thread en pause pendant un intervalle de temps
			} catch (InterruptedException e) { // Gère les interruptions du sommeil
				break; // Sort de la boucle
			}
		}
	}

}
