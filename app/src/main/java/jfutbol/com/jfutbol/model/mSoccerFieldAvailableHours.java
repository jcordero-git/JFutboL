package jfutbol.com.jfutbol.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by JOSEPH on 08/10/2015.
 */
public class mSoccerFieldAvailableHours {

    int soccerCenterdId;
    String soccerCenterName;
    int soccerFieldId;
    String soccerFieldName;
    String date;
    Calendar time;
    String startTime;
    String endTime;
    int isReserved;

    public int getSoccerCenterdId() {
        return soccerCenterdId;
    }

    public void setSoccerCenterdId(int soccerCenterdId) {
        this.soccerCenterdId = soccerCenterdId;
    }

    public String getSoccerCenterName() {
        return soccerCenterName;
    }

    public void setSoccerCenterName(String soccerCenterName) { this.soccerCenterName = soccerCenterName; }

    public int getSoccerFieldId() { return soccerFieldId; }

    public void setSoccerFieldId(int soccerFieldId) { this.soccerFieldId = soccerFieldId; }

    public String getSoccerFieldName() { return soccerFieldName; }

    public void setSoccerFieldName(String soccerFieldName) { this.soccerFieldName = soccerFieldName; }

    public String getDate() {
        return date;
    }

    /*
    public String getDateApp(){
        SimpleDateFormat dateAppFormat = new SimpleDateFormat("E dd/MMMM/yyyy");
        return dateAppFormat.format(date);
    }
    */
    public String getDateApp(){
        SimpleDateFormat dateAppFormat = new SimpleDateFormat("E dd/MMMM/yyyy");
        SimpleDateFormat dateSQLFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date newdate=null;
        try {
            newdate = dateSQLFormat.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateAppFormat.format(newdate);
    }

    public String getDateSQL(){
        SimpleDateFormat dateSQLFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateSQLFormat.format(date);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Calendar getTime() { return time; }

    public String getTimeApp(){
        SimpleDateFormat timeAppFormat = new SimpleDateFormat("hh:MM a");
        return timeAppFormat.format(time);
    }

    public String getTimeSQL(){
        SimpleDateFormat timeSQLFormat = new SimpleDateFormat("HH:MM");
        return timeSQLFormat.format(time);
    }

    public void setTime(Calendar time) { this.time = time; }

    public String getStartTime() {
        return startTime;
    }

    public String getStartTimeApp(){
        SimpleDateFormat timeAppFormat = new SimpleDateFormat("hh:mm a");
        java.util.Date date = new java.util.Date();
        String []time=startTime.split(":");
        date.setHours( Integer.parseInt(time[0]));
        date.setMinutes( Integer.parseInt(time[1]));
        return timeAppFormat.format(date);
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getEndTimeApp(){
        SimpleDateFormat timeAppFormat = new SimpleDateFormat("hh:mm a");
        java.util.Date date = new java.util.Date();
        String []time=endTime.split(":");
        date.setHours( Integer.parseInt(time[0]));
        date.setMinutes( Integer.parseInt(time[1]));
        return timeAppFormat.format(date);
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int isReserved() {
        return isReserved;
    }

    public void setReserved(int isReserved) {
        this.isReserved = isReserved;
    }
}
