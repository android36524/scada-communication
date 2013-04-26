package com.ht.scada.communication;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: bcw
 * Date: 13-4-25
 * Time: 下午4:07
 * To change this template use File | Settings | File Templates.
 */
@ChannelHandler.Sharable
public class MasterServer extends SimpleChannelHandler implements IService {
    private static final Logger log = LoggerFactory.getLogger(MasterServer.class);
    private boolean running = false;
    private int port = 4660;

    private ServerBootstrap bootstrap;

    public MasterServer(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        log.info("启动主机监听服务.");
        bootstrap = new ServerBootstrap(
            new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool()));
        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(MasterServer.this);
            }
        });
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(port));
        //bootstrap.bindAsync(new InetSocketAddress(port));
        running = true;
    }

    @Override
    public void stop() {
        log.info("停止主机监听服务.");
        bootstrap.shutdown();
        running = false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        super.exceptionCaught(ctx, e);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
        int len = buffer.readableBytes();
        byte[] data = new byte[len];
        buffer.readBytes(data);
        log.info("收到信息-{}：{}", ctx.getChannel().getRemoteAddress().toString(), new String(data));
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("channelConnected:{}", ctx.getChannel().getRemoteAddress().toString());
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("channelDisconnected:{}", ctx.getChannel().getRemoteAddress().toString());
    }

    @Override
    public void childChannelOpen(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
        log.info("childChannelOpen:{}", ctx.getChannel().getRemoteAddress().toString());
    }

    @Override
    public void childChannelClosed(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
        log.info("childChannelClosed:{}", ctx.getChannel().getRemoteAddress().toString());
    }

    public static void main(String[] args) {
//        MasterServer server = new MasterServer(4660);
//        server.start();
        char c = 13;
        System.out.println('\r');
    }
}
