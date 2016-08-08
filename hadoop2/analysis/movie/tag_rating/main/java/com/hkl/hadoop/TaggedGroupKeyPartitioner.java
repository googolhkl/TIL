package com.hkl.hadoop;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * Created by hkl on 16. 8. 7.
 */
public class TaggedGroupKeyPartitioner extends Partitioner<TaggedKey, Text>
{
    @Override
    public int getPartition(TaggedKey key, Text val, int numPartitions)
    {
        // user ID의 해시값으로 파티션 계산
        int hash = key.getUserIdMovieId().hashCode();
        int partition = hash % numPartitions;
        return partition;
    }
}
