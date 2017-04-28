package jfutbol.com.jfutbol.model;

/**
 * Created by JOSEPH on 18/03/2015.
 */
public class PlayerSkills {

    String id;
    String skillName;
    Boolean value;
    int intValue;

    public PlayerSkills(String id, String skillName, int intValue) {
        this.id=id;
        this.skillName=skillName;
        this.intValue=intValue;

        if(intValue==1)
            value = true;
        if(intValue==0)
            value = false;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getSkillName() { return skillName; }

    public void setSkillName(String skillName) { this.skillName = skillName; }

    public Boolean getValue() {
    if(intValue==1)
        value = true;
    if(intValue==0)
        value = false;
    return value;
    }

    public void setValue(int intValue) {
        if(intValue==1)
            value = true;
        if(intValue==0)
            value = false;

        this.intValue=intValue;
    }

    public void setSkill(String id, String skill, Boolean value) {
        this.id=id;
        this.value = value;
        if(value)
            intValue=1;
        else{
            intValue=0;
            }
    }

    public int getIntValue() {
        return intValue;
    }

}
