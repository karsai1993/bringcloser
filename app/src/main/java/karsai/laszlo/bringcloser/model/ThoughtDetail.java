package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Class to create detailed object for thought
 */
public class ThoughtDetail implements Parcelable{

    private String fromUid;
    private String fromPhotoUrl;
    private String fromName;
    private String extraPhotoUrl;
    private String timestamp;
    private String text;

    public ThoughtDetail(
            String fromUid,
            String fromPhotoUrl,
            String fromName,
            String extraPhotoUrl,
            String timestamp,
            String text) {
        this.fromUid = fromUid;
        this.fromPhotoUrl = fromPhotoUrl;
        this.fromName = fromName;
        this.extraPhotoUrl = extraPhotoUrl;
        this.timestamp = timestamp;
        this.text = text;
    }

    protected ThoughtDetail(Parcel in) {
        fromUid = in.readString();
        fromPhotoUrl = in.readString();
        fromName = in.readString();
        extraPhotoUrl = in.readString();
        timestamp = in.readString();
        text = in.readString();
    }

    public static final Creator<ThoughtDetail> CREATOR = new Creator<ThoughtDetail>() {
        @Override
        public ThoughtDetail createFromParcel(Parcel in) {
            return new ThoughtDetail(in);
        }

        @Override
        public ThoughtDetail[] newArray(int size) {
            return new ThoughtDetail[size];
        }
    };

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getFromPhotoUrl() {
        return fromPhotoUrl;
    }

    public void setFromPhotoUrl(String fromPhotoUrl) {
        this.fromPhotoUrl = fromPhotoUrl;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getExtraPhotoUrl() {
        return extraPhotoUrl;
    }

    public void setExtraPhotoUrl(String extraPhotoUrl) {
        this.extraPhotoUrl = extraPhotoUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(fromUid);
        parcel.writeString(fromPhotoUrl);
        parcel.writeString(fromName);
        parcel.writeString(extraPhotoUrl);
        parcel.writeString(timestamp);
        parcel.writeString(text);
    }
}
