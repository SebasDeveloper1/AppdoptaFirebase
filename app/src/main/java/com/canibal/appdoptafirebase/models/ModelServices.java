package com.canibal.appdoptafirebase.models;

public class ModelServices {
    //use same name as we given while uploading post
    // use el mismo nombre que le dimos al subir la publicación

    String pId, pTitle, pCategory, pLocation, pSchedule, ccp, pNumercontact, pServicewebsite, pDescr, pLikes, pComments, pImage, pTime, uid, uEmail, uDp, uName;

    public ModelServices() {
    }

    public ModelServices(String pId, String pTitle, String pCategory, String pLocation, String pSchedule, String ccp, String pNumercontact, String pServicewebsite, String pDescr, String pLikes, String pComments, String pImage, String pTime, String uid, String uEmail, String uDp, String uName) {
        this.pId = pId;
        this.pTitle = pTitle;
        this.pCategory = pCategory;
        this.pLocation = pLocation;
        this.pSchedule = pSchedule;
        this.ccp = ccp;
        this.pNumercontact = pNumercontact;
        this.pServicewebsite = pServicewebsite;
        this.pDescr = pDescr;
        this.pLikes = pLikes;
        this.pComments = pComments;
        this.pImage = pImage;
        this.pTime = pTime;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uName = uName;

    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpTitle() {
        return pTitle;
    }

    public void setpTitle(String pTitle) {
        this.pTitle = pTitle;
    }

    public String getpCategory() {
        return pCategory;
    }

    public void setpCategory(String pCategory) {
        this.pCategory = pCategory;
    }

    public String getpLocation() {
        return pLocation;
    }

    public void setpLocation(String pLocation) {
        this.pLocation = pLocation;
    }

    public String getpSchedule() {
        return pSchedule;
    }

    public void setpSchedule(String pSchedule) {
        this.pSchedule = pSchedule;
    }

    public String getCcp() {
        return ccp;
    }

    public void setCcp(String ccp) {
        this.ccp = ccp;
    }

    public String getpNumercontact() {
        return pNumercontact;
    }

    public void setpNumercontact(String pNumercontact) {
        this.pNumercontact = pNumercontact;
    }

    public String getpServicewebsite() {
        return pServicewebsite;
    }

    public void setpServicewebsite(String pServicewebsite) {
        this.pServicewebsite = pServicewebsite;
    }

    public String getpDescr() {
        return pDescr;
    }

    public void setpDescr(String pDescr) {
        this.pDescr = pDescr;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getpComments() {
        return pComments;
    }

    public void setpComments(String pComments) {
        this.pComments = pComments;
    }

    public String getpImage() {
        return pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }
}