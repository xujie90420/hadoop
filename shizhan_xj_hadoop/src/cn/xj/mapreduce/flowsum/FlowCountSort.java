package cn.xj.mapreduce.flowsum;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 13480253104 180 180 360 
 * 13502468823 7335 110349 117684 
 * 13560436666 1116 954 2070
 * 
 * @author
 * 
 */
public class FlowCountSort {
	
	static class FlowCountMapper extends Mapper<LongWritable, Text, FlowBean, Text> {
		Text text=new Text();
		FlowBean bean=new FlowBean();
		
		@Override
		protected void map(LongWritable key, Text value,Context context)throws IOException, InterruptedException {
			
			String line = value.toString();
			String[] fileds=line.split("\t");
			//取出手机号
			String phone = fileds[0];
			//取出上行流量下行流量
			Integer upFlow = Integer.parseInt(fileds[1]);
			Integer dFlow = Integer.parseInt(fileds[2]);
			bean.set(upFlow, dFlow);
			text.set(phone);
			context.write(bean,text);
		}
	}
	
	static class FlowCountRedeuce extends Reducer<FlowBean, Text, Text, FlowBean>{
		
		@Override
		protected void reduce(FlowBean bean, Iterable<Text> values,Context context)throws IOException, InterruptedException {
			context.write(values.iterator().next(), bean);
		}
	}
	
	public static void main(String[] args) throws Exception {
		Configuration conf=new Configuration();
		Job job = Job.getInstance(conf);
		
		job.setJarByClass(FlowCountSort.class);
		
		//指定map和reduce
		job.setMapperClass(FlowCountMapper.class);
		job.setReducerClass(FlowCountRedeuce.class);
		
		//指定mapper输出的key和value类型
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(FlowBean.class);
		
		//指定最终输出的key和value类型
		job.setOutputKeyClass(Text.class);
		job.setOutputKeyClass(FlowBean.class);
		
		//输入输出路径
		FileInputFormat.setInputPaths(job, new Path(""));
		FileOutputFormat.setOutputPath(job, new Path(""));
		
		/*job.submit();*/
		boolean flag = job.waitForCompletion(true);
		System.exit(flag?0:1);
		
	}
}
