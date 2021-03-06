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
![image](https://github.com/zhaoxuanhe/hive-note/blob/master/picture/Reactor.png)



# 2 YARN运行机制理解
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
 
 # 3 YARN基础库
 
 ## 3.1 Protocol Buffers
 > Protocol Buffers是Google开源的序列化库，是一种轻便高效的结构化数据存储格式，可以用于结构化数据序列化/反序列化。很适合做数据存储和或者RPC的数据交换格式，通常用作通信协议、数据存储等领域的与预言无关、平台无关、可扩展性的序列化结构数据格式；以.proto作为扩展名;生成java类</br>
 > C++ 、Java  、Python
 >>>	* 平台无关性、预言无关性
 >>>	* 高性能，解析速度是XML的20～100倍
 >>>	* 体积小，文件大小仅是XML的1/10 ～ 1/3
 >>>	* 使用简单
 >>>	* 兼容性好
 #### YARN中所有的RPC写协议均采用Protocol Buffers定义
 	* applicationmaster_protocol.proto:定义了AM与RM之间的协议——————ApplicationMasterProtocol
	* applicationclient_protocol.proto:定义了JobClient（作业提交客户端）与RM之间的协议——————ApplicationClientProtocol
	* containermanagement_protocol.proto:定义了AM与NM之间的协议——————ContainerManagementProtocol
	* resourcemanager_administration_protocol.proto:定义了Admin（管理员）与RM之间的通信协议——————		    ResourceMangerAdministrationProtocol
	* yarn_protos.proto:定义了各个协议RPC的参数
	* ResourceTracker.proto:定义了NM与RM之间的协议——————ResourceTracker
	* MRClientProtocol.proto：定义了JobClient（作业提交客户端）与MRAppMaster之间的协议——————MRClientProtocol
	* mr_protos.proto:定义了MRClientProtocol协议的各个参数
## 3.2 Apache Avro
#### Apache Avro本身既是一个序列化框架，同时也实现了RPC的功能。
	* 丰富的数据结构类型
	* 快速可压缩的二进制数据格式
	* 存储持久数据的文件容器
	* 提供远程过程调用RPC
	* 简单的动态语言结合功能
	* 支持动态模式，Avro不需要生成代码，有利于搭建通用的数据处理系统，同时避免了代码入侵
	* 数据无需加标签，减少序列化后数据的大小
	* 无需手工分配的域标识
#### Avro作为日志序列化库使用；在YARN MapReduce中，所有时间的序列化/反序列化均采用Avro完成
## 3.3 底层通信库
网络通信模块是分布式系统中最底层的模块，它直接支撑了上层分布式环境下复杂的进程间通信（Inter-Process Communication，IPC）逻辑。远程过程调用（Remote Procedure Call，RPC）是一种常用的分布式网络通信协议，它允许运行于一台计算机的程序调用另一台计算机的子程序，同时将网络的通信细节隐藏起来，使得用户无需额外的为这个交互作用编程。
### PRC通信模型
1、通信模块</br>
两个相互协作的通信模块实现请求-应答协议，在传递消息的时候，一般不会对数据包进行任何处理。请求-应答协议分为同步方式和异步方式</br>


![image](https://github.com/zhaoxuanhe/hive-note/blob/master/picture/CommunicationModule.png)



2、Stub程序</br>
客户端和服务器都包含Stub程序，可看成代理程序。它使得远程函数调用表现得跟本地调用一样，对用户程序完全透明。在客户端看来，它就是一个本地程序，但是Stub不执行本地的调用，而是将请求通过通信模块发送给服务器端</br>
3、调度程序</br>
调度程序接收来自通信模块的请求消息，并根据其中的标识选择一个Stub程序进行处理。通常客户端并发请求量比较大的时候，会采用线程池提高处理效率</br>
4、客户程序/服务过程</br>
请求的发出者和请求的处理者</br>
#### RPC基本的处理流程

![image](https://github.com/zhaoxuanhe/hive-note/blob/master/picture/RPCModel.png)


1）客户端以本地方式调用系统产生的Stub程序</br>
2）该Stub程序将函数调用信息按照网络通信模块的要求封装成消息包，并交给通信模块发送到远程服务器端</br>
3）远程服务器端接收此消息后，将此消息发送给相应的Stub程序</br>
4）Stub程序拆封消息，形成被调过程要求的形式，并调用相应的程序</br>
5）被调用函数按照所获参数执行，并将结果返回给Stub程序
6）Stub程序将此结果封装成消息，通过网络通信模块逐级地传送给客户程序

### Hadoop RPC总体架构
Hadoop RPC主要分为四个部分，分别是序列化层、函数调用层、网络传输层和服务器端处理框架。</br>

序列化层：Protocol Buffers和Apache Avro均可用在序列化层</br>

函数调用层：函数调用层主要功能是定位要调用的函数并执行该函数，Hadoop RPC采用了Java反射机制与动态代理实现了函数调用</br>

网络传输层：Hadoop RPC的网络传输层采用了基于TCP/IP的Socket机制进行Client与Server之间的消息传输</br>

服务器端处理框架：可以被抽象为网络I/O模型，Hadoop RPC采用了基于Reactor涉及模式的事件驱动I/O模型</br>

### Hadoop RPC类详解


Hadoop RPC使用可以分为四个步骤:</br>
1)定义RPC协议,也就是定义客户端和服务器端的通信接口,定义了服务器端对外提供的服务接口.</br>
2)实现RPC协议,通常情况下是一个Java接口,用户需要实现该接口.</br>
3)构造并启动RPC Server,使用RPC.java中的一个静态类Builder构造一个RPC Server,并通过start()启动该Server</br>
4)构造RPC Client并发送RPC请求,使用getProxy()构造客户端代理对象.直接通过代理对象调用远端的方法.

#### ipc.RPC类分析

![image](https://github.com/zhaoxuanhe/hive-note/blob/master/picture/RPC.png)

Hadoop RPC远程方法调用的流程:对于Hadoop RPC,函数调用由客户端发起,并在服务器端执行并返回,因此不能像单机程序那样直接在invoke方法中本地调用相关函数,具体做法是在invoke方法中,将函数调用信息(函数名,函数参数列表等)打包成可序列化的对象,并通过网络发送给服务器端,服务器端收到该调用信息后,解析出函数名,函数参数列表等信息后,利用java反射机制完成函数调用.</br>

#### ipc.Client

![image](https://github.com/zhaoxuanhe/hive-note/blob/master/picture/RPC-Client.png)

Client内部有两个重要的内部类,分别是Call和Connection

1)Call类:封装了一个RPC请求,包含唯一标识,重试次数,rpc请求的序列化对象,rpc响应的序列化对象,异常信息,prc框架类型,请求是否结束,外部的handler.由于Hadoop RPC Server采用异步方式处理客户端请求,这使远程过程调用的发生顺序与结果返回顺序无直接关系.Client通过唯一标识来识别不同的函数调用.</br>

2)Connection类:Client与每个Server之间维护一个通信连接,与该连接相关的基本信息及操作被封装到Connection类中,包括唯一标示id,与Server端通信的Socket,保存RPC请求的hash表</br>

![image](https://github.com/zhaoxuanhe/hive-note/blob/master/picture/RPC-Client-Connection.png)

当调用call函数执行某个远程方法时,Client端需要进行以下四个步骤:</br>
1)创建一个Connection对象,并将远程方法调用信息封装成Call对象,放到Connection对象中的哈希表中.</br>
2)调用Connection类中的sendRpcRequest()方法将当前Call对象发送给Server端;</br>
3)Server端处理完RPC请求后,将结果通过网络返回给Client端,Client端通过receiveRpcResponse()函数获取结果</br>
4)Client检查结果处理状态(成功还是失败),并将对应Call对象从哈希表中删除．</br>

#### ipc.Server

Hadoop RPC Server通过Reactor实现设计模式提高的整体性能，ipc.Server分为三个阶段：接收请求，处理请求和返回结果</br>

![image](https://github.com/zhaoxuanhe/hive-note/blob/master/picture/Hadoop-RPC-Server.png)

1）接收请求</br>
接收来自各个客户端的RPC请求，并将他们封装成固定的格式（Call类）放到共享队列callQueue中，该阶段的连接阶段由Listener线程完成；接收请求由Reader线程完成。整个Server只有一个Listener线程，通过轮询的方式从线程池中选择一个Reader线程来处理客户端最新到达的请求。Listener通过简单的轮询分配机制决定每个Reader负责哪些客户端连接。Reader线程负责监听它所负责的客户端连接中是否有新的RPC请求到达，并将他们封装成Call对象。</br>

2）处理请求</br>
Server端可同时存在多个Handler线程，Handler线程可以并行的从callQuery共享队列中读取Call对象，执行对应的函数调用后，并且考虑到某些函数调用返回结果很大或者网速过慢，很难将结果一次性发送给客户端，因此，Handler将后续发送任务交给Responder线程。</br>

3）返回结果</br>
Server端仅存在一个Responder建成，她的内部包含一个Selector对象，用于监听SelectionKey.OP_WRITE事件，Responder线程采用异步方式继续完成发送结果。</br>

### YARN RPC实现

Hadoop采用java编写，因而hadoop RPC的客户端可服务器端仅支持java语言，市场上一些其他的PRC框架还支持C++、Python、java等，这给用户编程带来了极大的不便。并且版本不同的hadoop之间不能通信。</br>
Hadoop YARN将RPC中的序列化部分剥离出来，以便将现有的开源RPC框架集成进来。经过改进以后，RPC类变成了一个工厂，他将具体的RPC实现授权给RPCEngine实现类，而现有的开源RPC只要实现RPC接口，便可以集成到Hadoop RPC中。</br>

#### YarnRPC相关实现类

![image](https://github.com/zhaoxuanhe/hive-note/blob/master/picture/YarnRPC.png)

#### YarnRPC应用实例

第一步：定义通信协议接口ResourceTracker，包含registerNodeManager()和nodeHeartbeat()</br>
第二步：为通信协议ResourceTracker提供Protocol Buffers定义（ResourceTracker.proto）及java实现（ResourceTrackerService.java）</br>
第三步：为RPC函数的参数和返回值提供Protocol Buffers定义（yarn_server_common_service_protos.proto）。</br>
第四步：为RPC函数的参数和返回值提供Java定义和封装。
第五步：为通信协议提供客户端和服务器端实现。

### 服务库

对于生命周期较长的对象，YARN采用了基于服务的对象管理模型对其进行管理</br>
1)每个被服务化的对象分为NOTINITED(被创建)、INITED(已初始化)、STARTED(已启动)、STOPPED(已停止)</br>
2)任何服务状态变化都可以触发另外一些动作。</br>
3)可通过组合的方式对任意服务进行组合，以便进行统一管理。</br>
#### 如果是非组合服务，直接继承AbstractService类；若是组合服务，则需要继承CompositeService类

### 事件库

YARN采用了基于事件驱动的并发模型，大大增强了系统的并发性，从而提高系统的整体性能。

![image](https://github.com/zhaoxuanhe/hive-note/blob/master/picture/Yarn-EventLibrary.png)

## 4.1 YARN应用程序设计方法

应用程序(Application)是用户编写的车里数据的程序的统称。YARN自身对应用程序的类型没有任何限制，可以是处理短类型任务的MapReduce作业，也可以是不熟长时间运行的服务的应用程序，比如Storm。YARN应用程序编写比较复杂，例如，专业人员实现可以直接运行在YARN之上的MapReduce框架库（假设打包后为yarn-mapreduce.jar，主要完成数据切分、资源申请、任务调度与容错、网络通信等功能）。而普通用户只需要编写map()和reduce()两个函数完成MapReduce程序设计（假设打包后为my-app.jar，主要完成自己计算所需的逻辑）。这样用户提交应用程序时，YARN会自动将yarn-mapreduce.jar和my-app.jar两个JAR包同时提交到YARN之上，以完成一个分布式应用的计算。该部分重点介绍如何编写一个运行在YARN之上的框架。</br>
    如果想要将一个新的应用程序运行在YARN之上，通常需要编写两个组件Client和ApplicationMaster。其中，客户端负责向ResourceMaster提交ApplicationMaster，并查询应用程序运行状态；ApplicationMaster负责向ResourceManager申请资源(以Container形式表示)，并与NodeManager通信以启动各个Container，此外，ApplicationMaster还负责监控各个任务的运行状态，并在失败的时候为其重新申请资源。</br>

 
![image](https://github.com/zhaoxuanhe/hive-note/blob/master/picture/CommunicationProtocol.png)

1)ApplicationClientProtocol(用于Client与ResourceManager之间)。Client通过该协议可实现将应用程序提交到ResourceManager上、查询应用程序的运行状态或者杀死应用程序等功能。</br>
2）ApplicationMasterProtocol(用于ApplicationMaster与ResourceManager之间)。ApplicationMaster使用该协议向ResourceManager注册、申请资源、获取各个任务运行情况等。</br>
3)ContainerManagerProtocol(用于ApplicationMaster与NodeManager之间)。ApplicationMaster使用该协议要求NodeManager启动/撤销Container或者查询Container的运行状态。

## 4.2 客户端设计

步骤1：Client通过RPC函数ApplicationClientProtocol#getNewApplication从ResourceManager中获取唯一的applicationID和最大可申请资源量。</br>
步骤2：Client通过RPC函数ApplicationClientProtocol#submitApplication将ApplicationMaster提交到ResourceManager上。客户端将启动ApplicationMaster所需的所欲信息打包到数据结构ApplicationSubmissionContext中</br>
 #### *注:ApplicationClientProtocol部分RPC函数主要用于客户端与ResourceManager之间的通信，这一部分对所有类型的应用程序来说都是一致的，故可以做成通用代码。但在实际应用环境中，为了减轻ResourceManager的负载，一旦应用程序的ApplicationMaster成功启动后，客户端通常直接与ApplicationMaster通信，以查询它的运行状态或者控制它的执行流程
 

因为不同类型的应用程序与ResourceManager交互逻辑是类似的，为了避免客户端的重复开发，YARN提供了能有ResourceManager交互完成各种操作的编程库org.apache.hadoop.yarn.client.YarnClient。该库对常用的函数进行了封装，并提供了容错、重试等容错机制。用户使用该库可以快速开发一个报刊应用程序提交、状态查询和控制等逻辑YARN客户端。（hadoop-yarn-client/src/main/java/org.apache.hadoop.yarn.client/api/YarnClient.java）
 
 
## 4.3 ApplicationMaster设计
ApplicationMaster(AM)需要与ResourceManager(RM)和NodeManager(NM)两个服务交互，

 








