package com.spothero.api.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
@Entity
public class ParkingRate {

    @Id
    @GeneratedValue
    private Long id;

    private String days;
    private String times;
    private int price;

    public ParkingRate(String days, String times, int price){
        this.days = days;
        this.price = price;
        this.times = times;
    }

    private ParkingRate(){}
}
