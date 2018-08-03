package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Class to create object for event
 */
public class Event implements Parcelable, Comparator<Event> {

    String fromUid;
    String connectionFromUid;
    String connectionToUid;
    String extraPhotoUrl;
    String whenToArrive;
    String title;
    String place;
    String text;
    boolean hasArrived;
    String key;
    Comparator<Event> listComparator;

    public Event(
            String fromUid,
            String connectionFromUid,
            String connectionToUid,
            String extraPhotoUrl,
            String whenToArrive,
            String title,
            String place,
            String text,
            boolean hasArrived,
            String key) {
        this.fromUid = fromUid;
        this.connectionFromUid = connectionFromUid;
        this.connectionToUid = connectionToUid;
        this.extraPhotoUrl = extraPhotoUrl;
        this.whenToArrive = whenToArrive;
        this.title = title;
        this.place = place;
        this.text = text;
        this.hasArrived = hasArrived;
        this.key = key;
    }

    public Event() {}

    protected Event(Parcel in) {
        fromUid = in.readString();
        connectionFromUid = in.readString();
        connectionToUid = in.readString();
        extraPhotoUrl = in.readString();
        whenToArrive = in.readString();
        title = in.readString();
        place = in.readString();
        text = in.readString();
        hasArrived = in.readByte() != 0;
        key = in.readString();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
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
        parcel.writeString(whenToArrive);
        parcel.writeString(title);
        parcel.writeString(place);
        parcel.writeString(text);
        parcel.writeByte((byte) (hasArrived ? 1 : 0));
        parcel.writeString(key);
    }

    @Override
    public int compare(Event eventOne, Event eventTwo) {
        return listComparator.compare(eventOne, eventTwo);
    }
}
