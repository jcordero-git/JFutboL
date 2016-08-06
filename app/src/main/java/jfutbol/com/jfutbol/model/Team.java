package jfutbol.com.jfutbol.model;

/**
 * Created by JOSEPH on 22/04/2015.
 */
public class Team {

    private int teamId;
    private int ownerId;
    private int captainId;
    private String completeCaptainName;
    private String name;
    int provinceId;
    String provinceName;
    int cantonId;
    String cantonName;
    String encodedImage;
    int countPlayers;

    public Team(){}

    public Team(int teamId, int ownerId, String name){
        this.teamId = teamId;
        this.ownerId = ownerId;
        this.name = name;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getCaptainId() { return captainId; }

    public void setCaptainId(int captainId) { this.captainId = captainId; }

    public String getCompleteCaptainName() { return completeCaptainName; }

    public void setCompleteCaptainName(String completeCaptainName) { this.completeCaptainName = completeCaptainName; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProvinceId() { return provinceId; }

    public void setProvinceId(int provinceId) { this.provinceId = provinceId; }

    public String getProvinceName() { return provinceName; }

    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }

    public int getCantonId() {
        return cantonId;
    }

    public void setCantonId(int cantonId) {
        this.cantonId = cantonId;
    }

    public String getCantonName() {
        return cantonName;
    }

    public void setCantonName(String cantonName) {
        this.cantonName = cantonName;
    }

    public String getEncodedImage() { return encodedImage; }

    public void setEncodedImage(String encodedImage) { this.encodedImage = encodedImage; }

    public int getCountPlayers() {
        return countPlayers;
    }

    public void setCountPlayers(int countplayers) {
        this.countPlayers = countplayers;
    }
}
