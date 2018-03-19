package com.beachboxdeliveryapp.model;

/**
 * Created by bitware on 23/3/17.
 */

public class ModelHistoryOrder {

    public String resName;
    public String resDateTime;
    public String resPrice;
    public String resRating;
    public String resImage,orderId,orderStatus,orderCreatedAt;



    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public String getResDateTime() {
        return resDateTime;
    }

    public void setResDateTime(String resDateTime) {
        this.resDateTime = resDateTime;
    }

    public String getResPrice() {
        return resPrice;
    }

    public void setResPrice(String resPrice) {
        this.resPrice = resPrice;
    }

    public String getResRating() {
        return resRating;
    }

    public void setResRating(String resRating) {
        this.resRating = resRating;
    }

    public String getResImage() {
        return resImage;
    }

    public void setResImage(String resImage) {
        this.resImage = resImage;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderCreatedAt() {
        return orderCreatedAt;
    }

    public void setOrderCreatedAt(String orderCreatedAt) {
        this.orderCreatedAt = orderCreatedAt;
    }
}
