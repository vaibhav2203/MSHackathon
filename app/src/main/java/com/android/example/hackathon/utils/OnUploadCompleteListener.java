package com.android.example.hackathon.utils;

/*
* Interface for notifying hen upload is complete
* */
public interface OnUploadCompleteListener{
    public void onUploadComplete(String response);
    public void errorOnUpload(Exception e);
}
