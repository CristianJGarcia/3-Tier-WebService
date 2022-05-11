package model;

public class AuditTrail {
    private String change_msg;
    private int changed_byID;
    private int person_ID;
    private String when_occurred;
    private String username;

    public AuditTrail(){

    }

    public AuditTrail(String msg, int byID, int toID, String time, String user){
        super();
        change_msg = msg;
        changed_byID = byID;
        person_ID = toID;
        when_occurred = time;
        username = user;
    }

    public String getChange_msg() {
        return change_msg;
    }

    public void setChange_msg(String change_msg) {
        this.change_msg = change_msg;
    }

    public int getChanged_byID() {
        return changed_byID;
    }

    public void setChanged_byID(int changed_byID) {
        this.changed_byID = changed_byID;
    }

    public int getPerson_ID() {
        return person_ID;
    }

    public void setPerson_ID(int person_ID) {
        this.person_ID = person_ID;
    }

    public String getWhen_occurred() {
        return when_occurred;
    }

    public void setWhen_occurred(String when_occurred) {
        this.when_occurred = when_occurred;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
