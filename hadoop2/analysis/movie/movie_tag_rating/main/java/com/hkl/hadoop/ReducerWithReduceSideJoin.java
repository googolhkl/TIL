package com.hkl.hadoop;

/**
 * Created by hkl on 16. 8. 9.
 */


import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class ReducerWithReduceSideJoin extends Reducer<TaggedKey, Text, Text, Text>
{
    // 출력키
    private Text outputKey = new Text();

    // 출력값
    private Text outputValue = new Text();

    public void reduce(TaggedKey key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException
    {
        Iterator<Text> iterator = values.iterator();

        Text TitleGenres = new Text(iterator.next());


        while(iterator.hasNext())
        {
            Text record = iterator.next();
            outputKey.set(key.getMovieIdStr());
            outputValue = new Text( "," + record.toString() + "," + TitleGenres.toString());
            context.write(outputKey, outputValue);
        }
    }
}