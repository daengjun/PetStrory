package com.example.petdiary.model;

public class MemberInfo {

    private String email;
    private String nickName;
    private String password;
    private String profileImg;
    private String memo;



    private String fcmToken;

    public MemberInfo(String email, String password, String nickName, String profileImg, String memo,String fcmToken){
        this.email = email;
        this.password = password;
        this.nickName = nickName;
        this.profileImg = profileImg;
        this.memo = memo;
        this.fcmToken = fcmToken;
    }


    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword(){
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickName(){
        return this.nickName;
    }
    public void setNickName(String nickName){
        this.nickName = nickName;
    }

    public String getProfileImg(){
        return this.profileImg;
    }
    public void setProfileImg(String profileImg){
        this.profileImg = profileImg;
    }

    public String getMemo(){
        return this.memo;
    }
    public void setMemo(String memo) {
        this.memo = memo;
    }

}
