package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UnusedPhotoDetail implements Parcelable{

    private String fromUid;
    private String toUid;
    private String photoUrl;

    protected UnusedPhotoDetail(Parcel in) {
        fromUid = in.readString();
        toUid = in.readString();
        photoUrl = in.readString();
    }

    public static final Creator<UnusedPhotoDetail> CREATOR = new Creator<UnusedPhotoDetail>() {
        @Override
        public UnusedPhotoDetail createFromParcel(Parcel in) {
            return new UnusedPhotoDetail(in);
        }

        @Override
        public UnusedPhotoDetail[] newArray(int size) {
            return new UnusedPhotoDetail[size];
        }
    };

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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public UnusedPhotoDetail(String fromUid, String toUid, String photoUrl) {
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.photoUrl = photoUrl;
    }

    public UnusedPhotoDetail() {}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(fromUid);
        parcel.writeString(toUid);
        parcel.writeString(photoUrl);
    }
}
