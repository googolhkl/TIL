package com.hkl.common;

import org.apache.hadoop.io.Text;

/**
 * Created by hkl on 16. 8. 18.
 */

public class TagsCountParser
{
    private int movieID;
    private String userID_movieID;
    private int userID;
    private String tag;
    private long timeStamp;
    private float rating;
    private String title;
    private String[] genres;

    public TagsCountParser(Text text)
    {
       try
       {
           String[] colums = text.toString().split(",");

           movieID = Integer.parseInt(colums[0].trim());
           userID_movieID = colums[1].trim();
           userID = Integer.parseInt(colums[2].trim());
           tag = colums[4].trim();
           timeStamp = Long.parseLong(colums[5].trim());
           rating = Float.parseFloat(colums[6].trim());
           title = colums[7].trim();


           if( colums[8].charAt(0) == '"')
           {
               genres = colums[8].split("\"");
           }
           else
           {
               genres = colums[8].split("\\|");
           }
       }
       catch(Exception e)
       {
           System.err.println("에러 발생(TagsCountParser.java) : " + e.getMessage());
       }
       finally{}
    }
    //getter
    public int getMovieID() { return movieID; }
    public String getUserID_movieID() { return userID_movieID; }
    public int getUserID() { return userID; }
    public String getTag() { return tag; }
    public long getTimeStamp() { return timeStamp; }
    public float getRating() { return rating; }
    public String getTitle() { return title; }
    public String[] getGenres() { return genres; }

    //setter
    public void setMovieID(int movieID) { this.movieID = movieID; }
    public void setUserID_movieID(String userID_movieID) { this.userID_movieID = userID_movieID; }
    public void setUserID(int userID) { this.userID = userID; }
    public void setTag(String tag) { this.tag = tag; }
    public void setTimeStamp(long timeStamp) { this.timeStamp = timeStamp; }
    public void setRating(float rating) { this.rating = rating; }
    public void setTitle(String title) { this.title = title; }
    public void setGenres(String[] genres) { this.genres = genres; }
}
