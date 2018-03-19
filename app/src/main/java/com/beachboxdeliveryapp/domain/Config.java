package com.beachboxdeliveryapp.domain;

/**
 * This class for local storage of constants. This contains all web service url.
 * Created by bitware on 10/7/17.
 */

public class Config {
   /* public static String BASE_URL ="http://beachbox.dotcomweavers.net/api/deliveryuser/";
    public static String BASE_URL1 ="http://beachbox.dotcomweavers.net/api/user/";
    public static String BASE_URL_COMMON = "http://beachbox.dotcomweavers.net/api/";
*/
    public static String BASE_URL ="https://app.beachboxenterprise.com/api/";
    //public static String BASE_URL = "http://beachbox.dotcomweavers.net/api/";
    public static String PHOTO_UPLOAD_URL = "http://app.beachboxenterprise.com/api/";

    public static String access_token = "dy1Wq-gJuCuAPfBa7dZsk-EK";
    public static String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    public static String PHONE_REGEX = "^\\([0-9]{3}\\)[0-9]{3}-[0-9]{4}$";
    public static String GET = "GET";
    public static String POST = "POST";
    public static final int API_UPCOMING_ORDER = 100;
    public static final int API_ORDER_HISTORY = 101;
    public static final int API_GET_ALLNOTIFICATION = 102;
 public static final int API_LOGOUT = 103;

 public static String DEVICE_TYPE = "android";
    public static String notiFlag = "No";
 public static String editorFlag = "No";
    public static String isFROM = "Orders";

}
