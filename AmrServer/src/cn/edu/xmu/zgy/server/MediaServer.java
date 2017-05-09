package cn.edu.xmu.zgy.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import cn.edu.xmu.zgy.config.CommonConfig;
import cn.edu.xmu.zgy.packet.FramePacket;

//blog.csdn.net/zgyulongfei
//Email: zgyulongfei@gmail.com

public abstract class MediaServer {
	private final int maxClientNumber = CommonConfig.MAX_CLIENT_NUMBER;

	// 上行通道：接收数据 udp
	private DatagramSocket udpServer;
	// 下行通道：发送数据 tcp
	private ServerSocket tcpServer;
	private ArrayList<Client> clientList;

	// 数据包存在FramePacket中
	private LinkedList<FramePacket> packetList;
	private int bufferSize;

	private boolean serverRunning;

	public MediaServer(int udpPort, int tcpPort, int bufferSize) {
		this.bufferSize = bufferSize;
		try {
			udpServer = new DatagramSocket(udpPort);
			tcpServer = new ServerSocket(tcpPort, maxClientNumber);
		} catch (Exception e) {
			// TODO: handle exception
		}
		packetList = new LinkedList<FramePacket>();
		clientList = new ArrayList<Client>();
		serverRunning = true;
	}

	public abstract void start();

	public void stop() {
		serverRunning = false;

		if (udpServer != null) {
			udpServer.close();
			udpServer = null;
		}

		if (tcpServer != null) {
			try {
				tcpServer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			tcpServer = null;
		}

		packetList.clear();
		packetList = null;

		clientList.clear();
		clientList = null;
	}

	public void addPacketToBuffer(FramePacket packet) {
		if (packetList.size() > bufferSize) {
			// 队列 先进先出
			takeAwayFirstPacket();
		}
		packetList.addLast(packet);
	}

	public byte[] takeAwayFirstFrame() {
		FramePacket packet = takeAwayFirstPacket();
		if (packet == null) {
			return null;
		}
		return packet.getFrame();
	}

	private synchronized FramePacket takeAwayFirstPacket() {
		if (packetList.size() <= 0) {
			return null;
		}
		FramePacket fp = packetList.getFirst();
		if (fp == null) {
			return null;
		}
		// 因为要return第一个FramePacket 所以拷贝了再删除
		FramePacket packet = new FramePacket(fp);
		packetList.removeFirst();
		return packet;
	}

	public void addClient(Socket clientSocket, int id) {
		clientList.add(new Client(clientSocket, id));
	}

	public void removeClient(int id) {
		int needRemoveIndex = -1;
		// 循环了一遍之后做一个标记
		for (int ix = 0; ix < clientList.size(); ++ix) {
			Client client = clientList.get(ix);
			if (client.getId() == id) {
				needRemoveIndex = ix;
				break;
			}
		}
		// 之后再根据标记来删除 不能在循环的时候直接删除 线程不安全
		if (needRemoveIndex != -1) {
			clientList.remove(needRemoveIndex);
		}
	}

	public DatagramSocket getUdpServer() {
		return udpServer;
	}

	public ServerSocket getTcpServer() {
		return tcpServer;
	}

	public boolean isServerRunning() {
		return serverRunning;
	}

	public boolean isBufferEmpty() {
		return packetList.size() == 0;
	}

	public class MulticastThread implements Runnable {
		public void run() {
			while (isServerRunning()) {
				try {
					sendDataToAllClient();
					Thread.sleep(25);
				} catch (Exception e) {
				}
			}
		}

		public void sendDataToAllClient() throws Exception {
			if (isBufferEmpty() || clientList.size() <= 0) {
				return;
			}
			boolean bufEmpty = isBufferEmpty();
			byte[] block = takeAwayFirstFrame();
			ArrayList<Integer> disConnectClient = new ArrayList<Integer>();
			for (int ix = 0; ix < clientList.size(); ++ix) {
				Client client = clientList.get(ix);
				Socket clientSocket = client.getSocket();

				if (clientSocket.isConnected()) {
					try {
						if (!bufEmpty) {
							// block没数据 找下一个client
							if (block == null) {
								continue;
							}
							OutputStream output = clientSocket.getOutputStream();
							output.write(block);
							output.flush();
						}
					} catch (Exception err) {
						// 报错的client就加入到离线的client列表里
						disConnectClient.add(ix);
						System.out.println("send data to id=" + client.getId() + " error" + " :"
								+ err.getMessage());
					}
				} else {
					// 没有连接也加入到离线的client列表里
					disConnectClient.add(ix);
				}
			}
			// 根据离线的client列表清空client列表
			for (int ix = 0; ix < disConnectClient.size(); ++ix) {
				int index = disConnectClient.get(ix);
				clientList.remove(index);
			}
			disConnectClient.clear();
			disConnectClient = null;
			block = null;
		}
	}

	private class Client {
		private Socket socket;
		private int id;

		public Client(Socket socket, int id) {
			this.socket = socket;
			this.id = id;
		}

		public Socket getSocket() {
			return socket;
		}

		public int getId() {
			return id;
		}
	}
}
