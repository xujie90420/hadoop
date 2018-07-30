package cn.xj.mapreduce.wcDemo;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * KEYIN: 默认情况下，是mr框架所读到的一行文本的起始偏移量，Long,
 * 但是在hadoop中有自己的更精简的序列化接口，所以不直接用Long，而用LongWritable
 * 
 * VALUEIN:默认情况下，是mr框架所读到的一行文本的内容，String，同上，用Text
 * 
 * KEYOUT：是用户自定义逻辑处理完成之后输出数据中的key，在此处是单词，String，同上，用Text
 * VALUEOUT：是用户自定义逻辑处理完成之后输出数据中的value，在此处是单词次数，Integer，同上，用IntWritable
 * 
 * @author
 *
 */

public class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable>{

	@Override
	protected void map(LongWritable key, Text value,Context context)throws IOException, InterruptedException {
		String line =value.toString();
		String[] words=line.split(" ");
		for (String word : words) {
			//将单词写入上下文context中
			context.write(new Text(word), new IntWritable(1));
		}
	}

}
