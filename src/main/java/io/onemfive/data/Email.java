package io.onemfive.data;

import java.io.Serializable;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class Email implements Persistable, Serializable {

    public static String MIMETYPE_TEXT_PLAIN = "text/plain";

    private Long id;
    private DID toDID;
    private DID fromDID;
    private String subject;
    private String message;
    private String messageType = MIMETYPE_TEXT_PLAIN;
    private int flag = 0;

    public Email() {
    }

    public Email(DID toDID, String subject, String message) {
        // Anonymous Message
        this.toDID = toDID;
        this.subject = subject;
        this.message = message;
    }

    public Email(DID toDID, DID fromDID, String subject, String message) {
        this.toDID = toDID;
        this.fromDID = fromDID;
        this.subject = subject;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DID getToDID() {
        return toDID;
    }

    public void setToDID(DID toDID) {
        this.toDID = toDID;
    }

    public DID getFromDID() {
        return fromDID;
    }

    public void setFromDID(DID fromDID) {
        this.fromDID = fromDID;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
