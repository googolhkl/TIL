package com.hkl.common;
import org.apache.hadoop.io.Text;
/**
 * Created by hkl on 16. 8. 8.
 */
public class tagParser
{
    private String userIdMovieId;
    private int tag;
    private String tags;

    public tagParser(Text text)
    {
        try
        {
            String[] colums = text.toString().split(",");

            userIdMovieId = colums[0].trim();
            tag = Integer.parseInt(colums[1].trim());
            tags = colums[2].trim();
        }
        catch(Exception e)
        {
            System.out.println("에러 발생(tag) : " + e.getMessage());
        }
        finally{}
    }

    public String getUserIdMovieId()
    {
        return userIdMovieId;
    }

    public int getTag()
    {
        return tag;
    }

    public String getTags()
    {
        return tags;
    }


    public void setUserIdMovieId(String userIdMovieId)
    {
        this.userIdMovieId = userIdMovieId;
    }

    public void setTag(int tag)
    {
        this.tag = tag;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }
}
