package org.tio.common.channel;

import org.tio.common.AttributeMap;
import org.tio.common.ChannelStat;
import org.tio.common.GroupContext;

import java.nio.channels.AsynchronousSocketChannel;

import static org.tio.common.CoreConstant.ConnectionStatus;

/**
 * Copyright (c) for darkidiot
 * Date:2017/9/12
 * Author: <a href="darkidiot@icloud.com">darkidiot</a>
 * Desc:
 */
public interface Channel extends AttributeMap {

    Integer id();

    void bind(AsynchronousSocketChannel channel);

    ConnectionStatus status();

    void changeStatus(ConnectionStatus status);

    void invalidate();

    ChannelStat stat();

    GroupContext channelContext();
}