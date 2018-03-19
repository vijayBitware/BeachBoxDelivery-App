
package com.beachboxdeliveryapp.model.upcomingOrder;

import java.util.List;

import com.beachboxdeliveryapp.volly.BaseResponse;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ModelVUpcomingOrder extends BaseResponse {

    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("is_success")
    @Expose
    private Boolean isSuccess;
    @SerializedName("upcomingorders")
    @Expose
    private List<Upcomingorder> upcomingorders = null;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public List<Upcomingorder> getUpcomingorders() {
        return upcomingorders;
    }

    public void setUpcomingorders(List<Upcomingorder> upcomingorders) {
        this.upcomingorders = upcomingorders;
    }

}
