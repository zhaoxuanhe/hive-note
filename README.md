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
 
 
