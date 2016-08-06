package jfutbol.com.jfutbol.model;

/**
 * Created by JOSEPH on 30/09/2015.
 */
public class mSoccerCenters {

    int soccerCenterId;
    int ownerId;
    String name;
    String address;
    String phone;
    String email;
    String openTime;
    String closeTime;
    int provinceId;
    String provinceName;
    int cantonId;
    String cantonName;
    String encodedImage;

    public int getSoccerCenterId() {
        return soccerCenterId;
    }

    public void setSoccerCenterId(int soccerCenterId) {
        this.soccerCenterId = soccerCenterId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public int getProvinceId() { return provinceId; }

    public void setProvinceId(int provinceId) { this.provinceId = provinceId; }

    public String getProvinceName() { return provinceName; }

    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }

    public int getCantonId() { return cantonId; }

    public void setCantonId(int cantonId) { this.cantonId = cantonId; }

    public String getCantonName() { return cantonName; }

    public void setCantonName(String cantonName) { this.cantonName = cantonName; }

    public String getEncodedImage() { return encodedImage; }

    public void setEncodedImage(String encodedImage) { this.encodedImage = encodedImage; }
}
