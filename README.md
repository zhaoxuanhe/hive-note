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

# Java反射机制
JAVA反射机制是在运行状态中，对于任意一个类，都能够知道这个类的所有属性和方法；对于任意一个对象，都能够调用它的任意方法和属性；这种动态获取信息以及动态调用对象方法的功能称为java语言的反射机制。

## 代码
```
public class Heros {
	private String name ;
	private String type ;
	private int camp ;
	public Heros(){}
	public String toString(){
		return "Heros [\n name = "+ name +",\n type = "+ type +", \n camp = "+ camp + "\n]";
	}
}

public class ReflectDemo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Class<?> c = Heros.class;
		try {
//			Class c = Class.forName("Heros");		
			//用反射机制创建一个对象的实例 相当于new 但是与new不同的地方是该方法不需要知道所声明的对象的类型或者构造方法
			Object object = c.newInstance();		
			//返回一个Field类型的对象，该对象包括由反射生成的对象c的类声明的属性和方法
			Field[] fields = c.getDeclaredFields();
			System.out.println("Heros所有属性： ");
			for(Field f : fields){
				System.out.println(f);
			}
			
			Field field = c.getDeclaredField("name");
			
			field.setAccessible(true);
			field.set(object, "炸弹人");
			System.out.println("修改后的属性值");
			System.out.println(field.get(object));
			System.out.println("修改属性后的Heros:");
			System.out.println((Heros)object);
		} catch (Exception e) {
	
		}
	}
	}

```

# Reactor设计模式
## 从前有座山 山里有座庙 庙里没有老和尚
庙里老王开了一家餐馆，每一个人来就餐就是一个事件，客人会先看一下菜单，然后点菜。</br>

客人老张来了，服务员一为他服务，看菜单，点菜，就餐</br>
客人老李来了，服务员二为他服务，看菜单，点菜，就餐</br>
客人老孙来了，服务员三为他服务，看菜单，点菜，就餐</br>
期初庙里老王的餐馆就有三个服务员，来三个人吃饭，每个人都能享受VIP待遇，庙里老王觉得很不错，店里的客人享受周到的服务，口碑相传。</br>
27号是个大喜的日子，不是因为我们单位发工资，而是因为庙里老王的店里今天来了5位客人。这下没有足够的服务员进行一对一VIP服务了，庙里老王就又招聘了2名服务员以保证可能能享受到尊贵的服务。庙里老王的店名声越来越大，客人越来越多。</br>
有一天，店里一下来了10个人，这下庙里老王脑瓜就迷糊了，开心不起来。若是继续再请服务员，店里就基本不赚钱，要是不请服务员，又有5个客人没人接待。庙里老王想了想，5个服务员对付10个客人也是能对付过来的，服务员勤快点就好了，伺候完一个客人马上伺候另外一个，还是来得及的。综合考虑了一下，庙里老王决定就使用5个服务人员的线程池啦</br>
但是这样有一个比较严重的缺点就是，如果正在接受服务员服务的客人点菜很慢，其他的客人可能就要等好长时间了。有些火爆脾气的客人可能就等不了走人了。</br>
庙里老王后来发现，客人点菜比较慢，大部服务员都在等着客人点菜，其实干的活不是太多。庙里老王能当老板当然有点不一样的地方，终于发现了一个新的方法，那就是：当客人点菜的时候，服务员就可以去招呼其他客人了，等客人点好了菜，直接招呼一声“服务员”，马上就有个服务员过去服务。嘿嘿，然后在老板有了这个新的方法之后，就进行了一次裁员，只留了一个服务员！这就是用单个线程来做多线程的事。
## 模式示意图
![image](https://github.com/zhaoxuanhe/hive-note/blob/master/YARN.jpg)



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
 
 
