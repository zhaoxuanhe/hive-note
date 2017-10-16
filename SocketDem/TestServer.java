package com.zhao.Demo.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

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
