package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Laci on 13/06/2018.
 */

public class Connection implements Parcelable {

    private String fromUid;
    private String toUid;
    private int connectionBit;
    private String type;

    public Connection(String fromUid, String toUid, int connectionBit, String type) {
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.connectionBit = connectionBit;
        this.type = type;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getToUid() {
        return toUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    public int getConnectionBit() {
        return connectionBit;
    }

    public void setConnectionBit(int connectionBit) {
        this.connectionBit = connectionBit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Connection() {}

    protected Connection(Parcel in) {
        this.fromUid = in.readString();
        this.toUid = in.readString();
        this.connectionBit = in.readInt();
        this.type = in.readString();
    }

    public static final Creator<Connection> CREATOR = new Creator<Connection>() {
        @Override
        public Connection createFromParcel(Parcel in) {
            return new Connection(in);
        }

        @Override
        public Connection[] newArray(int size) {
            return new Connection[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(fromUid);
        parcel.writeString(toUid);
        parcel.writeInt(connectionBit);
        parcel.writeString(type);
    }
}
