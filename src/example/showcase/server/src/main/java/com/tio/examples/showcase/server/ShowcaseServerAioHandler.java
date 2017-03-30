/**
 * **************************************************************************
 *
 * @说明: 
 * @项目名称: tio-examples-server
 *
 * @author: tanyaowu 
 * @创建时间: 2016年11月18日 上午9:13:15
 *
 * **************************************************************************
 */
package org.tio.examples.showcase.server;

import java.util.HashMap;
import java.util.Map;

import org.tio.core.Aio;
import org.tio.core.ChannelContext;
import org.tio.examples.showcase.common.ShowcaseAbsAioHandler;
import org.tio.examples.showcase.common.ShowcasePacket;
import org.tio.examples.showcase.common.ShowcaseSessionContext;
import org.tio.examples.showcase.common.Type;
import org.tio.examples.showcase.common.intf.AbsShowcaseBsHandler;
import org.tio.examples.showcase.server.handler.GroupMsgReqHandler;
import org.tio.examples.showcase.server.handler.HeartbeatReqHandler;
import org.tio.examples.showcase.server.handler.JoinGroupReqHandler;
import org.tio.examples.showcase.server.handler.LoginReqHandler;
import org.tio.examples.showcase.server.handler.P2PReqHandler;
import org.tio.server.intf.ServerAioHandler;

/**
 * 
 * @author tanyaowu 
 * @创建时间 2016年11月18日 上午9:13:15
 *
 * @操作列表
 *  编号	| 操作时间	| 操作人员	 | 操作说明
 *  (1) | 2016年11月18日 | tanyaowu | 新建类
 *
 */
public class ShowcaseServerAioHandler extends ShowcaseAbsAioHandler implements ServerAioHandler<ShowcaseSessionContext, ShowcasePacket, Object>
{
	
	private static Map<Byte, AbsShowcaseBsHandler<?>> handlerMap = new HashMap<>();
	static
	{
		handlerMap.put(Type.GROUP_MSG_REQ, new GroupMsgReqHandler());
		handlerMap.put(Type.HEART_BEAT_REQ, new HeartbeatReqHandler());
		handlerMap.put(Type.JOIN_GROUP_REQ, new JoinGroupReqHandler());
		handlerMap.put(Type.LOGIN_REQ, new LoginReqHandler());
		handlerMap.put(Type.P2P_REQ, new P2PReqHandler());
	}
	
	
	/** 
	 * 处理消息
	 */
	@Override
	public Object handler(ShowcasePacket packet, ChannelContext<ShowcaseSessionContext, ShowcasePacket, Object> channelContext) throws Exception
	{
		Byte type = packet.getType();
		AbsShowcaseBsHandler<?> showcaseBsHandler = handlerMap.get(type);
		showcaseBsHandler.handler(packet, channelContext);
		return null;
	}
}
