package jfutbol.com.jfutbol.model;

/**
 * Created by JOSEPH on 18/03/2015.
 */
public class PlayerSkills {

    String skillId;
    String skillName;
    Boolean value;
    int intValue;

    public PlayerSkills(String skillId, String skillName, int intValue) {
        this.skillId=skillId;
        this.skillName=skillName;
        this.intValue=intValue;

        if(intValue==1)
            value = true;
        if(intValue==0)
            value = false;
    }

    public String getSkillId() { return skillId; }

    public void setSkillId(String skillId) { this.skillId = skillId; }

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

    public void setSkill(String skillId, String skill, Boolean value) {
        this.skillId=skillId;
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
