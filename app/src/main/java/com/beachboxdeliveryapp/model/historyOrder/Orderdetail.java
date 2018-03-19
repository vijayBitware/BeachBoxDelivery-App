
package com.beachboxdeliveryapp.model.historyOrder;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Orderdetail {

    @SerializedName("customer_name")
    @Expose
    private String customerName;
    @SerializedName("rating")
    @Expose
    private String rating;
    @SerializedName("rs_pic")
    @Expose
    private String rsPic;
    @SerializedName("rs_name")
    @Expose
    private String rsName;
    @SerializedName("ordered_date")
    @Expose
    private String orderedDate;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("price")
    @Expose
    private Double price;
    @SerializedName("order_id")
    @Expose
    private Integer orderId;
    @SerializedName("restaurant_id")
    @Expose
    private Integer restaurantId;
    @SerializedName("rs_type")
    @Expose
    private String rsType;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getRsPic() {
        return rsPic;
    }

    public void setRsPic(String rsPic) {
        this.rsPic = rsPic;
    }

    public String getRsName() {
        return rsName;
    }

    public void setRsName(String rsName) {
        this.rsName = rsName;
    }

    public String getOrderedDate() {
        return orderedDate;
    }

    public void setOrderedDate(String orderedDate) {
        this.orderedDate = orderedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRsType() {
        return rsType;
    }

    public void setRsType(String rsType) {
        this.rsType = rsType;
    }

}
