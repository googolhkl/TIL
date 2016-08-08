package com.hkl.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.hkl.common.tagsFileParser;
/**
 * Created by hkl on 16. 8. 8.
 */
public class tagsMapper extends Mapper<LongWritable, Text, TaggedKey, Text>
{
    TaggedKey outputKey = new TaggedKey();
    Text outValue = new Text();

    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException
    {
        tagsFileParser parser = new tagsFileParser(value);

        // 출력 형식은 <<유저ID_영화ID,0>,재밌는> 으로 나온다
        outputKey.setUserId_movieId( Integer.toString(parser.getUserId()) + "_" + Integer.toString( parser.getMovieId()));
        outputKey.setTag(0);
        outValue.set(parser.getTag());

        context.write(outputKey, outValue);
    }
}
