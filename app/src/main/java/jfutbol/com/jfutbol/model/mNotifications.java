package jfutbol.com.jfutbol.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by JOSEPH on 30/05/2015.
 */
public class mNotifications {

    private int id;
    String shortNotification;
    String notification;
    int userId;
    String date;
    int status;
    int type;
    int keyId;
    int img;

    public int getId() {
        return id;
    }

    public void setId(int notificationsId) {
        this.id = notificationsId;
    }

    public String getShortNotification() { return shortNotification; }

    public void setShortNotification(String shortNotification) { this.shortNotification = shortNotification; }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public String getDateApp(){
        SimpleDateFormat dateAppFormat = new SimpleDateFormat("E dd/MMMM/yyyy h:mm a");
        SimpleDateFormat dateSQLFormat = new SimpleDateFormat("yyyy-MM-dd h:mm:ss");
        Date newdate=null;
        try {
            newdate = dateSQLFormat.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateAppFormat.format(newdate);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() { return type; }

    public void setType(int type) { this.type = type; }

    public int getKeyId() { return keyId;}

    public void setKeyId(int keyId) { this.keyId = keyId; }

    public int getImg() { return img; }

    public void setImg(int img) { this.img = img; }
}
