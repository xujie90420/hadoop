package cn.xj.mapreduce.flowsum;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.mockito.internal.matchers.CompareTo;

public class FlowBean implements WritableComparable<FlowBean>{
	
	private Integer upflow;
	private Integer dflow;
	private Integer sumflow;
	
	public FlowBean(){}
	

	public FlowBean(Integer upflow, Integer dflow) {
		this.upflow = upflow;
		this.dflow = dflow;
		this.sumflow = upflow+dflow;
	}

	public void set(Integer upflow, Integer dflow){
		this.upflow = upflow;
		this.dflow = dflow;
		this.sumflow = upflow+dflow;
	}

	public Integer getUpflow() {
		return upflow;
	}



	public void setUpflow(Integer upflow) {
		this.upflow = upflow;
	}



	public Integer getDflow() {
		return dflow;
	}



	public void setDflow(Integer dflow) {
		this.dflow = dflow;
	}



	public Integer getSumflow() {
		return sumflow;
	}



	public void setSumflow(Integer sumflow) {
		this.sumflow = sumflow;
	}



	@Override
	public void readFields(DataInput in) throws IOException {
		this.upflow=in.readInt();
		this.dflow=in.readInt();
		this.sumflow=in.readInt();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(upflow);
		out.writeInt(dflow);
		out.writeInt(sumflow);
		
	}


	@Override
	public int compareTo(FlowBean o) {
		return this.sumflow>o.getSumflow()?1:-1;
	}
	
	@Override
	public String toString() {
		 
		return upflow + "\t" + dflow + "\t" + sumflow;
	}

}
