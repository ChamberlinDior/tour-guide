package com.openclassrooms.tourguide.helper;


/**
 * Classe utilitaire pour les tests internes.
 * Cette classe permet de définir et d'accéder au nombre d'utilisateurs internes utilisé pour les tests.
 */

public class InternalTestHelper {

	// Définir ce nombre par défaut jusqu'à 100 000 pour les tests
	private static int internalUserNumber = 100;


	/**
	 * Définit le nombre d'utilisateurs internes utilisé pour les tests.
	 *
	 * @param internalUserNumber Le nombre d'utilisateurs internes
	 */
	public static void setInternalUserNumber(int internalUserNumber) {
		InternalTestHelper.internalUserNumber = internalUserNumber;
	}

	/**
	 * Obtient le nombre d'utilisateurs internes utilisé pour les tests.
	 *
	 * @return Le nombre d'utilisateurs internes
	 */
	public static int getInternalUserNumber() {
		return internalUserNumber;
	}
}
