package bigdata.mr;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

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
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import bigdata.bean.WebLogBean;

/**
 * 
 * 将清洗之后的日志梳理出点击流pageviews模型数据
 * 
 * 输入数据是清洗过后的结果数据
 * 
 * 区分出每一次会话，给每一次visit（session）增加了session-id（随机uuid）
 * 梳理出每一次会话中所访问的每个页面（请求时间，url，停留时长，以及该页面在这次session中的序号）
 * 保留referral_url，body_bytes_send，useragent
 * 
 * 
 * @author
 * 
 */

public class PageViews {
	
	static class PageViewMapper extends Mapper<LongWritable, Text, Text, WebLogBean>{
		Text k = new Text();
		WebLogBean v = new WebLogBean();

		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

			String line = value.toString();

			String[] fields = line.split("\001");
			if (fields.length < 9){
				return;
			}
			//将切分出来的各字段set到weblogbean中
			v.set("true".equals(fields[0]) ? true : false, fields[1], fields[2], fields[3], fields[4], fields[5], fields[6], fields[7], fields[8]);
			//只有有效记录才进入后续处理
			if (v.isValid()) {
				k.set(v.getRemote_addr());
				context.write(k, v);
			}
		}
	}
	
	static class PageViewReducer extends Reducer<Text, WebLogBean, NullWritable, Text>{
		NullWritable k=NullWritable.get();
		Text v=new Text();
		@Override
		protected void reduce(Text key, Iterable<WebLogBean> values,Context context) throws IOException, InterruptedException {
			try {
				int step=1;
				String session = UUID.randomUUID().toString();
				ArrayList<WebLogBean> beans = new ArrayList<WebLogBean>();
	
				// 先将一个用户的所有访问记录中的时间拿出来排序
				for (WebLogBean bean : values) {
					WebLogBean webLogBean = new WebLogBean();
					try {
						BeanUtils.copyProperties(webLogBean, bean);
					} catch(Exception e) {
						e.printStackTrace();
					}
					beans.add(webLogBean);
				}
				
				//将bean按时间先后顺序排序
				Collections.sort(beans, new Comparator<WebLogBean>() {

					@Override
					public int compare(WebLogBean o1, WebLogBean o2) {
						try {
							Date d1 = toDate(o1.getTime_local());
							Date d2 = toDate(o2.getTime_local());
							if (d1 == null || d2 == null)
								return 0;
							return d1.compareTo(d2);
						} catch (Exception e) {
							e.printStackTrace();
							return 0;
						}
					}

				});
				for (int i = 0; i < beans.size(); i++) {
					WebLogBean bean = beans.get(i);
					//说明只有一条数据
					if(1==beans.size()){
						// 设置默认停留市场为60s
						v.set(session+"\001"+key.toString()+"\001"+bean.getRemote_user() + "\001" + bean.getTime_local() + "\001" + bean.getRequest() + "\001" + step + "\001" + (60) + "\001" + bean.getHttp_referer() + "\001" + bean.getHttp_user_agent() + "\001" + bean.getBody_bytes_sent() + "\001"
								+ bean.getStatus());
						context.write(k, v);
						session = UUID.randomUUID().toString();
						break;
					}
					//多条数据的时候，第一条数据不做处理
					if(i==0){
						continue;
					}
					
					long timeDiff=timeDiff(bean.getTime_local(),beans.get(i-1).getTime_local());
					//间隔时间超过30分钟，默认停留时间为60分钟
					if(timeDiff>30*60*1000){
						v.set(session+"\001"+key.toString()+"\001"+bean.getRemote_user() + "\001" + bean.getTime_local() + "\001" + bean.getRequest() + "\001" + step + "\001" + (60) + "\001" + bean.getHttp_referer() + "\001" + bean.getHttp_user_agent() + "\001" + bean.getBody_bytes_sent() + "\001"
								+ bean.getStatus());
						context.write(k, v);
						session = UUID.randomUUID().toString();
						step=1;
						break;
					}else{
						//间隔时间不超过30分钟，记录步骤
						v.set(session+"\001"+key.toString()+"\001"+bean.getRemote_user() + "\001" + bean.getTime_local() + "\001" + bean.getRequest() + "\001" + step + "\001" + timeDiff + "\001" + bean.getHttp_referer() + "\001" + bean.getHttp_user_agent() + "\001" + bean.getBody_bytes_sent() + "\001"
								+ bean.getStatus());
						context.write(k, v);
						step++;
					}
					//最后一次默认停留时间为60s
					if(i==beans.size()-1){
						v.set(session+"\001"+key.toString()+"\001"+bean.getRemote_user() + "\001" + bean.getTime_local() + "\001" + bean.getRequest() + "\001" + step + "\001" + (60) + "\001" + bean.getHttp_referer() + "\001" + bean.getHttp_user_agent() + "\001" + bean.getBody_bytes_sent() + "\001"
								+ bean.getStatus());
						context.write(k, v);
					}
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		private Date toDate(String timeStr) throws ParseException {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
			return df.parse(timeStr);
		}

		private long timeDiff(String time1, String time2) throws ParseException {
			Date d1 = toDate(time1);
			Date d2 = toDate(time2);
			return d1.getTime() - d2.getTime();

		}

	}
	
	public static void main(String[] args) throws Exception {
		Configuration conf =new Configuration();
		Job job = Job.getInstance(conf);
		
		job.setJarByClass(PageViews.class);
		
		job.setMapperClass(PageViewMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(WebLogBean.class);
		
		job.setReducerClass(PageViewReducer.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);
		
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job,new Path(args[1]));
		/*FileInputFormat.setInputPaths(job, new Path("f:/weblog/Weblogoutput"));
		FileOutputFormat.setOutputPath(job, new Path("f:/weblog/PageViewoutput"));*/
		
		job.waitForCompletion(true);
	}
}
