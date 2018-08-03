package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Class to create object for wish
 */
public class Wish implements Parcelable, Comparator<Wish> {

    String fromUid;
    String connectionFromUid;
    String connectionToUid;
    String extraPhotoUrl;
    String whenToArrive;
    String occasion;
    String text;
    boolean hasArrived;
    String key;
    Comparator<Wish> listComparator;

    public Wish() {}

    public Wish(
            String fromUid,
            String connectionFromUid,
            String connectionToUid,
            String extraPhotoUrl,
            String whenToArrive,
            String occasion,
            String text,
            boolean hasArrived,
            String key) {
        this.fromUid = fromUid;
        this.connectionFromUid = connectionFromUid;
        this.connectionToUid = connectionToUid;
        this.extraPhotoUrl = extraPhotoUrl;
        this.whenToArrive = whenToArrive;
        this.occasion = occasion;
        this.text = text;
        this.hasArrived = hasArrived;
        this.key = key;
    }

    protected Wish(Parcel in) {
        fromUid = in.readString();
        connectionFromUid = in.readString();
        connectionToUid = in.readString();
        extraPhotoUrl = in.readString();
        whenToArrive = in.readString();
        occasion = in.readString();
        text = in.readString();
        hasArrived = in.readByte() != 0;
        key = in.readString();
    }

    public static final Creator<Wish> CREATOR = new Creator<Wish>() {
        @Override
        public Wish createFromParcel(Parcel in) {
            return new Wish(in);
        }

        @Override
        public Wish[] newArray(int size) {
            return new Wish[size];
        }
    };

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getExtraPhotoUrl() {
        return extraPhotoUrl;
    }

    public void setExtraPhotoUrl(String extraPhotoUrl) {
        this.extraPhotoUrl = extraPhotoUrl;
    }

    public String getWhenToArrive() {
        return whenToArrive;
    }

    public void setWhenToArrive(String whenToArrive) {
        this.whenToArrive = whenToArrive;
    }

    public String getOccasion() {
        return occasion;
    }

    public void setOccasion(String occasion) {
        this.occasion = occasion;
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

    @Override
    public int compare(Wish wishOne, Wish wishTwo) {
        return listComparator.compare(wishOne, wishTwo);
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
        parcel.writeString(whenToArrive);
        parcel.writeString(occasion);
        parcel.writeString(text);
        parcel.writeByte((byte) (hasArrived ? 1 : 0));
        parcel.writeString(key);
    }
}
