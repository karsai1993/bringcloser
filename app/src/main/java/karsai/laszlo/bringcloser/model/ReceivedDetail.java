package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Class to create object for received detail
 */
public class ReceivedDetail implements Parcelable, Comparator<ReceivedDetail>{

    WishDetail wishDetail;
    EventDetail eventDetail;
    ThoughtDetail thoughtDetail;
    String type;
    Comparator<ReceivedDetail> listComparator;

    public ReceivedDetail(WishDetail wishDetail, String type) {
        this.wishDetail = wishDetail;
        this.type = type;
    }

    public ReceivedDetail(EventDetail eventDetail, String type) {
        this.eventDetail = eventDetail;
        this.type = type;
    }

    public ReceivedDetail(ThoughtDetail thoughtDetail, String type) {
        this.thoughtDetail = thoughtDetail;
        this.type = type;
    }

    protected ReceivedDetail(Parcel in) {
        wishDetail = in.readParcelable(WishDetail.class.getClassLoader());
        eventDetail = in.readParcelable(EventDetail.class.getClassLoader());
        thoughtDetail = in.readParcelable(ThoughtDetail.class.getClassLoader());
        type = in.readString();
    }

    public static final Creator<ReceivedDetail> CREATOR = new Creator<ReceivedDetail>() {
        @Override
        public ReceivedDetail createFromParcel(Parcel in) {
            return new ReceivedDetail(in);
        }

        @Override
        public ReceivedDetail[] newArray(int size) {
            return new ReceivedDetail[size];
        }
    };

    public WishDetail getWishDetail() {
        return wishDetail;
    }

    public void setWishDetail(WishDetail wishDetail) {
        this.wishDetail = wishDetail;
    }

    public EventDetail getEventDetail() {
        return eventDetail;
    }

    public void setEventDetail(EventDetail eventDetail) {
        this.eventDetail = eventDetail;
    }

    public ThoughtDetail getThoughtDetail() {
        return thoughtDetail;
    }

    public void setThoughtDetail(ThoughtDetail thoughtDetail) {
        this.thoughtDetail = thoughtDetail;
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
        parcel.writeParcelable(wishDetail, i);
        parcel.writeParcelable(eventDetail, i);
        parcel.writeParcelable(thoughtDetail, i);
    }

    @Override
    public int compare(ReceivedDetail receivedDetailOne, ReceivedDetail receivedDetailTwo) {
        return listComparator.compare(receivedDetailOne, receivedDetailTwo);
    }
}
