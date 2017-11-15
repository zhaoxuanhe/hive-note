package com.protobufDemo.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import com.protobufDemo.api.Calculator;
import com.protobufDemo.protogen.CalculatorMsg.RequestProto;
import com.protobufDemo.protogen.CalculatorMsg.ResponseProto;

public class TestCalculator implements Calculator{
	public int doTest(String op, int a, int b){  
        // TODO Auto-generated method stub  
        Socket s = null;  
        DataOutputStream out = null;  
        DataInputStream in = null;  
        int ret = 0;  
        try {  
        	
            s= new Socket("localhost", 7456);  
            out = new DataOutputStream(s.getOutputStream());  
            in = new DataInputStream(s.getInputStream());  
            
            //输出流
            RequestProto.Builder builder = RequestProto.newBuilder();  
            builder.setMethodName(op);  
            builder.setNum1(a);  
            builder.setNum2(b);  
            RequestProto request = builder.build();  
              
            byte [] bytes = request.toByteArray();  
            out.writeInt(bytes.length);  
            out.write(bytes);  
            out.flush();  
             
            //输入流
            int dataLen = in.readInt();  
            byte[] data = new byte[dataLen];  
            int count = in.read(data);  
            if(count != dataLen){  
                System.err.println("something bad happened!");  
            }  
              
            ResponseProto result = ResponseProto.parseFrom(data);  
            System.out.println(a + " " + op + " " +  b + "=" + result.getResult());              
            ret =  result.getResult();  
              
        }catch(Exception e){  
            e.printStackTrace();  
            System.err.println(e.toString());  
        }finally {  
            try{  
            in.close();  
            out.close();  
            s.close();  
            }catch(IOException e){  
                e.printStackTrace();  
            }  
        }  
        return ret;  
    }  
    public int add(int a, int b) {  
        // TODO Auto-generated method stub  
        return doTest("add", a, b);  
    }  
  
    
    public int minus(int a, int b) {  
        // TODO Auto-generated method stub  
        return doTest("minus", a, b);  
    }  
  
    /** 
     * @param args 
     */  
    public static void main(String[] args) {  
        // TODO Auto-generated method stub  
        TestCalculator tc = new TestCalculator();  
        int testCount = 5;  
        Random rand = new Random();  
        while(testCount-- > 0){  
            int a = rand.nextInt(100);  
            int b = rand.nextInt(100);  
            tc.add(a,b);  
            tc.minus(a, b);  
        }          
          
    }  
}
