package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Laci on 02/07/2018.
 */

public class Message implements Parcelable{

    private String from;
    private String text;
    private String photoUrl;
    private String timestamp;

    public Message(String from, String text, String photoUrl, String timestamp) {
        this.from = from;
        this.text = text;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
    }

    public Message() {}

    protected Message(Parcel in) {
        from = in.readString();
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
        parcel.writeString(from);
        parcel.writeString(text);
        parcel.writeString(photoUrl);
        parcel.writeString(timestamp);
    }
}
