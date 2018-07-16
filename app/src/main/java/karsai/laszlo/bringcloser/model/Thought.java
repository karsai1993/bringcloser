package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Thought implements Parcelable {

    String fromUid;
    String extraPhotoUrl;
    String timestamp;
    String content;
    boolean shouldBeSent;

    public Thought(
            String fromUid,
            String extraPhotoUrl,
            String timestamp,
            String content,
            boolean shouldBeSent) {
        this.fromUid = fromUid;
        this.extraPhotoUrl = extraPhotoUrl;
        this.timestamp = timestamp;
        this.content = content;
        this.shouldBeSent = shouldBeSent;
    }

    public Thought() {}

    protected Thought(Parcel in) {
        fromUid = in.readString();
        extraPhotoUrl = in.readString();
        timestamp = in.readString();
        content = in.readString();
        shouldBeSent = in.readByte() != 0;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isShouldBeSent() {
        return shouldBeSent;
    }

    public void setShouldBeSent(boolean shouldBeSent) {
        this.shouldBeSent = shouldBeSent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(fromUid);
        parcel.writeString(extraPhotoUrl);
        parcel.writeString(timestamp);
        parcel.writeString(content);
        parcel.writeByte((byte) (shouldBeSent ? 1 : 0));
    }
}
