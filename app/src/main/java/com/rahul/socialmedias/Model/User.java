package com.rahul.socialmedias.Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class User implements Parcelable {

    private String userId;
    private String name;
    private String email;
    private String profilePic;
    private String bio;
    private String mobileNo;
    private String gender;
    private String dob;
    private ArrayList<String> followers;
    private ArrayList<String> following;

    public User(String userId, String name, String email, String profilePic, String bio,String mobileNo,String gender,String dob,
                ArrayList<String> followers, ArrayList<String> following) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profilePic = profilePic;
        this.bio = bio;
        this.mobileNo = mobileNo;
        this.gender = gender;
        this.dob = dob;
        this.followers = followers;
        this.following = following;
    }

    public User(){

    }

    protected User(Parcel in) {
        userId = in.readString();
        name = in.readString();
        email = in.readString();
        profilePic = in.readString();
        bio = in.readString();
        mobileNo = in.readString();
        gender = in.readString();
        dob = in.readString();
        followers = in.createStringArrayList();
        following = in.createStringArrayList();
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public ArrayList<String> getFollowers() {
        return followers;
    }

    public void setFollowers(ArrayList<String> followers) {
        this.followers = followers;
    }

    public ArrayList<String> getFollowing() {
        return following;
    }

    public void setFollowing(ArrayList<String> following) {
        this.following = following;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(profilePic);
        dest.writeString(bio);
        dest.writeString(mobileNo);
        dest.writeString(gender);
        dest.writeString(dob);
        dest.writeStringList(followers);
        dest.writeStringList(following);
    }
}
