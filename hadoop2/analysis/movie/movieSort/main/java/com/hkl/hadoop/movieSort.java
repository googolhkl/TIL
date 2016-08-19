package com.hkl.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.InputSampler;
import org.apache.hadoop.mapred.lib.TotalOrderPartitioner;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.net.URI;


/**
 * Created by hkl on 16. 8. 18.
 */
public class movieSort  extends Configured implements Tool
{
    public static void main(String[] args) throws Exception
    {
        int res = ToolRunner.run(new Configuration(), new movieSort(), args);
        System.out.println("MB-JOB result: " + res);
    }

    public int run(String[] args) throws Exception
    {
        JobConf conf = new JobConf(getConf(), movieSort.class);
        conf.setJobName("movieSort");

        conf.setInputFormat(SequenceFileInputFormat.class);
        conf.setOutputFormat(SequenceFileOutputFormat.class);
        conf.setOutputKeyClass(IntWritable.class);
        conf.setPartitionerClass(TotalOrderPartitioner.class);

        SequenceFileOutputFormat.setCompressOutput(conf,true);
        SequenceFileOutputFormat.setOutputCompressorClass(conf, GzipCodec.class);
        SequenceFileOutputFormat.setOutputCompressionType(conf, CompressionType.BLOCK);

        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        Path inputDir = FileInputFormat.getInputPaths(conf)[0];
        inputDir = inputDir.makeQualified(inputDir.getFileSystem(conf));
        Path partitionFile = new Path(inputDir, "_partitions");
        TotalOrderPartitioner.setPartitionFile(conf,partitionFile);

        InputSampler.Sampler<IntWritable, Text> sampler =
                new InputSampler.RandomSampler<IntWritable, Text>(0.1, 1000, 10);
        InputSampler.writePartitionFile(conf, sampler);

        URI partitionUri = new URI(partitionFile.toString() + "#_partitions");
        DistributedCache.addCacheFile(partitionUri, conf);
        DistributedCache.createSymlink(conf);

        JobClient.runJob(conf);
        return 0;
    }
}
