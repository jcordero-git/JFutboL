package jfutbol.com.jfutbol.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by JOSEPH on 30/09/2015.
 */
public class mSoccerFields {
    int id;
    int soccerCenterId;
    String soccerCenterName;
    String name;
    String openTime;
    //Date openTimeFormat;
    String closeTime;
    String encodedImage;
    //Date closeTimeFormat;
    int status;

    public int getId() { return id; }

    public void setId(int soccerFieldId) { this.id = soccerFieldId; }

    public int getSoccerCenterId() {
        return soccerCenterId;
    }

    public String getSoccerCenterName() { return soccerCenterName; }

    public void setSoccerCenterName(String soccerCenterName) { this.soccerCenterName = soccerCenterName; }

    public void setSoccerCenterId(int soccerCenterId) {
        this.soccerCenterId = soccerCenterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpenTime() { return openTime; }

    public void setOpenTime(String openTime) { this.openTime = openTime; }

    public String getOpenTimeApp(){
        SimpleDateFormat timeAppFormat = new SimpleDateFormat("hh:mm a");
        Date date = new Date();
        String []time=openTime.split(":");
        date.setHours( Integer.parseInt(time[0]));
        date.setMinutes( Integer.parseInt(time[1]));
        return timeAppFormat.format(date);
    }

    public void setOpenTimeSQL(Date openTime) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        this.openTime = timeFormat.format(openTime);
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public String getCloseTimeApp(){
        SimpleDateFormat timeAppFormat = new SimpleDateFormat("hh:mm a");
        Date date = new Date();
        String []time=closeTime.split(":");
        date.setHours( Integer.parseInt(time[0]));
        date.setMinutes( Integer.parseInt(time[1]));
        return timeAppFormat.format(date);
    }

    public void setCloseTimeSQL(Date closeTime) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        this.closeTime = timeFormat.format(closeTime);
    }

    public int getStatus() { return status; }

    public void setStatus(int status) { this.status = status; }

    public String getEncodedImage() { return encodedImage; }

    public void setEncodedImage(String encodedImage) { this.encodedImage = encodedImage; }
}
