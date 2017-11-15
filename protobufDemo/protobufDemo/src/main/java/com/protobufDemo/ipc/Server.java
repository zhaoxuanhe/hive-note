package com.protobufDemo.ipc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.protobuf.BlockingService;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;
import com.protobufDemo.protogen.CalculatorMsg.RequestProto;

public class Server extends Thread {
	private Class<?> protocol;  
	   private BlockingService impl;  
	   private int port;  
	   private ServerSocket ss;  
	  
	   public Server(Class<?> protocol, BlockingService protocolImpl, int port){  
	      this.protocol = protocol;  
	      this.impl = protocolImpl;   
	      this.port = port;  
	   }  
	  
	   public void run(){  
	      Socket clientSocket = null;  
	      DataOutputStream dos = null;  
	      DataInputStream dis = null;  
	      try {  
	           ss = new ServerSocket(port);  
	       }catch(IOException e){  
	       }      
	       int testCount = 10; //进行10次计算后就退出  
	  
	       while(testCount-- > 0){  
	          try {  
	               clientSocket = ss.accept();  
	               dos = new DataOutputStream(clientSocket.getOutputStream());  
	               dis = new DataInputStream(clientSocket.getInputStream());  
	               //获取输入流的长度
 	               int dataLen = dis.readInt();
// 	               System.out.println("输入字节流长度："+dataLen);
	               byte[] dataBuffer = new byte[dataLen]; 
	               
	               
	               //从包含的输入流中读取一些字节数，并将它们存储到缓冲区数组dataBuffer中。 
	               //实际读取的字节数作为整数返回。此方法阻塞，直到输入数据可用，检测到文件结尾或引发异常。
	               int readCount = dis.read(dataBuffer);  
	               byte[] result = processOneRpc(dataBuffer);  
	  
	               dos.writeInt(result.length);  
	               dos.write(result);  
	               dos.flush();  
	           }catch(Exception e){  
	           }  
	       }  
	       try {   
	           dos.close();  
	           dis.close();  
	           ss.close();  
	       }catch(Exception e){  
	       };  
	  
	   }  
	  
	   public byte[] processOneRpc (byte[] data) throws Exception {  
	       //将data转换成message格式
		   RequestProto request = RequestProto.parseFrom(data);  
	  
	      String methodName = request.getMethodName();  
	      MethodDescriptor methodDescriptor = impl.getDescriptorForType().findMethodByName(methodName);  
	      Message response = impl.callBlockingMethod(methodDescriptor, null, request);  
	      return response.toByteArray();  
	   }  
}
