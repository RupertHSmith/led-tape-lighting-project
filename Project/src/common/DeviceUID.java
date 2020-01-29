package common;

import java.io.Serializable;

public class DeviceUID implements Serializable {
    private String uid;
    private String description;

    public DeviceUID(String uid, String description) throws InvalidUIDException {
        if (uid.length() <= 10){
            throw new InvalidUIDException("UID too short");
        } else {
            this.uid = uid;
            this.description = description;
        }
    }

    /**
     * Return the UID
     * @return
     */
    public String getUid() {
        return uid;
    }

    /**
     * Return the description
     * @return
     */
    public String getDescription() {
        return description;
    }

    class InvalidUIDException extends Exception{
        public InvalidUIDException (String error){
            super("INVALID UID: " + error);
        }
    }
}
