package com.hkl.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.hkl.common.TagsCountParser;

/**
 * Created by hkl on 16. 8. 18.
 */
public class TagsCountMapper extends Mapper<LongWritable, Text, Text, IntWritable>
{
    private final static IntWritable one = new IntWritable(1);
    private Text tag = new Text();

    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException
    {
        TagsCountParser parser = new TagsCountParser(value);
        String[] genres = parser.getGenres();

        if(parser.getRating() >= 4.0f)
        {
            for (String genre : genres)
            {
                if(!genre.isEmpty())
                {
                    tag.set(genre);
                    context.write(tag, one);
                }
            }
        }
    }
}
