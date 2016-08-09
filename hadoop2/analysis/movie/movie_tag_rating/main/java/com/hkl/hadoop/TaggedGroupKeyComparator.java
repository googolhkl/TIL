package com.hkl.hadoop;

/**
 * Created by hkl on 16. 8. 9.
 */

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class TaggedGroupKeyComparator extends WritableComparator
{
    protected TaggedGroupKeyComparator()
    {
        super(TaggedKey.class, true);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public int compare(WritableComparable w1, WritableComparable w2)
    {
        TaggedKey k1 = (TaggedKey) w1;
        TaggedKey k2 = (TaggedKey) w2;

        return k1.getMovieIdStr().compareTo(k2.getMovieIdStr());
    }
}
