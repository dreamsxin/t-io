package org.tio.websocket.server;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.exception.AioDecodeException;
import org.tio.http.common.http.HttpConst;
import org.tio.http.common.http.HttpRequestDecoder;
import org.tio.http.common.http.HttpRequestPacket;
import org.tio.http.common.http.HttpResponseEncoder;
import org.tio.http.common.http.HttpResponsePacket;
import org.tio.http.common.http.HttpResponseStatus;
import org.tio.server.intf.ServerAioHandler;
import org.tio.websocket.common.Opcode;
import org.tio.websocket.common.WsPacket;
import org.tio.websocket.common.WsRequestPacket;
import org.tio.websocket.common.WsResponsePacket;
import org.tio.websocket.common.WsServerDecoder;
import org.tio.websocket.common.WsServerEncoder;
import org.tio.websocket.common.WsSessionContext;
import org.tio.websocket.common.util.BASE64Util;
import org.tio.websocket.common.util.SHA1Util;
import org.tio.websocket.server.handler.IWsMsgHandler;

/**
 * 
 * @author tanyaowu 
 *
 */
public class WsServerAioHandler implements ServerAioHandler<WsSessionContext, WsPacket, Object> {
	private static Logger log = LoggerFactory.getLogger(WsServerAioHandler.class);

	//	private static Map<Command, ImBsHandlerIntf> handlerMap = new HashMap<>();
	//	static {
	//		handlerMap.put(Command.COMMAND_HANDSHAKE_REQ, new HandshakeReqHandler());
	//		handlerMap.put(Command.COMMAND_AUTH_REQ, new AuthReqHandler());
	//		handlerMap.put(Command.COMMAND_CHAT_REQ, new ChatReqHandler());
	//		handlerMap.put(Command.COMMAND_JOIN_GROUP_REQ, new JoinReqHandler());
	//		handlerMap.put(Command.COMMAND_HEARTBEAT_REQ, new HeartbeatReqHandler());
	//		handlerMap.put(Command.COMMAND_CLOSE_REQ, new CloseReqHandler());
	//
	//		handlerMap.put(Command.COMMAND_LOGIN_REQ, new LoginReqHandler());
	//		handlerMap.put(Command.COMMAND_CLIENT_PAGE_REQ, new ClientPageReqHandler());
	//
	//	}

	private WsServerConfig wsServerConfig;

	private IWsMsgHandler wsMsgHandler;

	/**
	 * 
	 *
	 * @author: tanyaowu
	 * 2016年11月18日 上午9:13:15
	 * 
	 */
	public WsServerAioHandler(WsServerConfig wsServerConfig, IWsMsgHandler wsMsgHandler) {
		this.wsServerConfig = wsServerConfig;
		this.wsMsgHandler = wsMsgHandler;
	}

	/**
	 * @param args
	 *
	 * @author: tanyaowu
	 * 2016年11月18日 上午9:13:15
	 * 
	 */
	public static void main(String[] args) {
	}

	/** 
	 * @see org.tio.core.intf.AioHandler#handler(org.tio.core.intf.Packet)
	 * 
	 * @param packet
	 * @return
	 * @throws Exception 
	 * @author: tanyaowu
	 * 2016年11月18日 上午9:37:44
	 * 
	 */
	@Override
	public Object handler(WsPacket packet, ChannelContext<WsSessionContext, WsPacket, Object> channelContext) throws Exception {
		WsRequestPacket wsRequestPacket = (WsRequestPacket) packet;

		if (wsRequestPacket.isHandShake()) {
			WsSessionContext wsSessionContext = channelContext.getSessionContext();
			HttpRequestPacket httpRequestPacket = wsSessionContext.getHandshakeRequestPacket();
			HttpResponsePacket httpResponsePacket = wsSessionContext.getHandshakeResponsePacket();
			HttpResponsePacket r = wsMsgHandler.handshake(httpRequestPacket, httpResponsePacket, channelContext);
			if (r == null) {
				Aio.remove(channelContext, "业务层不同意握手");
				return null;
			}

			WsResponsePacket wsResponsePacket = new WsResponsePacket();
			wsResponsePacket.setHandShake(true);
			Aio.send(channelContext, wsResponsePacket);
			wsSessionContext.setHandshaked(true);
			return null;
		}

		WsResponsePacket wsResponsePacket = h(wsRequestPacket, wsRequestPacket.getBody(), wsRequestPacket.getWsOpcode(), channelContext);

		if (wsResponsePacket != null) {
			Aio.send(channelContext, wsResponsePacket);
		}

		return null;
	}

	private WsResponsePacket h(WsRequestPacket websocketPacket, byte[] bytes, Opcode opcode, ChannelContext<WsSessionContext, WsPacket, Object> channelContext) throws Exception {
		WsResponsePacket wsResponsePacket = null;
		if (opcode == Opcode.TEXT) {
			if (bytes == null || bytes.length == 0) {
				Aio.remove(channelContext, "错误的websocket包，body为空");
				return null;
			}
			String text = new String(bytes, wsServerConfig.getCharset());
			Object retObj = wsMsgHandler.onText(websocketPacket, text, channelContext);
			String methodName = "onText";
			wsResponsePacket = processRetObj(retObj, methodName, channelContext);
			return wsResponsePacket;
		} else if (opcode == Opcode.BINARY) {
			if (bytes == null || bytes.length == 0) {
				Aio.remove(channelContext, "错误的websocket包，body为空");
				return null;
			}
			Object retObj = wsMsgHandler.onBytes(websocketPacket, bytes, channelContext);
			String methodName = "onBytes";
			wsResponsePacket = processRetObj(retObj, methodName, channelContext);
			return wsResponsePacket;
		} else if (opcode == Opcode.PING || opcode == Opcode.PONG) {
			log.error("收到" + opcode);
			return null;
		} else if (opcode == Opcode.CLOSE) {
			Object retObj = wsMsgHandler.onClose(websocketPacket, bytes, channelContext);
			String methodName = "onClose";
			wsResponsePacket = processRetObj(retObj, methodName, channelContext);
			return wsResponsePacket;
		} else {
			Aio.remove(channelContext, "错误的websocket包，错误的Opcode");
			return null;
		}
	}

	private WsResponsePacket processRetObj(Object obj, String methodName, ChannelContext<WsSessionContext, WsPacket, Object> channelContext) throws Exception {
		WsResponsePacket wsResponsePacket = null;
		if (obj == null) {
			return null;
		} else {
			if (obj instanceof String) {
				String str = (String) obj;
				wsResponsePacket = new WsResponsePacket();
				wsResponsePacket.setBody(str.getBytes(wsServerConfig.getCharset()));
				wsResponsePacket.setWsOpcode(Opcode.TEXT);
				return wsResponsePacket;
			} else if (obj instanceof byte[]) {
				wsResponsePacket = new WsResponsePacket();
				wsResponsePacket.setBody((byte[]) obj);
				wsResponsePacket.setWsOpcode(Opcode.BINARY);
				return wsResponsePacket;
			} else if (obj instanceof WsResponsePacket) {
				return (WsResponsePacket) obj;
			} else if (obj instanceof ByteBuffer) {
				wsResponsePacket = new WsResponsePacket();
				byte[] bs = ((ByteBuffer) obj).array();
				wsResponsePacket.setBody(bs);
				wsResponsePacket.setWsOpcode(Opcode.BINARY);
				return wsResponsePacket;
			} else {
				log.error("{} {}.{}()方法，只允许返回byte[]、ByteBuffer、WsResponsePacket或null，但是程序返回了{}", channelContext, this.getClass().getName(), methodName, obj.getClass().getName());
				return null;
			}
		}
		
	}

	/** 
	 * @see org.tio.core.intf.AioHandler#encode(org.tio.core.intf.Packet)
	 * 
	 * @param packet
	 * @return
	 * @author: tanyaowu
	 * 2016年11月18日 上午9:37:44
	 * 
	 */
	@Override
	public ByteBuffer encode(WsPacket packet, GroupContext<WsSessionContext, WsPacket, Object> groupContext, ChannelContext<WsSessionContext, WsPacket, Object> channelContext) {
		WsResponsePacket wsResponsePacket = (WsResponsePacket) packet;

		//握手包
		if (wsResponsePacket.isHandShake()) {
			WsSessionContext imSessionContext = channelContext.getSessionContext();
			HttpResponsePacket handshakeResponsePacket = imSessionContext.getHandshakeResponsePacket();
			return HttpResponseEncoder.encode(handshakeResponsePacket, groupContext, channelContext);
		}

		ByteBuffer byteBuffer = WsServerEncoder.encode(wsResponsePacket, groupContext, channelContext);
		return byteBuffer;
	}

	/** 
	 * @see org.tio.core.intf.AioHandler#decode(java.nio.ByteBuffer)
	 * 
	 * @param buffer
	 * @return
	 * @throws AioDecodeException
	 * @author: tanyaowu
	 * 2016年11月18日 上午9:37:44
	 * 
	 */
	@Override
	public WsRequestPacket decode(ByteBuffer buffer, ChannelContext<WsSessionContext, WsPacket, Object> channelContext) throws AioDecodeException {
		WsSessionContext imSessionContext = channelContext.getSessionContext();
		//		int initPosition = buffer.position();

		if (!imSessionContext.isHandshaked()) {
			HttpRequestPacket httpRequestPacket = HttpRequestDecoder.decode(buffer, channelContext);
			if (httpRequestPacket == null) {
				return null;
			}

			HttpResponsePacket httpResponsePacket = updateWebSocketProtocol(httpRequestPacket, channelContext);
			if (httpResponsePacket == null) {
				throw new AioDecodeException("http协议升级到websocket协议失败");
			}

			imSessionContext.setHandshakeRequestPacket(httpRequestPacket);
			imSessionContext.setHandshakeResponsePacket(httpResponsePacket);

			WsRequestPacket wsRequestPacket = new WsRequestPacket();
			//			wsRequestPacket.setHeaders(httpResponsePacket.getHeaders());
			//			wsRequestPacket.setBody(httpResponsePacket.getBody());
			wsRequestPacket.setHandShake(true);

			return wsRequestPacket;
		}

		WsRequestPacket websocketPacket = WsServerDecoder.decode(buffer, channelContext);
		return websocketPacket;
		//		if (websocketPacket == null) {
		//			return null;
		//		}
		//
		//		if (!websocketPacket.isWsEof()) {
		//			log.error("{} websocket包还没有传完", channelContext);
		//			return null;
		//		}
		//
		//		Opcode opcode = websocketPacket.getWsOpcode();
		//		if (opcode == Opcode.BINARY) {
		//			byte[] wsBody = websocketPacket.getWsBody();
		//			if (wsBody == null || wsBody.length == 0) {
		//				throw new AioDecodeException("错误的websocket包，body为空");
		//			}
		//
		//			WsRequestPacket imPacket = new WsRequestPacket();
		//
		//			if (wsBody.length > 1) {
		//				byte[] dst = new byte[wsBody.length - 1];
		//				System.arraycopy(wsBody, 1, dst, 0, dst.length);
		//				imPacket.setBody(dst);
		//			}
		//			return imPacket;
		//		} else if (opcode == Opcode.PING || opcode == Opcode.PONG) {
		//			return heartbeatPacket;
		//		} else if (opcode == Opcode.CLOSE) {
		//			WsRequestPacket imPacket = new WsRequestPacket();
		//			return imPacket;
		//		} else if (opcode == Opcode.TEXT) {
		//			throw new AioDecodeException("错误的websocket包，不支持TEXT类型的数据");
		//		} else {
		//			throw new AioDecodeException("错误的websocket包，错误的Opcode");
		//		}
	}

	/**
	 * 本方法改编自baseio: https://git.oschina.net/generallycloud/baseio<br>
	 * 感谢开源作者的付出
	 * @param httpRequestPacket
	 * @return
	 *
	 * @author: tanyaowu
	 * 2017年2月23日 下午4:11:41
	 *
	 */
	public HttpResponsePacket updateWebSocketProtocol(HttpRequestPacket httpRequestPacket, ChannelContext<WsSessionContext, WsPacket, Object> channelContext) {
		Map<String, String> headers = httpRequestPacket.getHeaders();

		String Sec_WebSocket_Key = headers.get(HttpConst.RequestHeaderKey.Sec_WebSocket_Key);

		if (StringUtils.isNotBlank(Sec_WebSocket_Key)) {
			String Sec_WebSocket_Key_Magic = Sec_WebSocket_Key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
			byte[] key_array = SHA1Util.SHA1(Sec_WebSocket_Key_Magic);
			String acceptKey = BASE64Util.byteArrayToBase64(key_array);
			HttpResponsePacket httpResponsePacket = new HttpResponsePacket(httpRequestPacket);

			httpResponsePacket.setStatus(HttpResponseStatus.C101);

			Map<String, String> respHeaders = new HashMap<>();
			respHeaders.put(HttpConst.ResponseHeaderKey.Connection, HttpConst.ResponseHeaderValue.Connection.Upgrade);
			respHeaders.put(HttpConst.ResponseHeaderKey.Upgrade, "WebSocket");
			respHeaders.put(HttpConst.ResponseHeaderKey.Sec_WebSocket_Accept, acceptKey);
			httpResponsePacket.setHeaders(respHeaders);
			return httpResponsePacket;
		}
		return null;
	}

	/**
	 * @return the httpServerConfig
	 */
	public WsServerConfig getHttpServerConfig() {
		return wsServerConfig;
	}

	/**
	 * @param httpServerConfig the httpServerConfig to set
	 */
	public void setHttpServerConfig(WsServerConfig httpServerConfig) {
		this.wsServerConfig = httpServerConfig;
	}

}
