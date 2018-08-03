package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Class to create object for thought
 */
public class Thought implements Parcelable, Comparator<Thought> {

    String fromUid;
    String connectionFromUid;
    String connectionToUid;
    String extraPhotoUrl;
    String timestamp;
    String text;
    boolean hasArrived;
    String key;
    Comparator<Thought> listComparator;

    public Thought() {}

    public Thought(
            String fromUid,
            String connectionFromUid,
            String connectionToUid,
            String extraPhotoUrl,
            String timestamp,
            String text,
            boolean hasArrived,
            String key) {
        this.fromUid = fromUid;
        this.connectionFromUid = connectionFromUid;
        this.connectionToUid = connectionToUid;
        this.extraPhotoUrl = extraPhotoUrl;
        this.timestamp = timestamp;
        this.text = text;
        this.hasArrived = hasArrived;
        this.key = key;
    }

    protected Thought(Parcel in) {
        fromUid = in.readString();
        connectionFromUid = in.readString();
        connectionToUid = in.readString();
        extraPhotoUrl = in.readString();
        timestamp = in.readString();
        text = in.readString();
        hasArrived = in.readByte() != 0;
        key = in.readString();
    }

    public static final Creator<Thought> CREATOR = new Creator<Thought>() {
        @Override
        public Thought createFromParcel(Parcel in) {
            return new Thought(in);
        }

        @Override
        public Thought[] newArray(int size) {
            return new Thought[size];
        }
    };

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
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

    public boolean hasArrived() {
        return hasArrived;
    }

    public void setHasArrived(boolean hasArrived) {
        this.hasArrived = hasArrived;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(fromUid);
        parcel.writeString(connectionFromUid);
        parcel.writeString(connectionToUid);
        parcel.writeString(extraPhotoUrl);
        parcel.writeString(timestamp);
        parcel.writeString(text);
        parcel.writeByte((byte) (hasArrived ? 1 : 0));
        parcel.writeString(key);
    }

    @Override
    public int compare(Thought thoughtOne, Thought thoughtTwo) {
        return listComparator.compare(thoughtOne, thoughtTwo);
    }
}
