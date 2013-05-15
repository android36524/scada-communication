package com.ht.scada.communication.cluser;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created with IntelliJ IDEA.
 * User: bcw
 * Date: 13-4-27
 * Time: 下午4:14
 * To change this template use File | Settings | File Templates.
 */
public interface CallbackNotifier {
    void onActive(ChannelHandlerContext outboundCtx);
    void onUnregistered(ChannelHandlerContext outboundCtx);
}
