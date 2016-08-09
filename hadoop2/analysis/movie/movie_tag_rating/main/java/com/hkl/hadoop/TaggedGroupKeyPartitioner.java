package com.hkl.hadoop;

/**
 * Created by hkl on 16. 8. 9.
 */

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class TaggedGroupKeyPartitioner extends Partitioner<TaggedKey, Text>
{
    @Override
    public int getPartition(TaggedKey key, Text val, int numPartitions)
    {
        // user ID의 해시값으로 파티션 계산
        int hash = key.getMovieIdStr().hashCode();
        int partition = hash % numPartitions;
        return partition;
    }
}
