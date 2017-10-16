# hive-note
记录hive数据仓库建立过程

 使用sqoop链接oracle数据库，遍历所有数据库：
      sqoop list-databases --connect jdbc:oracle:thin:@10.32.151.110:1521:xiniu --username dw --password xkdw
      
      
 使用sqoop链接MySQL数据库，遍历所有数据库:(root用户和hadoop用户有不同的数据库)
      sqoop list-databases --connect jdbc:mysql://xslave3:3306 --username root --password neusoft 


 遍历出oracle中DW库中的所有数据表：
    sqoop list-tables --connect jdbc:oracle:thin:@10.32.151.110:1521:xiniu --username dw --password xkdw
    
    
 遍历出Oracle中ODS库中的说有数据表： 
    sqoop list-tables --connect jdbc:oracle:thin:@10.32.151.110:1521:xiniu --username dw --password xkdw
 
 
 遍历出（xslave3）MySQL中的数据表
    sqoop list-tables --connect jdbc:mysql://xslave3:3306/hive --username root --password neusoft
 
 
 设置导入到hdfs的根目录，设置map的并发数为4 如果不设置map并发数 默认的就是分四块 
    sqoop import --connect jdbc:mysql://xslave3:3306/hive --username root --password neusoft --table sds --warehouse-dir /zhao -m 4
 导入后HDFS的目录结构为 /zhao/sds 
 
 
 设置导入到HDFS的目标目录
     sqoop import --connect jdbc:mysql://xslave3:3306/hive --username root --password neusoft --table sds --target-dir /zhao/test -m 4 
 导入后HDFS的目录结构为 /zhao/test  （路径中不使用所导入的表名）
 
 
 
 导入查询后的结果集 通过sds.SD_ID分行 分成三个map并行计算
    sqoop import --connect jdbc:mysql://xslave3:3306/hive --username root --password neusoft --query 'SELECT sds.SD_ID,sds.CD_ID from sds wh    ere $CONDITIONS' --split-by sds.SD_ID --target-dir /zhao/queryTest2 -m 3


给字段值前后加上固定的字符 该例子中是加上双引号
   sqoop import --connect jdbc:mysql://xslave3:3306/hive --username root --password neusoft --query 'SELECT sds.SD_ID,sds.CD_ID from sds wh    ere $CONDITIONS' --split-by sds.SD_ID --enclosed-by '\"' --target-dir /zhao/queryTest3 -m 3 
   
   
通过hcatalog创建hive表
   hcat -e "use financials;create table hcatTest2(order_id int,order_name string) stored as rcfile;"
   
   
通过sqoop导入通过hcatalog创建的表
   sqoop import --connect jdbc:oracle:thin:@10.32.151.110:1521:xiniu --username ods --password xkods --query 'SELECT oo.order_id,oo.order_n    ame from ods.ch_app_order_info oo where $CONDITIONS' --hcatalog-database financials --hcatalog-table hcattest --split-by oo.order_id -m 3
   
   
通过hcatalog导入到hive表
   sqoop import --connect jdbc:mysql://10.32.144.143:3306/acornhc_healthdata --username hdw --password qingdw1220 --table phr_daily_test_pul    se_rate --hcatalog-storage-stanza 'stored as rcfile' --fields-terminated-by '\t' --hcatalog-database ods --hcatalog-table phr_daily_test_pu    lse_rate -m 5
~

# Socket简单理解
1、用于网络编程
2、分为服务器端和客户端

## 服务器端：

首先服务器方要先启动，并根据请求提供相应服务： 

1. 打开一通信通道并告知本地主机，它愿意在某一公认地址上（周知口，如FTP为21）接收客户请求； 

2. 等待客户请求到达该端口； 

3. 接收到重复服务请求，处理该请求并发送应答信号。接收到并发服务请求，要激活一新进程来处理这个客户请求（如UNIX系统中用fork、exec）。新进程处理此客户请求，并不需要对其它请求作出应答。服务完成后，关闭此新进程与客户的通信链路，并终止。 

4. 返回第二步，等待另一客户请求。 

5. 关闭服务器 

## 客户端： 
1. 打开一通信通道，并连接到服务器所在主机的特定端口； 

2. 向服务器发服务请求报文，等待并接收应答；继续提出请求...... 

3. 请求结束后关闭通信通道并终止。 

 ### 服务进程一般是先于客户请求而启动的。只要系统运行，该服务进程一直存在，直到正常或强迫终止。 

 ## Demo代码
 ```
 服务器端：
 public class TestServer extends Thread{
	private ServerSocket ss = null ;
	public TestServer(){
		try {
			ss = new ServerSocket(7456);
			System.out.println("server已经启动,开始建立监听");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void run(){
		while(true){
			Socket socket;
			try {
				socket = ss.accept();
				ServerThread st = new ServerThread(socket);
				st.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args ){
		new TestServer().start();
	}
}

public class ServerThread extends Thread {
	private Socket socket;
	public ServerThread(Socket socket){
		this.socket = socket;
	}
	public void run(){
		try {
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			PrintStream ps = new PrintStream(os);
			while(true){
				String temp = br.readLine();
				ps.println("服务器端消息："+temp);
				if(temp.equals("bye")){
					break;
				}
			}
			ps.close();
			br.close();
			socket.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}

客户端：
public class TestClient {
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Socket so = new Socket("127.0.0.1",7456);
		InputStream is = so.getInputStream();
		OutputStream os = so.getOutputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		PrintStream ps = new PrintStream(os);
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader key = new BufferedReader(isr);
		while(true){
			String temp = key.readLine();
			ps.println(temp);
			System.out.println(br.readLine());
			if(temp.equals("bye")){
				Thread.sleep(1000);
				break;
			}
		}
		key.close();
		ps.close();
		br.close();
		so.close();
	}
}
 ```



# YARN运行机制理解
 ![image](https://github.com/zhaoxuanhe/hive-note/blob/master/YARN.jpg)
 
 步骤：
 第一步：客户端向YARN平台提交应用程序，其中包括ApplicationMaster程序、启动命令脚本和用户程序等
 
 
 第二步：ResourceManager中的Scheduler为ApplicationMaster分配第一个Container，并且ApplicationsManager与对应的NodeManager通信，让NodeManager使ApplicationMaster在这个Container中运行。
 
 
 第三步：ApplicationMaster向ApplicationsManager注册自己，使ResourceManager能随时监控自己的状态。
 
 
 第四步：ApplicationMaster将任务拆分为多个内部任务，然后ApplicationMaster向Scheduler通信，为拆分后的Task申请资源和领取资源，ApplicationsManager与Scheduler通信以协调资源。
 
 
 第五步：ApplicationMast一旦申请得到资源后，便与NodeManager通信，申请启动运行任务
 
 
 第六步：NodeManager为拆分后的任务准备好运行环境，在申请得到的Container中通过脚本运行Task。
 
 
 第七步：各个Task通过RPC方式与ApplicationMaster通信，使ApplicationMaster可以随时知道各个任务的运行状态，从而可以在任务失败的时候发送命令重启任务。
 
 
 第八步：直到客户端提交的任务运行结束后，ApplicationMaster向ApplicationsManager注销并且关闭自己。
 
 
