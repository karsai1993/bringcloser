package karsai.laszlo.bringcloser.model;

/**
 * Created by Laci on 07/07/2018.
 */

public class ChatDetail {
    private MessageDetail messageDetail;
    private String date;

    public ChatDetail(MessageDetail messageDetail) {
        this.messageDetail = messageDetail;
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
