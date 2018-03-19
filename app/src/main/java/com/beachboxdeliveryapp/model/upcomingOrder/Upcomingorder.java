
package com.beachboxdeliveryapp.model.upcomingOrder;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Upcomingorder {

    @SerializedName("restaurant_id")
    @Expose
    private Integer restaurantId;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("restaurant_image")
    @Expose
    private String restaurantImage;
    @SerializedName("created_user_id")
    @Expose
    private Integer createdUserId;
    @SerializedName("delivery_user_id")
    @Expose
    private Integer deliveryUserId;
    @SerializedName("rs_name")
    @Expose
    private String rsName;
    @SerializedName("rs_description")
    @Expose
    private Object rsDescription;
    @SerializedName("tip_amount")
    @Expose
    private Integer tipAmount;
    @SerializedName("total_amount")
    @Expose
    private Float totalAmount;
    @SerializedName("sub_total")
    @Expose
    private Integer subTotal;
    @SerializedName("sales_tax")
    @Expose
    private Float salesTax;
    @SerializedName("user_lat")
    @Expose
    private Float userLat;
    @SerializedName("user_lng")
    @Expose
    private Float userLng;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("order_type")
    @Expose
    private String orderType;
    @SerializedName("b_address1")
    @Expose
    private Object bAddress1;
    @SerializedName("b_city")
    @Expose
    private Object bCity;
    @SerializedName("b_address2")
    @Expose
    private Object bAddress2;
    @SerializedName("b_state")
    @Expose
    private Object bState;
    @SerializedName("b_country")
    @Expose
    private Object bCountry;
    @SerializedName("b_zipcode")
    @Expose
    private Object bZipcode;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("rs_type")
    @Expose
    private String rsType;
    @SerializedName("rating")
    @Expose
    private Integer rating;

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRestaurantImage() {
        return restaurantImage;
    }

    public void setRestaurantImage(String restaurantImage) {
        this.restaurantImage = restaurantImage;
    }

    public Integer getCreatedUserId() {
        return createdUserId;
    }

    public void setCreatedUserId(Integer createdUserId) {
        this.createdUserId = createdUserId;
    }

    public Integer getDeliveryUserId() {
        return deliveryUserId;
    }

    public void setDeliveryUserId(Integer deliveryUserId) {
        this.deliveryUserId = deliveryUserId;
    }

    public String getRsName() {
        return rsName;
    }

    public void setRsName(String rsName) {
        this.rsName = rsName;
    }

    public Object getRsDescription() {
        return rsDescription;
    }

    public void setRsDescription(Object rsDescription) {
        this.rsDescription = rsDescription;
    }

    public Integer getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(Integer tipAmount) {
        this.tipAmount = tipAmount;
    }

    public Float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(Integer subTotal) {
        this.subTotal = subTotal;
    }

    public Float getSalesTax() {
        return salesTax;
    }

    public void setSalesTax(Float salesTax) {
        this.salesTax = salesTax;
    }

    public Float getUserLat() {
        return userLat;
    }

    public void setUserLat(Float userLat) {
        this.userLat = userLat;
    }

    public Float getUserLng() {
        return userLng;
    }

    public void setUserLng(Float userLng) {
        this.userLng = userLng;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Object getBAddress1() {
        return bAddress1;
    }

    public void setBAddress1(Object bAddress1) {
        this.bAddress1 = bAddress1;
    }

    public Object getBCity() {
        return bCity;
    }

    public void setBCity(Object bCity) {
        this.bCity = bCity;
    }

    public Object getBAddress2() {
        return bAddress2;
    }

    public void setBAddress2(Object bAddress2) {
        this.bAddress2 = bAddress2;
    }

    public Object getBState() {
        return bState;
    }

    public void setBState(Object bState) {
        this.bState = bState;
    }

    public Object getBCountry() {
        return bCountry;
    }

    public void setBCountry(Object bCountry) {
        this.bCountry = bCountry;
    }

    public Object getBZipcode() {
        return bZipcode;
    }

    public void setBZipcode(Object bZipcode) {
        this.bZipcode = bZipcode;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getRsType() {
        return rsType;
    }

    public void setRsType(String rsType) {
        this.rsType = rsType;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

}
