package com.hillel.items_exchange.service;

import com.hillel.items_exchange.dao.LocationRepository;
import com.hillel.items_exchange.model.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

    public Optional<Location> findById(long id) {
        return locationRepository.findById(id);
    }

    public Location createLocation(Location location) {
        return locationRepository.save(location);
    }
}
