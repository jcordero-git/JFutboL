package jfutbol.com.jfutbol.model;





import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by JOSEPH on 17/03/2015.
 */
public class User {

    private int id;
    String email;
    String password;
    String firstName;
    String lastName;
    String age;
    String phone;
    Date birthday;
    PlayerSkills [] skills;
    String position;
    String encodedImage;
    String activationCode;
    Integer requestStatus;
    Integer userType;
    int provinceId;
    String provinceName;
    int cantonId;
    String cantonName;
    int extraPlayer;

    public User(){ }

    public Integer getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }

    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }

    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public Date getBirthday() { return birthday; }

    public String getBirthdayApp(){
        SimpleDateFormat dateAppFormat = new SimpleDateFormat("E dd/MMMM/yyyy");
        return dateAppFormat.format(this.birthday);
    }

    public void setBirthday(String birthday) { this.birthday  = java.sql.Date.valueOf( birthday ); }

    public PlayerSkills[] getSkills() { return skills; }

    public String[] getSkillsString() {
        String []toReturn = new String[skills.length];
        for (int i=0;i<skills.length;i++)
        {
            toReturn[i]=skills[i].getSkillName();
        }
        return toReturn;
    }

    public String getSkillName(int index) { return skills[index].getId(); }

    public boolean verifySkillName(ArrayList<String> skill) {
         boolean status=false;
            for(int k=0;k<skill.size();k++) {
                for (int i = 0; i < skills.length; i++) {
                    if (skills[i].getSkillName().contains(skill.get(k))) {
                        status = true;
                    }
                }
            }
         return status;
        }

    public Boolean getSkillValue(int index) { return skills[index].getValue(); }

    public void setSkills(PlayerSkills[] skills) { this.skills = skills; }

    public String getAge() { return age; }

    public void setAge(String age) { this.age = age; }

    public String getPosition() { return position; }

    public void setPosition(String position) { this.position = position; }

    public String getEncodedImage() { return encodedImage; }

    public void setEncodedImage(String encodedImage) { this.encodedImage = encodedImage; }

    public String getActivationCode() { return activationCode; }

    public void setActivationCode(String activationCode) { this.activationCode = activationCode; }

    public Integer getRequestStatus() { return requestStatus; }

    public void setRequestStatus(Integer requestStatus) { this.requestStatus = requestStatus; }

    public Integer getUserType() { return userType; }

    public void setUserType(Integer userType) { this.userType = userType; }

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

    public int getExtraPlayer() { return extraPlayer; }

    public void setExtraPlayer(int extraPlayer) { this.extraPlayer = extraPlayer; }
}
