package com.example.bakingapp.data;

import androidx.room.ColumnInfo;

public class StepData {

    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "short_description")
    private String shortDescription;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "video_url")
    private String video_url;
    @ColumnInfo(name = "thumbnail_url")
    private String thumbnail_url;

    public StepData(int id, String shortDescription,  String description, String videoURL, String thumbnailURL){
        this.id = id;
        this.shortDescription = shortDescription;
        this.description = description;
        this.video_url = videoURL;
        this.thumbnail_url = thumbnailURL;
    }

    public int getId() {
        return id;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public String getVideo_url() {
        return video_url;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

}
