package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Class to create detailed object for wish
 */
public class WishDetail implements Parcelable{

    String fromUid;
    String fromPhotoUrl;
    String fromName;
    String extraPhotoUrl;
    String whenToArrive;
    String occasion;
    String text;
    Comparator<WishDetail> listComparator;

    public WishDetail(
            String fromUid,
            String fromPhotoUrl,
            String fromName,
            String extraPhotoUrl,
            String whenToArrive,
            String occasion,
            String text) {
        this.fromUid = fromUid;
        this.fromPhotoUrl = fromPhotoUrl;
        this.fromName = fromName;
        this.extraPhotoUrl = extraPhotoUrl;
        this.whenToArrive = whenToArrive;
        this.occasion = occasion;
        this.text = text;
    }

    protected WishDetail(Parcel in) {
        fromUid = in.readString();
        fromPhotoUrl = in.readString();
        fromName = in.readString();
        extraPhotoUrl = in.readString();
        whenToArrive = in.readString();
        occasion = in.readString();
        text = in.readString();
    }

    public static final Creator<WishDetail> CREATOR = new Creator<WishDetail>() {
        @Override
        public WishDetail createFromParcel(Parcel in) {
            return new WishDetail(in);
        }

        @Override
        public WishDetail[] newArray(int size) {
            return new WishDetail[size];
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

    public String getExtraPhotoUrl() {
        return extraPhotoUrl;
    }

    public void setExtraPhotoUrl(String extraPhotoUrl) {
        this.extraPhotoUrl = extraPhotoUrl;
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
        parcel.writeString(whenToArrive);
        parcel.writeString(occasion);
        parcel.writeString(text);
    }
}
