package com.zhao.Demo.socket;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

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
			//System.out.println("客户端开始发送消息");
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
