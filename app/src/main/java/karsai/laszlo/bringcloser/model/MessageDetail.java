package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Laci on 03/07/2018.
 * Class to create detailed object for message
 */
public class MessageDetail implements Parcelable{

    private String from;
    private String fromPhotoUrl;
    private String text;
    private String photoUrl;
    private String timestamp;

    public MessageDetail(
            String from,
            String fromPhotoUrl,
            String text,
            String photoUrl,
            String timestamp) {
        this.from = from;
        this.fromPhotoUrl = fromPhotoUrl;
        this.text = text;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
    }

    protected MessageDetail(Parcel in) {
        from = in.readString();
        fromPhotoUrl = in.readString();
        text = in.readString();
        photoUrl = in.readString();
        timestamp = in.readString();
    }

    public static final Creator<MessageDetail> CREATOR = new Creator<MessageDetail>() {
        @Override
        public MessageDetail createFromParcel(Parcel in) {
            return new MessageDetail(in);
        }

        @Override
        public MessageDetail[] newArray(int size) {
            return new MessageDetail[size];
        }
    };

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFromPhotoUrl() {
        return fromPhotoUrl;
    }

    public void setFromPhotoUrl(String fromPhotoUrl) {
        this.fromPhotoUrl = fromPhotoUrl;
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
        parcel.writeString(fromPhotoUrl);
        parcel.writeString(text);
        parcel.writeString(photoUrl);
        parcel.writeString(timestamp);
    }
}
