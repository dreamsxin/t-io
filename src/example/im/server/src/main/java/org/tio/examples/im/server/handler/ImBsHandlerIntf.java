package org.tio.examples.im.server.handler;

import org.tio.core.ChannelContext;
import org.tio.examples.im.common.ImPacket;
import org.tio.examples.im.common.ImSessionContext;

/**
 * 
 * @author tanyaowu 
 * @创建时间 2016年11月18日 下午1:06:56
 *
 * @操作列表
 *  编号	| 操作时间	| 操作人员	 | 操作说明
 *  (1) | 2016年11月18日 | tanyaowu | 新建类
 *
 */
public interface ImBsHandlerIntf
{
	/**
	 * 
	 * @param packet
	 * @param channelContext
	 * @return
	 *
	 * @author: tanyaowu
	 * @创建时间:　2016年11月18日 下午1:08:45
	 *
	 */
	public Object handler(ImPacket packet, ChannelContext<ImSessionContext, ImPacket, Object> channelContext)  throws Exception;
}