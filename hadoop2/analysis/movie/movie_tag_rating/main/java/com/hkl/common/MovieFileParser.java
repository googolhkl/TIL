package com.hkl.common;

/**
 * Created by hkl on 16. 8. 9.
 */
import org.apache.hadoop.io.Text;

/**
 * Created by hkl on 16. 8. 8.
 */
public class MovieFileParser
{
    private int movieId;
    private String title;
    private String genres;

    public MovieFileParser(Text text)
    {
        try
        {
            String[] colums = text.toString().split(",");

            movieId = Integer.parseInt(colums[0].trim());
            title = colums[1].trim();
            genres = colums[2].trim();
        } catch (Exception e)
        {
            System.out.println("에러 발생(moviefile) : " + e.getMessage());
        } finally {}
    }

    // getter
    public int getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getGenres() { return genres; }

    // setter
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public void setTag(String title) { this.title = title; }
    public void setGenres(String genres) { this.genres = genres; }
}