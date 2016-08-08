package com.hkl.hadoop;

/**
 * Created by hkl on 16. 8. 7.
 */
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TaggedKey implements WritableComparable<TaggedKey>
{
    private String userId_movieId;
    private Integer tag;

    public TaggedKey(){}

    public TaggedKey(String userId_movieId, int tag)
    {
        this.userId_movieId = userId_movieId;
        this.tag = tag;
    }


    public String getUserIdMovieId() { return userId_movieId; }
    public void setUserId_movieId(String userId_movieId) { this.userId_movieId = userId_movieId; }

    public Integer getTag()
    {
        return tag;
    }
    public void setTag(Integer tag) { this.tag = tag; }

    @Override
    public int compareTo(TaggedKey key)
    {
        int result = this.userId_movieId.compareTo(key.userId_movieId);

        if(result ==0)
        {
            return this.tag.compareTo(key.tag);
        }
        return result;
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        WritableUtils.writeString(out, userId_movieId);
        out.writeInt(tag);
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        userId_movieId = WritableUtils.readString(in);
        tag = in.readInt();
    }
}
