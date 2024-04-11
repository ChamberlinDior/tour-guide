package com.openclassrooms.tourguide.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.service.RewardsService;

@Configuration
public class TourGuideModule {
	// Configuration d'un bean pour obtenir une instance de GpsUtil
	@Bean
	public GpsUtil getGpsUtil() {
		return new GpsUtil();
	}
	// Configuration d'un bean pour obtenir une instance de RewardsService en utilisant le bean GpsUtil et le bean RewardCentral
	@Bean
	public RewardsService getRewardsService() {
		return new RewardsService(getGpsUtil(), getRewardCentral());
	}
	// Configuration d'un bean pour obtenir une instance de RewardCentral
	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}
	
}
