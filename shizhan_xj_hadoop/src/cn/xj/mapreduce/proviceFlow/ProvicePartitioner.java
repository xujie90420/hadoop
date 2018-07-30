package cn.xj.mapreduce.proviceFlow;

import java.util.HashMap;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class ProvicePartitioner extends Partitioner<Text, FlowBean>{
	
	public static HashMap<String,Integer> proviceDict=new HashMap<String,Integer>();
	
	static {
		proviceDict.put("136", 0);
		proviceDict.put("137", 1);
		proviceDict.put("138", 2);
		proviceDict.put("139", 3);
	}

	@Override
	public int getPartition(Text key, FlowBean value, int partitionerNum) {
		String prefix = key.toString().substring(0, 3);
		Integer num = proviceDict.get(prefix);
		
		return num==null?num:4;
	}

}
