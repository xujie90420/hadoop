package cn.xj.GroupingComparator;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class SortMain {
	 static class SortMainMapper extends Mapper<LongWritable, Text, OrderBean, NullWritable>{
		 OrderBean b=new OrderBean();
		 Text text=new Text();
		 DoubleWritable dw=new DoubleWritable();
		 
		@Override
		protected void map(LongWritable key,Text value,Context context)throws IOException, InterruptedException {
			String line=value.toString();
			String[] fileds = line.split(",");
			text.set(fileds[0]);
			dw.set(Double.parseDouble(fileds[2]));
			b.set(text,dw );
			context.write(b, NullWritable.get());
		}
		 
	 }
	 
	 static class SortMainReducer extends Reducer<OrderBean, NullWritable, OrderBean, NullWritable>{
		 @Override
		//到达reduce时，相同id的所有bean已经被看成一组，且金额最大的那个一排在第一位
		protected void reduce(OrderBean bean,Iterable<NullWritable> value,Context context)throws IOException, InterruptedException {
			context.write(bean, NullWritable.get());
		}
	 }
	 
	 public static void main(String[] args) throws Exception {
		 Configuration conf=new Configuration();
		 Job job = Job.getInstance(conf);
		 
		 job.setJarByClass(SortMain.class);
		 
		 job.setMapperClass(SortMainMapper.class);
		 job.setReducerClass(SortMainReducer.class);
		 
		 job.setMapOutputKeyClass(OrderBean.class);
		 job.setMapOutputValueClass(NullWritable.class);
		 
		 job.setPartitionerClass(ItemIdPartitioner.class);
		 job.setGroupingComparatorClass(ItemGroupingComparator.class);
		 
		 job.setNumReduceTasks(2);
		 
		 FileInputFormat.setInputPaths(job, new Path("d:/wordcount/gpinput"));
		 FileOutputFormat.setOutputPath(job, new Path("d:/wordcount/gpoutput"));
		 
		 job.waitForCompletion(true);
	}
}
