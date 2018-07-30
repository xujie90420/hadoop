package bigdata.mr;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import bigdata.bean.WebLogBean;
import bigdata.bean.WebLogParser;

public class PreProgress {
	
	static class PreProgressMapper extends Mapper<LongWritable, Text, WebLogBean, NullWritable>{
		WebLogBean k =new WebLogBean();
		NullWritable v=NullWritable.get();
		//用来存储网站url分类数据
		Set<String> pages = new HashSet<String>();
		
		@Override
		protected void setup(Context context)throws IOException, InterruptedException {
			pages.add("/about");
			pages.add("/black-ip-list/");
			pages.add("/cassandra-clustor/");
			pages.add("/finance-rhive-repurchase/");
			pages.add("/hadoop-family-roadmap/");
			pages.add("/hadoop-hive-intro/");
			pages.add("/hadoop-zookeeper-intro/");
			pages.add("/hadoop-mahout-roadmap/");
		}
		@Override
		protected void map(LongWritable key,Text value,Context context)throws IOException, InterruptedException {
			String line=value.toString();
			k = WebLogParser.parser(line);
			WebLogParser.filtStaticResource(k, pages);
			//k.set(webLogBean.toString());
			context.write(k, v);
		}
	}
	
	static class PreProgressReducer extends Reducer<WebLogBean, NullWritable, NullWritable, WebLogBean>{
		
		@Override
		protected void reduce(WebLogBean bean, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
			context.write(NullWritable.get(), bean);
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		Configuration conf=new Configuration();
		Job job = Job.getInstance(conf);
		
		job.setJarByClass(PreProgress.class);
		
		job.setMapperClass(PreProgressMapper.class);
		job.setMapOutputKeyClass(WebLogBean.class);
		job.setMapOutputValueClass(NullWritable.class);
		
		//job.setNumReduceTasks(0);
		job.setReducerClass(PreProgressReducer.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(WebLogBean.class);
		
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job,new Path(args[1]));
		/*FileInputFormat.setInputPaths(job, new Path("f:/weblog/input"));
		FileOutputFormat.setOutputPath(job, new Path("f:/weblog/Weblogoutput"));*/
		
		job.waitForCompletion(true);
	}
	
}
