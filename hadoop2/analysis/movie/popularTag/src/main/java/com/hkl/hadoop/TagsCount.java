package com.hkl.hadoop;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * Created by hkl on 16. 8. 18.
 */
public class TagsCount
{
    public static void main(String[] args) throws Exception
    {
        Configuration conf = new Configuration();
        if(args.length != 2)
        {
            System.err.println("사용 방법 : TagsCount <input> <output>");
            System.exit(2);
        }

        Job job = new Job(conf, "TagsCount");

        job.setJarByClass(TagsCount.class);
        job.setMapperClass(TagsCountMapper.class);
        job.setReducerClass(TagsCountReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}
