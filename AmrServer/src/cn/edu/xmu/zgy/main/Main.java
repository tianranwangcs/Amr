package cn.edu.xmu.zgy.main;

import javax.swing.JFrame;

//blog.csdn.net/zgyulongfei
//Email: zgyulongfei@gmail.com
public class Main {
	public static void main(String args[]) {
		ServerFrame serverFrame = new ServerFrame();
		serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		serverFrame.setSize(500, 500);
		serverFrame.setVisible(true);
	}
}
