
package com.beachboxdeliveryapp.model.notificationResponse;

import com.beachboxdeliveryapp.volly.BaseResponse;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Notification  extends BaseResponse {

    @SerializedName("notification_id")
    @Expose
    private Integer notificationId;
    @SerializedName("rs_type")
    @Expose
    private String rsType;
    @SerializedName("restaurant_image")
    @Expose
    private String restaurantImage;
    @SerializedName("notification")
    @Expose
    private String notification;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("notification_date")
    @Expose
    private String notificationDate;
    @SerializedName("order_id")
    @Expose
    private Integer orderId;
    @SerializedName("order_status")
    @Expose
    private String orderStatus;
    @SerializedName("order_type")
    @Expose
    private String orderType;

    public Integer getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }

    public String getRsType() {
        return rsType;
    }

    public void setRsType(String rsType) {
        this.rsType = rsType;
    }

    public String getRestaurantImage() {
        return restaurantImage;
    }

    public void setRestaurantImage(String restaurantImage) {
        this.restaurantImage = restaurantImage;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(String notificationDate) {
        this.notificationDate = notificationDate;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

}
