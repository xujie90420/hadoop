package cn.xj.mapreduce.join;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 订单表和商品表合到一起
order.txt(订单id, 日期, 商品编号, 数量)
	1001	20150710	P0001	2
	1002	20150710	P0001	3
	1002	20150710	P0002	3
	1003	20150710	P0003	3
product.txt(商品编号, 商品名字, 价格, 数量)
	P0001	小米5	1001	2
	P0002	锤子T1	1000	3
	P0003	锤子	1002	4
 */

public class RJoin {
	
	static class RjoinMapper extends Mapper<LongWritable, Text, Text, InfoBean>{
		InfoBean bean=new InfoBean();
		Text text=new Text();
		
		@Override
		protected void map(LongWritable key, Text value,Context context)throws IOException, InterruptedException {
			String line=value.toString();
			//获取文件名
			FileSplit filesplit= (FileSplit) context.getInputSplit();
			String pid = "";
			String name = filesplit.getPath().getName();
			//根据pid分别建立bean归类在一起
			if(name.startsWith("order")){
				String[] fields = line.split("\t");
				// id date pid amount
				pid = fields[2];
				bean.set(Integer.parseInt(fields[0]), fields[1], pid, Integer.parseInt(fields[3]), "", 0, 0, "0");
			}else{
				String[] fields = line.split("\t");
				// id pname category_id price
				pid = fields[0];
				bean.set(0, "", pid, 0, fields[1], Integer.parseInt(fields[2]), Float.parseFloat(fields[3]), "1");
			}
			text.set(pid);
			context.write(text, bean);
		}
	}
	
	static class RjoinReducer extends Reducer<Text, InfoBean, InfoBean, NullWritable>{
		
		@Override
		protected void reduce(Text key, Iterable<InfoBean> beans,Context context)throws IOException, InterruptedException {
			InfoBean info=new InfoBean();
			List<InfoBean> list=new ArrayList<InfoBean>();
			//pid一样的所有bean
			for (InfoBean bean : beans) {
				if("1".equals(bean.getFlag())){//同pid，同一个产品信息
					try {
						BeanUtils.copyProperties(info,bean);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else{ //订单  多个，添加到list中
					InfoBean newBean=new InfoBean();
					try {
						BeanUtils.copyProperties(newBean,bean);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					list.add(newBean);
				}
			}
			//订单bean遍历拼接商品信息，进行合并拼成完整的bean输出
			for (InfoBean bean : list) {
				bean.setPname(info.getPname());
				bean.setCategory_id(info.getCategory_id());
				bean.setPrice(info.getPrice());
				context.write(bean, NullWritable.get());
			}
		}
		
	}
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		
		conf.set("mapred.textoutputformat.separator", "\t");
		
		Job job = Job.getInstance(conf);

		// 指定本程序的jar包所在的本地路径
		// job.setJarByClass(RJoin.class);
//		job.setJar("c:/join.jar");

		job.setJarByClass(RJoin.class);
		// 指定本业务job要使用的mapper/Reducer业务类
		job.setMapperClass(RjoinMapper.class);
		job.setReducerClass(RjoinReducer.class);

		// 指定mapper输出数据的kv类型
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(InfoBean.class);

		// 指定最终输出的数据的kv类型
		job.setOutputKeyClass(InfoBean.class);
		job.setOutputValueClass(NullWritable.class);

		// 指定job的输入原始文件所在目录
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		// 指定job的输出结果所在目录
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		// 将job中配置的相关参数，以及job所用的java类所在的jar包，提交给yarn去运行
		/* job.submit(); */
		boolean res = job.waitForCompletion(true);
		System.exit(res ? 0 : 1);

	}
}
