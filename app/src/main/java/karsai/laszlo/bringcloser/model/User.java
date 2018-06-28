package karsai.laszlo.bringcloser.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Laci on 28/05/2018.
 */

public class User implements Parcelable {

    private boolean mIsEmailVerified;
    private String mUsername;
    private String mPhotoUrl;
    private String mBirthday;
    private String mGender;
    private Map<String, Object> mTokensMap;
    private String mUid;

    public User(
            boolean isEmailVerified,
            String username,
            String photoUrl,
            String birthday,
            String gender,
            Map<String, Object> tokensMap,
            String uid) {
        this.mIsEmailVerified = isEmailVerified;
        this.mUsername = username;
        this.mPhotoUrl = photoUrl;
        this.mBirthday = birthday;
        this.mGender = gender;
        this.mTokensMap = tokensMap;
        this.mUid = uid;
    }

    public User () {}

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        this.mUid = uid;
    }

    public Map<String, Object> getTokensMap() {
        return mTokensMap;
    }

    public void setTokensMap(Map<String, Object> tokensMap) {
        this.mTokensMap = tokensMap;
    }

    public boolean getIsEmailVerified() {
        return mIsEmailVerified;
    }

    public void setIsEmailVerified(boolean isEmailVerified) {
        this.mIsEmailVerified = isEmailVerified;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.mPhotoUrl = photoUrl;
    }

    public String getBirthday() {
        return mBirthday;
    }

    public void setBirthday(String birthday) {
        this.mBirthday = birthday;
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String gender) {
        this.mGender = gender;
    }

    protected User(Parcel in) {
        this.mIsEmailVerified = in.readInt() == 1 ? true : false;
        this.mUsername = in.readString();
        this.mPhotoUrl = in.readString();
        this.mBirthday = in.readString();
        this.mGender = in.readString();
        this.mTokensMap = in.readHashMap(Boolean.class.getClassLoader());
        this.mUid = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mIsEmailVerified ? 1 : 0);
        parcel.writeString(mUsername);
        parcel.writeString(mPhotoUrl);
        parcel.writeString(mBirthday);
        parcel.writeString(mGender);
        parcel.writeMap(mTokensMap);
        parcel.writeString(mUid);
    }
}
