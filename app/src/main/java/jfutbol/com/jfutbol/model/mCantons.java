package jfutbol.com.jfutbol.model;

/**
 * Created by JOSEPH on 01/10/2015.
 */
public class mCantons {

    int id;
    int provinceId;
    String name;

    public int getId() {
        return id;
    }

    public void setId(int cantonId) {
        this.id = cantonId;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
