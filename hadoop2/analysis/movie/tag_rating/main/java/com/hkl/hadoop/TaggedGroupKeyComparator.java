package com.hkl.hadoop;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * Created by hkl on 16. 8. 7.
 */
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

        return k1.getUserIdMovieId().compareTo(k2.getUserIdMovieId());
    }
}
