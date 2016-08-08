package com.hkl.hadoop;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.hkl.common.tagParser;

/**
 * Created by hkl on 16. 8. 7.
 */
public class ReducerWithReduceSideJoin extends Reducer<TaggedKey, Text, Text, Text>
{
    // 출력키
    private Text outputKey = new Text();

    // 출력값
    private Text outputValue = new Text();

    public void reduce(TaggedKey key, Iterable<Text> values, Context context)
        throws IOException, InterruptedException
    {
        String tags;
        Iterator<Text> iterator = values.iterator();

        tags = iterator.next().toString();

        while(iterator.hasNext())
        {
            Text record = iterator.next();
            tagParser parser = new tagParser(record);

            if(parser.getTag() == 0)
            {
                tags += "|" + parser.getTags();
            }
            else
            {
                outputKey.set(key.getUserIdMovieId());
                outputValue = new Text( "," + record.toString() + "," + tags);
                context.write(outputKey, outputValue);
            }
        }
    }
}
