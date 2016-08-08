package com.hkl.hadoop;

import java.io.IOException;
import java.io.InterruptedIOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.hkl.common.ratingsFileParser;

/**
 * Created by hkl on 16. 8. 7.
 */
public class MapperWithReduceSideJoin extends Mapper<LongWritable, Text, TaggedKey, Text>
{
    TaggedKey outputKey = new TaggedKey();

    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException
    {
        ratingsFileParser parser = new ratingsFileParser(value);
        // 출력형식은 <<유저ID_영화ID,1>,userid,movieid,rating,timestamp> 다.
        outputKey.setUserId_movieId( Integer.toString(parser.getUserId()) + "_" + Integer.toString( parser.getMovieId()));
        outputKey.setTag(1);

        context.write(outputKey, value);

    }
}
