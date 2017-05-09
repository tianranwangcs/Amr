package cn.edu.xmu.zgy.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import cn.edu.xmu.zgy.config.CommonConfig;
import cn.edu.xmu.zgy.packet.FramePacket;

//blog.csdn.net/zgyulongfei
//Email: zgyulongfei@gmail.com

public class AmrServer extends MediaServer {
    // 上行通道：接收数据 udp
    private static final int udpPort = CommonConfig.AUDIO_SERVER_UP_PORT;
    // 下行通道：发送数据 tcp
    private static final int tcpPort = CommonConfig.AUDIO_SERVER_DOWN_PORT;
    private static final int BUFFER_SIZE = 50 * 100;

    private DatagramSocket udpServer;
    private ServerSocket tcpServer;

    public AmrServer() {
        super(udpPort, tcpPort, BUFFER_SIZE);
        udpServer = getUdpServer();
        tcpServer = getTcpServer();
    }

    @Override
    public void start() {
        // 上传和下行开启两个线程
        new Thread(new UdpThread()).start();
        new Thread(new TcpThread()).start();
    }

    // 上行 接收数据 UDP
    private class UdpThread implements Runnable {
        @Override
        public void run() {
            System.out.println("audio udp server start...");

            byte[] data = new byte[1024 * 10];
            while (isServerRunning()) {
                try {
                    // 接收数据
                    DatagramPacket pack = new DatagramPacket(data, data.length);
                    udpServer.receive(pack);
                    // 存到FramePacket
                    addPacketToBuffer(new FramePacket(pack.getData(), pack.getLength()));
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        }
    }

    // 下行 发送数据 TCP 其实这里没有发送数据 仅仅是负责连接的建立 发送数据在MediaServer里
    private class TcpThread implements Runnable {
        public void run() {
            try {
                System.out.println("audio tcp server start...");
                new Thread(new MulticastThread()).start();
                int clientId = 0;
                while (isServerRunning()) {
                    // 不断试图建立新连接
                    // client id自行分配 不断增长
                    Socket clientSocket = tcpServer.accept();
                    addClient(clientSocket, clientId);
                    ++clientId;
                }
            } catch (Exception e) {
                System.out.println("audio TcpThread error.....");
            }
        }
    }
}
