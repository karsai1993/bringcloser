package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Laci on 02/07/2018.
 * Class to create object for message
 */

public class Message implements Parcelable{

    private String connectionFromUid;
    private String connectionToUid;
    private String from;
    private String to;
    private String text;
    private String photoUrl;
    private String timestamp;

    public Message(
            String connectionFromUid,
            String connectionToUid,
            String from,
            String to,
            String text,
            String photoUrl,
            String timestamp) {
        this.connectionFromUid = connectionFromUid;
        this.connectionToUid = connectionToUid;
        this.from = from;
        this.to = to;
        this.text = text;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
    }

    public Message() {}

    protected Message(Parcel in) {
        connectionFromUid = in.readString();
        connectionToUid = in.readString();
        from = in.readString();
        to = in.readString();
        text = in.readString();
        photoUrl = in.readString();
        timestamp = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getConnectionFromUid() {
        return connectionFromUid;
    }

    public void setConnectionFromUid(String connectionFromUid) {
        this.connectionFromUid = connectionFromUid;
    }

    public String getConnectionToUid() {
        return connectionToUid;
    }

    public void setConnectionToUid(String connectionToUid) {
        this.connectionToUid = connectionToUid;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(connectionFromUid);
        parcel.writeString(connectionToUid);
        parcel.writeString(from);
        parcel.writeString(to);
        parcel.writeString(text);
        parcel.writeString(photoUrl);
        parcel.writeString(timestamp);
    }
}
