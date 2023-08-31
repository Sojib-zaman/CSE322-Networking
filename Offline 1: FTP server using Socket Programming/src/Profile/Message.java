package Profile;

import java.util.Date;

public class Message {

    User senderID ;
    User receiverID;
    String text ;
    boolean unread ;
    String date ;
    public Message(User a, User b, String s , String date)
    {
        senderID = a ;
        receiverID = b ;
        text = s ;
        unread = true ;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public User getSenderID() {
        return senderID;
    }

    public void setSenderID(User senderID) {
        this.senderID = senderID;
    }

    public User getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(User receiverID) {
        this.receiverID = receiverID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getStatus() {
        return unread;
    }

    public void setStatus(boolean status) {
        this.unread = status;
    }
}

