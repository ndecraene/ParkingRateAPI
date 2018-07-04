package com.spothero.api.controller;


import com.spothero.api.model.ParkingRate;
import com.spothero.api.model.ParkingRateListWrapper;
import com.spothero.api.repository.ParkingRateRepository;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/rates")
public class ParkingRateController {

    private final ParkingRateRepository parkingRateRepository;

    @Autowired
    public ParkingRateController(ParkingRateRepository r){
        this.parkingRateRepository = r;
    }

    @GetMapping(params={"startTime", "endTime"})
    public ResponseEntity<Integer> calculateRate(@RequestParam String startTime, @RequestParam String endTime){
        DateTime start = new DateTime(startTime);
        DateTime end = new DateTime(endTime);

        String day = start.dayOfWeek().getAsText().substring(0, 3).toLowerCase();

        int rateStartTimeInSeconds = start.getSecondOfDay();
        int rateEndTimeInSeconds = end.getSecondOfDay();

        List<ParkingRate> rates = parkingRateRepository.findParkingRatesByDaysContains(day);
        String[] timeRange;
        for(ParkingRate rate: rates){
            timeRange = rate.getTimes().split("-");
            if(getSeconds(timeRange[0]) <rateStartTimeInSeconds && rateEndTimeInSeconds < getSeconds(timeRange[1])){
                return ResponseEntity.ok(rate.getPrice());
            }
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @PutMapping
    public ResponseEntity<?> putRates(@RequestBody ParkingRateListWrapper rates){
        parkingRateRepository.deleteAllInBatch();
        parkingRateRepository.saveAll(rates.getRates());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().build().toUri();

        return ResponseEntity.created(location).build();
    }

    private static int getSeconds(String time){
        int hour = Integer.parseInt(time.substring(0,2));
        int minute = Integer.parseInt(time.substring(2,4));
        return hour * 3600 + minute * 60;
    }
}
