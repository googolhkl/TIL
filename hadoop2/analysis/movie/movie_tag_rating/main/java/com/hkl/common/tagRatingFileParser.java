package com.hkl.common;

import org.apache.hadoop.io.Text;

/**
 * Created by hkl on 16. 8. 9.
 */
public class tagRatingFileParser
{
    private String userIdMovieId;
    private int userId;
    private int movieId;
    private String tag;
    private long  timestamp;
    private String rating;

    public tagRatingFileParser(Text text)
    {
        try
        {
            String[] colums = text.toString().split(",");

            userIdMovieId = colums[0].trim();
            userId = Integer.parseInt(colums[1].trim());
            movieId = Integer.parseInt(colums[2].trim());
            tag = colums[3].trim();
            timestamp = Long.parseLong(colums[4].trim());
            rating = colums[5].trim();
        } catch (Exception e)
        {
            System.out.println("에러 발생(tagRatingFile) : " + e.getMessage());
        } finally {}
    }

    // getter
    public String getUserIdMovieId() { return userIdMovieId; }
    public int getUserId() { return userId; }
    public int getMovieId() { return movieId; }
    public String getTag() { return tag; }
    public long getTimestamp() { return timestamp; }
    public String getRating() { return rating; }

    // setter
    public void setUserIdMovieId(String userIdMovieId) { this.userIdMovieId = userIdMovieId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public void setTag(String tag) { this.tag = tag; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setRating(String rating) { this.rating = rating; }
}
