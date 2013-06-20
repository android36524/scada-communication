package com.ht.scada.communication.cluser;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
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
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case ALL_IDLE:
                    log.info("备机模式：主备机通讯通道长时间空闲，即将重新建立连接");
                    ctx.close();
                    break;
                case READER_IDLE:
                    log.info("备机模式：主备机通讯通道长时间没有接收,即将重新建立连接。");
                    ctx.close();
                    break;
                case WRITER_IDLE:
                    log.info("备机模式：主备机通讯通道长时间没有发送,即将重新建立连接。");
                    ctx.close();
                    break;
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
        log.info("备机模式：已与主机建立连接");
        cb.onActive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        cb.onUnregistered(ctx);
    }
}
