package com.spothero.api.repository;


import com.spothero.api.model.ParkingRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkingRateRepository extends JpaRepository<ParkingRate, Long> {
    List<ParkingRate> findParkingRatesByDaysContains(String day);
}
