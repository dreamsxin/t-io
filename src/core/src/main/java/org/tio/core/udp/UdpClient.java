package org.tio.core.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Node;
import org.tio.core.udp.task.UdpSendRunnable;

/**
 * @author tanyaowu 
 * 2017年7月5日 下午2:54:12
 */
public class UdpClient {
	private static Logger log = LoggerFactory.getLogger(UdpClient.class);

	private static final int TIMEOUT = 5000; //设置接收数据的超时时间

	private LinkedBlockingQueue<DatagramPacket> queue = new LinkedBlockingQueue<>();

	private UdpClientConf udpClientConf = null;

	/**
	 * 服务器地址
	 */
	private InetSocketAddress inetSocketAddress = null;

	private UdpSendRunnable udpSendRunnable = null;

	public UdpClient(UdpClientConf udpClientConf) {
		super();
		this.udpClientConf = udpClientConf;
		Node node = this.udpClientConf.getServerNode();
		inetSocketAddress = new InetSocketAddress(node.getIp(), node.getPort());
		udpSendRunnable = new UdpSendRunnable(queue, udpClientConf);
	}

	public void start() {
		Thread thread = new Thread(udpSendRunnable, "tio-udp-client-send");
		thread.setDaemon(false);
		thread.start();
	}

	public void send(byte[] data) {
		DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetSocketAddress);
		queue.add(datagramPacket);
	}

	public static void main(String args[]) throws IOException {
		UdpClientConf udpClientConf = new UdpClientConf("127.0.0.1", 3000, 5000);
		UdpClient udpClient = new UdpClient(udpClientConf);
		udpClient.start();

		for (int i = 0; i < 100; i++) {
			String str = i + "、" + "有点意思";
			udpClient.send(str.getBytes());
		}
	}
}
