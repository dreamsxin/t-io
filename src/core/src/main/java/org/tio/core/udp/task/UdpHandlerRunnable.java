package org.tio.core.udp.task;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.udp.UdpPacket;
import org.tio.core.udp.intf.UdpHandler;

/**
 * @author tanyaowu 
 * 2017年7月6日 上午9:47:24
 */
public class UdpHandlerRunnable implements Runnable{
	private static Logger log = LoggerFactory.getLogger(UdpHandlerRunnable.class);

	private UdpHandler udpHandler;
	private LinkedBlockingQueue<UdpPacket> queue;
	
	
	public UdpHandlerRunnable(UdpHandler udpHandler, LinkedBlockingQueue<UdpPacket> queue) {
		super();
		this.udpHandler = udpHandler;
		this.queue = queue;
	}

	private boolean isStopped = false;

	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {

	}

	@Override
	public void run() {
		while(!isStopped) {
			try {
				UdpPacket udpPacket = queue.take();
				if (udpPacket != null) {
					udpHandler.handler(udpPacket); 
				}
			} catch (Throwable e) {
				log.error(e.toString(), e);
			}
		}
	}
	
	public void stop() {
		isStopped = true;
	}
}
