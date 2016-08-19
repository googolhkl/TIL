package com.hkl.hadoop;

/**
 * Created by hkl on 16. 8. 19.
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.hkl.common.CountParser;

import java.io.IOException;

public class popularMovie2sequenceFile extends Configured implements Tool
{
    static class CountMapper extends MapReduceBase implements
            Mapper<LongWritable, Text, IntWritable, Text>
    {
        private IntWritable outputKey = new IntWritable();

        public void map(LongWritable key, Text value,
                        OutputCollector<IntWritable, Text> output, Reporter reporter)
            throws IOException
        {
            try
            {
                CountParser parser = new CountParser(value);
                outputKey.set(parser.getCount());
                output.collect(outputKey, value);
            }
            catch(ArrayIndexOutOfBoundsException ae)
            {
                outputKey.set(0);
                output.collect(outputKey, value);
                ae.printStackTrace();
            }
            catch(Exception e)
            {
                outputKey.set(0);
                output.collect(outputKey, value);
                e.printStackTrace();
            }
            finally {}
        }
    }

    public int run(String[] args) throws Exception
    {
        JobConf conf = new JobConf(popularMovie2sequenceFile.class);
        conf.setJobName("popularMovie2seQuenceFile");

        conf.setMapperClass(CountMapper.class);
        conf.setNumReduceTasks(0);

        // 입출력 경로 설정
        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        //출력 포맷을 SequenceFile로 설정
        conf.setOutputFormat(SequenceFileOutputFormat.class);
        //출력 키를 카운트(count) 로설정
        conf.setOutputKeyClass(IntWritable.class);
        // 출력값을 영화 정보(Text)로 설정
        conf.setOutputValueClass(Text.class);

        //시퀀스파일 입축 포맷 설정
        SequenceFileOutputFormat.setCompressOutput(conf, true);
        SequenceFileOutputFormat.setOutputCompressorClass(conf,GzipCodec.class);
        SequenceFileOutputFormat.setOutputCompressionType(conf, CompressionType.BLOCK);
        JobClient.runJob(conf);
        return 0;
    }

    public static void main(String[] args) throws Exception
    {
        int res = ToolRunner.run(new Configuration(), new popularMovie2sequenceFile(), args);
        System.out.println("MR-Job result = " + res);
    }
}
