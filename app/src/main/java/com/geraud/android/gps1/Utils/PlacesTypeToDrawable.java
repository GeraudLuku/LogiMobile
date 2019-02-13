package com.geraud.android.gps1.Utils;

import com.geraud.android.gps1.R;

import java.util.HashMap;
import java.util.Map;

// used to convert a place name/type to a corresponding icon drawable resource
public class PlacesTypeToDrawable {

    private static Map<String, Integer> placeToIcon = new HashMap<>();

    static {
        placeToIcon.put("Home", R.drawable.house);
        placeToIcon.put("Airport", R.drawable.airport);
        placeToIcon.put("Amusement Park", R.drawable.amusement);
        placeToIcon.put("Aquarium", R.drawable.aquarium);
        placeToIcon.put("Art Gallery", R.drawable.art_gallery);
        placeToIcon.put("ATM Vendor", R.drawable.atm);
        placeToIcon.put("Bakery", R.drawable.bakery);
        placeToIcon.put("Bank", R.drawable.bank_dollar);
        placeToIcon.put("Bar", R.drawable.bar);
        placeToIcon.put("Beauty", R.drawable.barber);
        placeToIcon.put("Beach", R.drawable.beach);
        placeToIcon.put("Bicycle Store", R.drawable.bicycle);
        placeToIcon.put("Book Store", R.drawable.book_shop);
        placeToIcon.put("Bowling Alley", R.drawable.bowling);
        placeToIcon.put("Bus Station", R.drawable.bus);
        placeToIcon.put("Cafe", R.drawable.cafe);
        placeToIcon.put("Campground", R.drawable.camping);
        placeToIcon.put("Car Dealer", R.drawable.car_dealer);
        placeToIcon.put("Car Rental", R.drawable.car_rental);
        placeToIcon.put("Car Repair", R.drawable.car_repair);
        placeToIcon.put("Car Wash", R.drawable.car_wash);
        placeToIcon.put("Casino", R.drawable.casino);
        placeToIcon.put("cemetery", R.drawable.Cemetery);
        placeToIcon.put("Church", R.drawable.church);
        placeToIcon.put("City Hall", R.drawable.civic_building);
        placeToIcon.put("Clothing Store", R.drawable.clothing_store);
        placeToIcon.put("Convenience Store", R.drawable.convenience);
        placeToIcon.put("Courthouse", R.drawable.courthouse);
        placeToIcon.put("Dentist", R.drawable.dentist);
        placeToIcon.put("Doctor", R.drawable.doctor);
        placeToIcon.put("Electrician", R.drawable.electrician);
        placeToIcon.put("Electronics Store", R.drawable.electronics);
        placeToIcon.put("Embassy", R.drawable.embassy);
        placeToIcon.put("Fire Station", R.drawable.fire_station);
        placeToIcon.put("Florist", R.drawable.flower);
        placeToIcon.put("Furniture Store", R.drawable.furniture_store);
        placeToIcon.put("Gas Station", R.drawable.gas_station);
        placeToIcon.put("Gym", R.drawable.fitness);
        placeToIcon.put("Hair Saloon", R.drawable.hair_saloon);
        placeToIcon.put("Hindu Temple", R.drawable.worship_hindu);
        placeToIcon.put("Hospital", R.drawable.hospital);
        placeToIcon.put("Insurance Agency", R.drawable.insurance_agency);
        placeToIcon.put("Jewerly Store", R.drawable.jewelry);
        placeToIcon.put("Laundry", R.drawable.laundry);
        placeToIcon.put("Lawyer", R.drawable.lawyer);
        placeToIcon.put("Library", R.drawable.library);
        placeToIcon.put("Local Government Office", R.drawable.government);
        placeToIcon.put("Lodging", R.drawable.lodging);
        placeToIcon.put("Mosque", R.drawable.mosque);
        placeToIcon.put("Movie Theater", R.drawable.movies);
        placeToIcon.put("Moving Company", R.drawable.moving_company);
        placeToIcon.put("Museum", R.drawable.museum);
        placeToIcon.put("Night Club", R.drawable.night_club);
        placeToIcon.put("Park", R.drawable.park);
        placeToIcon.put("Parking", R.drawable.parking);
        placeToIcon.put("Pharmacy", R.drawable.pharmacy);
        placeToIcon.put("Plumber", R.drawable.plumber);
        placeToIcon.put("Police", R.drawable.police);
        placeToIcon.put("Post Office", R.drawable.post_office);
        placeToIcon.put("Repairer", R.drawable.repair);
        placeToIcon.put("Restaurant", R.drawable.restaurant);
        placeToIcon.put("School", R.drawable.school);
        placeToIcon.put("Shoe Store", R.drawable.shoe_store);
        placeToIcon.put("Shopping Mall", R.drawable.shopping);
        placeToIcon.put("Spa", R.drawable.spa);
        placeToIcon.put("Stadium", R.drawable.stadium);
        placeToIcon.put("Store", R.drawable.store);
        placeToIcon.put("Subway Station", R.drawable.subway_station);
        placeToIcon.put("Supermarket", R.drawable.supermarket);
        placeToIcon.put("Taxi Stand", R.drawable.taxi);
        placeToIcon.put("Train Station", R.drawable.train);
        placeToIcon.put("Travel Agency", R.drawable.travel_agent);
        placeToIcon.put("University", R.drawable.university);
        placeToIcon.put("Veterinary Care", R.drawable.veterinary_clinic);
        placeToIcon.put("Zoo", R.drawable.zoo);
    }

    public static Integer getDrawable(String code) {
        return placeToIcon.get(code);
    }

    public static Map<String, Integer> getAll() {
        return placeToIcon;
    }
}
