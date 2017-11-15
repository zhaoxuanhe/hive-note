package com.protobufDemo.business;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.protobuf.BlockingService;
import com.protobufDemo.api.Calculator;
import com.protobufDemo.ipc.Server;

public class CalculatorService implements Calculator {
	private Server server = null;  
    private final Class calculatorProtocol = Calculator.class;  
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();  
    private final String protoPackage = "com.protobufDemo.protogen";  
    private final String host = "localhost";  
    private final int port = 7456;  
      
    public CalculatorService (){  
          
    }  
	public int add(int a, int b) {
		// TODO Auto-generated method stub
		return a+b;
	}

	public int minus(int a, int b) {
		// TODO Auto-generated method stub
		return a-b;
	}
	public void init(){  
        createServer();          
    }  
      
      
    /* 
     * return com.protobufDemo.api.CalculatorPBServiceImpl
     * 包含构造方法、add、minus三个方法 
     */  
    public Class<?> getPbServiceImplClass(){  
    	//calculatorProtocol是proto生成的class，calculatorProtocol是Calculator类的一个描述类
    	//packageName = com.protobufDemo.api
        String packageName = calculatorProtocol.getPackage().getName();  
        System.out.println(packageName);
//      String packageName2 = Calculator.class.getPackage().getName();
//    	System.out.println(packageName2);    	
        String className = calculatorProtocol.getSimpleName();  
        //找到CalculatorPBServiceImpl.java
        String pbServiceImplName =  packageName + "." + className +  "PBServiceImpl";          
        Class<?> clazz = null;  
        try{  
            clazz = Class.forName(pbServiceImplName, true, classLoader);  
        }catch(ClassNotFoundException e){  
            System.err.println(e.toString());  
        }  
        return clazz;  
    }  
      
    /* 
     * return org.tao.pbtest.proto.Calculator$CalculatorService 
     */  
    public Class<?> getProtoClass(){  
    	//返回基础类的简单名称
        String className = calculatorProtocol.getSimpleName();  
        
        System.out.println("className"+className);
        //com.protobufDemo.protogen.Calculator.java是通过protobuf编译后生成的类，其中CalculatorService是自动生成的方法
        String protoClazzName =  protoPackage + "." + className + "$" + className + "Service";  
        System.out.println(protoClazzName);
        Class<?> clazz = null;  
        try{  
        	//初始化这个CalculatorService类后返回该类
            clazz = Class.forName(protoClazzName, true, classLoader);  
        }catch(ClassNotFoundException e){  
            System.err.println(e.toString());  
        }  
        return clazz;  
    }  
      
    public void createServer(){  
    	//返回CalculatorPBServiceImpl类
        Class<?> pbServiceImpl = getPbServiceImplClass(); 
        Constructor<?> constructor = null;  
        try{ 
        	//返回构造函数,CalculatorPBServiceImpl的public构造函数需要一个Calculator类型的参数
            constructor = pbServiceImpl.getConstructor(calculatorProtocol); 
            //反射的对象在使用时应该取消java语言访问检查
            constructor.setAccessible(true);  
        }catch(NoSuchMethodException e){  
            System.err.print(e.toString());  
        }  
          
        Object service = null;  // instance of CalculatorPBServiceImpl  
        try {  
            service = constructor.newInstance(this);  
        }catch(InstantiationException e){  
        } catch (IllegalArgumentException e) {  
        } catch (IllegalAccessException e) {  
        } catch (InvocationTargetException e) {  
        }  
       
        
        
        System.out.println("aaa  "+service.getClass().getName());
        /* 
         * interface: com.protobufDemo.api.CalculatorPB 
         */  
        //得到CalculatorPBServiceImpl所实现的接口,getInterfaces()[0]就是返回实现的第一个接口（CalculatorPB）
        Class<?> pbProtocol = service.getClass().getInterfaces()[0];  
                  
        /* 
         * 返回com.protobufDemo.protogen.Calculator$CalculatorService 
         */  
        /*------------------------------------------分割线-----------------------------------------------------------------*/
        //protoClazz是protobuf生成的Calculator类中的内部类CalculatorService
        Class<?> protoClazz = getProtoClass();  
          
        Method method = null;  
        try {  
  
            // pbProtocol.getInterfaces()[] 即是接口 com.protobufDemo.protogen.Calculator$CalculatorService$BlockingInterface  
        	Class<?> b = pbProtocol.getInterfaces()[0];
        	System.out.println("bb  "+b.getName());
        	
        	//pbProtocol.getInterfaces()[0]是BlockingInterface。第一个参数是方法名，第二个参数是形参
            method = protoClazz.getMethod("newReflectiveBlockingService", pbProtocol.getInterfaces()[0]);  
            method.setAccessible(true);  
        }catch(NoSuchMethodException e){  
            System.err.print(e.toString());  
        }  
          
        try{  
            createServer(pbProtocol, (BlockingService)method.invoke(null, service));  
        }catch(InvocationTargetException e){  
        } catch (IllegalArgumentException e) {  
        } catch (IllegalAccessException e) {  
        }  
          
    }  
      
    public void createServer(Class pbProtocol, BlockingService service){  
        server = new Server(pbProtocol, service, port);  
        server.start();  
    }  
      
    public static void main(String[] args){  
        CalculatorService cs = new CalculatorService();  
        cs.init();  
    }  

}
