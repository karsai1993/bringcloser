package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Laci on 26/06/2018.
 */

public class ConnectionDetail implements Parcelable{

    private String fromUid;
    private String fromName;
    private String fromGender;
    private String fromPhotoUrl;
    private String fromBirthday;
    private String toUid;
    private String toName;
    private String toGender;
    private String toPhotoUrl;
    private String toBirthday;
    private String type;
    private int connectionBit;

    public ConnectionDetail() {}

    public ConnectionDetail(
            String fromUid,
            String fromName,
            String fromGender,
            String fromPhotoUrl,
            String fromBirthday,
            String toUid,
            String toName,
            String toGender,
            String toPhotoUrl,
            String toBirthday,
            String type,
            int connectionBit) {
        this.fromUid = fromUid;
        this.fromName = fromName;
        this.fromGender = fromGender;
        this.fromPhotoUrl = fromPhotoUrl;
        this.fromBirthday = fromBirthday;
        this.toUid = toUid;
        this.toName = toName;
        this.toGender = toGender;
        this.toPhotoUrl = toPhotoUrl;
        this.toBirthday = toBirthday;
        this.type = type;
        this.connectionBit = connectionBit;
    }

    protected ConnectionDetail(Parcel in) {
        fromUid = in.readString();
        fromName = in.readString();
        fromGender = in.readString();
        fromPhotoUrl = in.readString();
        fromBirthday = in.readString();
        toUid = in.readString();
        toName = in.readString();
        toGender = in.readString();
        toPhotoUrl = in.readString();
        toBirthday = in.readString();
        type = in.readString();
        connectionBit = in.readInt();
    }

    public static final Creator<ConnectionDetail> CREATOR = new Creator<ConnectionDetail>() {
        @Override
        public ConnectionDetail createFromParcel(Parcel in) {
            return new ConnectionDetail(in);
        }

        @Override
        public ConnectionDetail[] newArray(int size) {
            return new ConnectionDetail[size];
        }
    };

    public String getFromBirthday() {
        return fromBirthday;
    }

    public void setFromBirthday(String fromBirthday) {
        this.fromBirthday = fromBirthday;
    }

    public String getToBirthday() {
        return toBirthday;
    }

    public void setToBirthday(String toBirthday) {
        this.toBirthday = toBirthday;
    }

    public String getFromPhotoUrl() {
        return fromPhotoUrl;
    }

    public void setFromPhotoUrl(String fromPhotoUrl) {
        this.fromPhotoUrl = fromPhotoUrl;
    }

    public String getToPhotoUrl() {
        return toPhotoUrl;
    }

    public void setToPhotoUrl(String toPhotoUrl) {
        this.toPhotoUrl = toPhotoUrl;
    }

    public int getConnectionBit() {
        return connectionBit;
    }

    public void setConnectionBit(int connectionBit) {
        this.connectionBit = connectionBit;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromGender() {
        return fromGender;
    }

    public void setFromGender(String fromGender) {
        this.fromGender = fromGender;
    }

    public String getToUid() {
        return toUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getToGender() {
        return toGender;
    }

    public void setToGender(String toGender) {
        this.toGender = toGender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(fromUid);
        parcel.writeString(fromName);
        parcel.writeString(fromGender);
        parcel.writeString(fromPhotoUrl);
        parcel.writeString(fromBirthday);
        parcel.writeString(toUid);
        parcel.writeString(toName);
        parcel.writeString(toGender);
        parcel.writeString(toPhotoUrl);
        parcel.writeString(toBirthday);
        parcel.writeString(type);
        parcel.writeInt(connectionBit);
    }
}
