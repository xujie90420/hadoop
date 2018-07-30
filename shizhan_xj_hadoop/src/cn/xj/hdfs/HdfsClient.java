package cn.xj.hdfs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.junit.Before;
import org.junit.Test;

public class HdfsClient {
	FileSystem fs=null;
	
	@Before
	public void init() throws Exception{
		Configuration conf = new Configuration();
		fs=FileSystem.get(new URI("hdfs://192.168.10.170:9000"), conf, "root");
	}
	
	@Test
	public void upload() throws Exception{
		fs.copyFromLocalFile(new Path("c:/oem8.log"), new Path("/oem8.log.copy"));
		fs.close();
	}
	
	@Test
	public void delete() throws Exception{
		fs.delete(new Path("/oem8.log.copy"), true);
		fs.close();
	}
	
	@Test
	public void mkdir() throws Exception{
		boolean flag = fs.mkdirs(new Path("/test"));
		System.out.println(flag);
	}
	
	@Test
	public void listFile() throws Exception{
		RemoteIterator<LocatedFileStatus> listFiles = fs.listFiles(new Path("/"), true);
		while(listFiles.hasNext()){
			LocatedFileStatus fileStatus = listFiles.next();
			System.out.println("blocksize: " +fileStatus.getBlockSize());
			System.out.println("owner: " +fileStatus.getOwner());
			System.out.println("Replication: " +fileStatus.getReplication());
			System.out.println("Permission: " +fileStatus.getPermission());
			System.out.println("Name: " +fileStatus.getPath().getName());
			System.out.println("------------------");
			BlockLocation[] blockLocations = fileStatus.getBlockLocations();
			for(BlockLocation b:blockLocations){
				System.out.println("块起始偏移量: " +b.getOffset());
				System.out.println("块长度:" + b.getLength());
				//块所在的datanode节点
				String[] datanodes = b.getHosts();
				for(String dn:datanodes){
					System.out.println("datanode:" + dn);
				}
			}
		}
	}
	
	@Test
	public void testLs2() throws Exception {
		
		FileStatus[] listStatus = fs.listStatus(new Path("/"));
		for(FileStatus file :listStatus){
			
			System.out.println("name: " + file.getPath().getName());
			System.out.println((file.isFile()?"file":"directory"));
			
		}
		
	}
	
	/**
	 * 通过流的方式上传文件到hdfs
	 * @throws Exception
	 */
	@Test
	public void testUpload() throws Exception {
		
		FSDataOutputStream outputStream = fs.create(new Path("/angelababy.love"), true);
		FileInputStream inputStream = new FileInputStream("c:/angelababy.love");
		
		IOUtils.copy(inputStream, outputStream);
		
	}
	
	
	/**
	 * 通过流的方式获取hdfs上数据
	 * @throws Exception
	 */
	@Test
	public void testDownLoad() throws Exception {
		
		FSDataInputStream inputStream = fs.open(new Path("/angelababy.love"));		
		
		FileOutputStream outputStream = new FileOutputStream("d:/angelababy.love");
		
		IOUtils.copy(inputStream, outputStream);
		
	}
	
	
	@Test
	public void testRandomAccess() throws Exception{
		
		FSDataInputStream inputStream = fs.open(new Path("/angelababy.love"));
	
		inputStream.seek(12);
		
		FileOutputStream outputStream = new FileOutputStream("d:/angelababy.love.part2");
		
		IOUtils.copy(inputStream, outputStream);
		
		
	}
	
	
	
	/**
	 * 显示hdfs上文件的内容
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testCat() throws IllegalArgumentException, IOException{
		
		FSDataInputStream in = fs.open(new Path("/angelababy.love"));
		
		IOUtils.copy(in, System.out);
		
//		IOUtils.copyBytes(in, System.out, 1024);
	}
	
}
