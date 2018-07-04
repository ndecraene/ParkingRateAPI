package com.spothero.api.model;

import lombok.Getter;

import java.util.List;

@Getter
public class ParkingRateListWrapper {

    private List<ParkingRate> rates;

    public ParkingRateListWrapper(List<ParkingRate> rates){
        this.rates = rates;
    }

    private ParkingRateListWrapper(){}
}
