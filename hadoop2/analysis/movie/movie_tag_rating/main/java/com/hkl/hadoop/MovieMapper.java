package com.hkl.hadoop;

/**
 * Created by hkl on 16. 8. 9.
 */


import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.hkl.common.MovieFileParser;

public class MovieMapper extends Mapper<LongWritable, Text, TaggedKey, Text>
{
    TaggedKey outputKey = new TaggedKey();
    Text outValue = new Text();

    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException
    {
        MovieFileParser parser = new MovieFileParser(value);

        // 출력 형식은 <<MovieId,0>,title,genres> 으로 나온다
        outputKey.setMovieIdStr( Integer.toString(parser.getMovieId()));
        outputKey.setTag(0);
        outValue.set(parser.getTitle() + "," + parser.getGenres());

        context.write(outputKey, outValue);
    }
}