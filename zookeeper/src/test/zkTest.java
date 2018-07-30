/**
 * @author xujie
 * @date: 2017年10月27日 下午3:56:04 
 */
package test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.xml.ws.BindingType;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Administrator
 *
 */
public class zkTest {
	
	private static final String servers="192.168.10.170:2181,192.168.10.171:2181,192.168.10.172:2181";
	private static final int sessionTimeout=2000;
	static ZooKeeper zkClient=null;
	
	/*	
		
		一由于连接还未完成就执行zookeeper的get/create/exsit操作,导致报KeeperErrorCode = ConnectionLoss的错

		解决方法:

		利用"CountDownLatch 类 + zookeeper的watcher + zookeeper的getStat" 实现连接完成后再调用.

		可防止此错误发生
	**/	
	public static void waitUntilConnected(ZooKeeper zooKeeper, CountDownLatch connectedLatch) {  
		//如果是在连接中，线程等待
        if (States.CONNECTING == zooKeeper.getState()) {  
            try {  
                connectedLatch.await();  
            } catch (InterruptedException e) {  
                throw new IllegalStateException(e);  
            }  
        }  
    }  
   
    static class ConnectedWatcher implements Watcher {  
   
        private CountDownLatch connectedLatch;  
   
        ConnectedWatcher(CountDownLatch connectedLatch) {  
            this.connectedLatch = connectedLatch;  
        }  
   
        @Override  
        public void process(WatchedEvent event) {  
        	//判断是否连接上
           if (event.getState() == KeeperState.SyncConnected) {  
        	   //计数器
               connectedLatch.countDown(); 
               System.out.println("监听");
				try {
					//重新获取子节点并注册监听
					zkClient.getChildren("/", true);
				} catch (Exception e) {
					// TODO: handle exception
				}
           }  
        }  
    }  
	
	@Before
	public void init()throws Exception  {
		CountDownLatch connectedLatch = new CountDownLatch(1);  
        Watcher watcher = new ConnectedWatcher(connectedLatch);  
		zkClient=new ZooKeeper(servers, sessionTimeout, watcher);
		waitUntilConnected(zkClient, connectedLatch); 
	}
	
	@Test
	public void createNode() throws Exception {
		
		//参数1：节点路径，参数2：节点数据，参数3：节点权限，参数4：节点类型（持久，临时。。）
		zkClient.create("/eclipse", "helloZk".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	}
	
	@Test
	public void getChildren() throws Exception {
		
		List<String> children = zkClient.getChildren("/", true);
		for (String child : children) {
			System.out.println(child);
		}
	}
	
	@Test
	public void isExist() throws Exception {
		
		Stat exists = zkClient.exists("/", false);
		System.out.println((exists == null) ? "not exist" : "exist");
	}
	
	@Test
	  public void getData()throws Exception{
		//参数3：默认是最新的数据
	    byte[] data = this.zkClient.getData("/eclipse", false, null);
	    System.out.println(new String(data));
	  }
	
	  @Test
	  public void deleteZnode()throws Exception{
		  //参数2：最新的数据
	    this.zkClient.delete("/eclipse", -1);
	  }
	
	  @Test
	  public void setData()throws Exception{
	    this.zkClient.setData("/app1", "阿斯弗".getBytes(), -1);
	
	    byte[] data = this.zkClient.getData("/app1", false, null);
	    System.out.println(new String(data));
	  }
}
