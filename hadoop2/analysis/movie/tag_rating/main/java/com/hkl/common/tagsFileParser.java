package com.hkl.common;
import org.apache.hadoop.io.Text;

/**
 * Created by hkl on 16. 8. 8.
 */
public class tagsFileParser
{
    private int userId;
    private int movieId;
    private String tag;
    private long timestamp;

    public tagsFileParser(Text text)
    {
        try
        {
            String[] colums = text.toString().split(",");

            userId = Integer.parseInt(colums[0].trim());
            movieId = Integer.parseInt(colums[1].trim());
            tag = colums[2].trim();
            timestamp = Long.parseLong(colums[3].trim());
        } catch (Exception e)
        {
            System.out.println("에러 발생(tags) : " + e.getMessage());
        } finally {}
    }

        // getter
        public int getUserId() { return userId; }
        public int getMovieId() { return movieId; }
        public String getTag() { return tag; }
        public long getTimestamp() { return timestamp; }

        // setter
        public void setUserId(int userId) { this.userId = userId; }
        public void setMovieId(int movieId) { this.movieId = movieId; }
        public void setTag(String tag) { this.tag = tag; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
