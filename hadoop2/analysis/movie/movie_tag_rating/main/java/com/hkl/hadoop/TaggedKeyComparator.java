package com.hkl.hadoop;

import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableComparable;

/**
 * Created by hkl on 16. 8. 9.
 */

public class TaggedKeyComparator extends WritableComparator
{
    protected TaggedKeyComparator()
    {
        super(TaggedKey.class, true);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public int compare(WritableComparable w1, WritableComparable w2)
    {
        TaggedKey k1 = (TaggedKey) w1;
        TaggedKey k2 = (TaggedKey) w2;

        int cmp = k1.getMovieIdStr().compareTo(k2.getMovieIdStr());
        if(cmp != 0)
        {
            return cmp;
        }

        return k1.getTag().compareTo(k2.getTag());
    }
}