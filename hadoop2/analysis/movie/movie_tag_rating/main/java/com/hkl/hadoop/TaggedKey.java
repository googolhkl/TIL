package com.hkl.hadoop;

/**
 * Created by hkl on 16. 8. 9.
 */
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TaggedKey implements WritableComparable<TaggedKey>
{
    private String movieIdStr;
    private Integer tag;

    public TaggedKey(){}

    public TaggedKey(String movieIdStr, int tag)
    {
        this.movieIdStr= movieIdStr;
        this.tag = tag;
    }


    public String getMovieIdStr() { return movieIdStr; }
    public void setMovieIdStr(String movieIdStr) { this.movieIdStr= movieIdStr; }

    public Integer getTag()
    {
        return tag;
    }
    public void setTag(Integer tag) { this.tag = tag; }

    @Override
    public int compareTo(TaggedKey key)
    {
        int result = this.movieIdStr.compareTo(key.movieIdStr);

        if(result ==0)
        {
            return this.tag.compareTo(key.tag);
        }
        return result;
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        WritableUtils.writeString(out, movieIdStr);
        out.writeInt(tag);
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        movieIdStr = WritableUtils.readString(in);
        tag = in.readInt();
    }
}

