package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Class to create detailed object for event
 */
public class EventDetail implements Parcelable{

    String fromUid;
    String fromPhotoUrl;
    String fromName;
    String extraPhotoUrl;
    String whenToArrive;
    String title;
    String place;
    String text;
    Comparator<EventDetail> listComparator;

    public EventDetail(
            String fromUid,
            String fromPhotoUrl,
            String fromName,
            String extraPhotoUrl,
            String whenToArrive,
            String title,
            String place,
            String text) {
        this.fromUid = fromUid;
        this.fromPhotoUrl = fromPhotoUrl;
        this.fromName = fromName;
        this.extraPhotoUrl = extraPhotoUrl;
        this.whenToArrive = whenToArrive;
        this.title = title;
        this.place = place;
        this.text = text;
    }

    protected EventDetail(Parcel in) {
        fromUid = in.readString();
        fromPhotoUrl = in.readString();
        fromName = in.readString();
        extraPhotoUrl = in.readString();
        whenToArrive = in.readString();
        title = in.readString();
        place = in.readString();
        text = in.readString();
    }

    public static final Creator<EventDetail> CREATOR = new Creator<EventDetail>() {
        @Override
        public EventDetail createFromParcel(Parcel in) {
            return new EventDetail(in);
        }

        @Override
        public EventDetail[] newArray(int size) {
            return new EventDetail[size];
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

    public String getWhenToArrive() {
        return whenToArrive;
    }

    public void setWhenToArrive(String whenToArrive) {
        this.whenToArrive = whenToArrive;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
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
        parcel.writeString(whenToArrive);
        parcel.writeString(title);
        parcel.writeString(place);
        parcel.writeString(text);
    }
}
