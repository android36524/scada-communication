package com.ht.scada.communication.cluser;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: bcw
 * Date: 13-4-27
 * Time: 下午3:42
 * To change this template use File | Settings | File Templates.
 */
public class SlaverClientHandler extends ChannelInboundMessageHandlerAdapter<String> {
    private static final Logger log = LoggerFactory.getLogger(SlaverClientHandler.class);

    private final CallbackNotifier cb;

    public SlaverClientHandler(CallbackNotifier cb) {
        this.cb = cb;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleState) {
            IdleState e = (IdleState) evt;
            if (e == IdleState.ALL_IDLE) {
                log.info("通道空闲超时，即将关闭通道");
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
        log.debug("服务端信息：{}", msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("建立连接");
        cb.onActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("连接断开");
        //cb.onUnregistered(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("channelUnregistered");
        cb.onUnregistered(ctx);
    }
}
