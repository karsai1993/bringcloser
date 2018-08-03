package karsai.laszlo.bringcloser.model;

/**
 * Created by Laci on 07/07/2018.
 * Class to create detailed object for chat
 */
public class ChatDetail {
    private MessageDetail messageDetail;
    private String date;
    private String time;

    public ChatDetail(MessageDetail messageDetail, String time) {
        this.messageDetail = messageDetail;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ChatDetail(String date) {
        this.date = date;
    }

    public MessageDetail getMessageDetail() {
        return messageDetail;
    }

    public void setMessageDetail(MessageDetail messageDetail) {
        this.messageDetail = messageDetail;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
