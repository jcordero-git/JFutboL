package jfutbol.com.jfutbol.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by JOSEPH on 01/06/2015.
 */
public class mMatches {

    int id;
    int team1Id;
    String team1Name;
    int goalsTeam1;
    int team2Id;
    String team2Name;
    int goalsTeam2;
    String date;
    private int team1OwnerId;
    private int team1captainId;
    private String team1completeCaptainName;
    private int team2OwnerId;
    private int team2captainId;
    private String team2completeCaptainName;
    String startTime;
    String endTime;
    int soccerFieldId;
    String soccerFieldName;
    String soccerCenterName;
    int isReserved;
    int myTeamId;
    String myTeamName;
    int myTeamProvinceId;
    String myTeamProvinceName;
    int myTeamCantonId;
    String myTeamCantonName;


    public int getId() {
        return id;
    }

    public void setId(int matchId) {
        this.id = matchId;
    }

    public int getTeam1Id() {
        return team1Id;
    }

    public void setTeam1Id(int team1Id) {
        this.team1Id = team1Id;
    }

    public String getTeam1Name() {
        return team1Name;
    }

    public void setTeam1Name(String team1Name) {
        this.team1Name = team1Name;
    }

    public int getGoalsTeam1() {
        return goalsTeam1;
    }

    public void setGoalsTeam1(int goalsTeam1) {
        this.goalsTeam1 = goalsTeam1;
    }

    public int getTeam2Id() {
        return team2Id;
    }

    public void setTeam2Id(int team2Id) {
        this.team2Id = team2Id;
    }

    public String getTeam2Name() {
        return team2Name;
    }

    public void setTeam2Name(String team2Name) {
        this.team2Name = team2Name;
    }

    public int getGoalsTeam2() {
        return goalsTeam2;
    }

    public void setGoalsTeam2(int goalsTeam2) {
        this.goalsTeam2 = goalsTeam2;
    }

    public String getDate() {
        return date;
    }

    public String getDateApp(){
        SimpleDateFormat dateAppFormat = new SimpleDateFormat("E dd/MMMM/yyyy");
        SimpleDateFormat dateSQLFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date newdate=null;
        try {
            newdate = dateSQLFormat.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateAppFormat.format(newdate);
    }

    public void setDate(String date) {
        this.date=date;
    }

    public int getTeam1OwnerId() {
        return team1OwnerId;
    }

    public void setTeam1OwnerId(int team1OwnerId) {
        this.team1OwnerId = team1OwnerId;
    }

    public int getTeam2OwnerId() {
        return team2OwnerId;
    }

    public void setTeam2OwnerId(int team2OwnerId) {
        this.team2OwnerId = team2OwnerId;
    }

    public int getTeam1captainId() { return team1captainId; }

    public void setTeam1captainId(int team1captainId) { this.team1captainId = team1captainId; }

    public String getTeam1completeCaptainName() { return team1completeCaptainName; }

    public void setTeam1completeCaptainName(String team1completeCaptainName) { this.team1completeCaptainName = team1completeCaptainName; }

    public int getTeam2captainId() { return team2captainId; }

    public void setTeam2captainId(int team2captainId) { this.team2captainId = team2captainId; }

    public String getTeam2completeCaptainName() { return team2completeCaptainName; }

    public void setTeam2completeCaptainName(String team2completeCaptainName) { this.team2completeCaptainName = team2completeCaptainName; }

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

    public int getSoccerFieldId() { return soccerFieldId; }

    public void setSoccerFieldId(int soccerFieldId) { this.soccerFieldId = soccerFieldId; }

    public String getSoccerFieldName() {
        return soccerFieldName;
    }

    public void setSoccerFieldName(String soccerFieldName) { this.soccerFieldName = soccerFieldName; }

    public String getSoccerCenterName() {
        return soccerCenterName;
    }

    public void setSoccerCenterName(String soccerCenterName) { this.soccerCenterName = soccerCenterName; }

    public int getIsReserved() { return isReserved; }

    public void setIsReserved(int isReserved) { this.isReserved = isReserved; }

    public int getMyTeamId() { return myTeamId; }

    public void setMyTeamId(int myTeamId) { this.myTeamId = myTeamId; }

    public String getMyTeamName() { return myTeamName; }

    public void setMyTeamName(String myTeamName) { this.myTeamName = myTeamName; }

    public int getMyTeamProvinceId() { return myTeamProvinceId; }

    public void setMyTeamProvinceId(int myTeamProvinceId) { this.myTeamProvinceId = myTeamProvinceId; }

    public int getMyTeamCantonId() { return myTeamCantonId; }

    public void setMyTeamCantonId(int myTeamCantonId) { this.myTeamCantonId = myTeamCantonId; }

    public String getMyTeamProvinceName() { return myTeamProvinceName; }

    public void setMyTeamProvinceName(String myTeamProvinceName) { this.myTeamProvinceName = myTeamProvinceName; }

    public String getMyTeamCantonName() { return myTeamCantonName; }

    public void setMyTeamCantonName(String myTeamCantonName) { this.myTeamCantonName = myTeamCantonName; }
}
