package com.ht.scada.communication.cluser;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: bcw
 * Date: 13-4-27
 * Time: 下午3:23
 * To change this template use File | Settings | File Templates.
 */
public class MasterServerHandler extends ChannelInboundMessageHandlerAdapter<String> {
    private static Logger log = LoggerFactory.getLogger(MasterServerHandler.class);

    private final CallbackNotifier cb;

    public MasterServerHandler(CallbackNotifier cb) {
        this.cb = cb;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("主机模式：收到从机信息({})-{}", ctx.channel().remoteAddress().toString(), msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);    //To change body of overridden methods use File | Settings | File Templates.
        if (cause instanceof ReadTimeoutException) {
            log.info("主机模式：接收从机数据超时,即将断开连接。");
            ctx.close();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("主机模式：已与从机建立连接-{}", ctx.channel().remoteAddress().toString());
        cb.onActive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("主机模式：与从机连接断开-{}", ctx.channel().remoteAddress().toString());
        cb.onUnregistered(ctx);
    }
}
