package com.hkl.hadoop;

/**
 * Created by hkl on 16. 8. 9.
 */

import java.io.IOException;
import java.io.InterruptedIOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.hkl.common.tagRatingFileParser;

public class MapperWithReduceSideJoin extends Mapper<LongWritable, Text, TaggedKey, Text>
{
    TaggedKey outputKey = new TaggedKey();

    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException
    {
        tagRatingFileParser parser = new tagRatingFileParser(value);
        // 출력형식은 <<MovieId,1>, userID_movieID, userID, movieID, tag, timestamp, rating> 다.
        outputKey.setMovieIdStr(Integer.toString(parser.getMovieId()));
        outputKey.setTag(1);

        context.write(outputKey, value);

    }
}
