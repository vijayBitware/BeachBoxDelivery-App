package com.beachboxdeliveryapp.model;

/**
 * Created by bitware on 22/3/17.
 */

public class ModelRestaurantList {

    public String restaurantName;
    public String restaurantDes;
    public String restaurantRating;
    public String restaurantImageUrl;
    public int restaurantSampleImage;
    public String dataType;

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantDes() {
        return restaurantDes;
    }

    public void setRestaurantDes(String restaurantDes) {
        this.restaurantDes = restaurantDes;
    }

    public String getRestaurantRating() {
        return restaurantRating;
    }

    public void setRestaurantRating(String restaurantRating) {
        this.restaurantRating = restaurantRating;
    }

    public String getRestaurantImageUrl() {
        return restaurantImageUrl;
    }

    public void setRestaurantImageUrl(String restaurantImageUrl) {
        this.restaurantImageUrl = restaurantImageUrl;
    }

    public int getRestaurantSampleImage() {
        return restaurantSampleImage;
    }

    public void setRestaurantSampleImage(int restaurantSampleImage) {
        this.restaurantSampleImage = restaurantSampleImage;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
