package com.ht.scada.communication.cluser;

import com.ht.scada.communication.IService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: bcw
 * Date: 13-4-25
 * Time: 下午4:07
 * To change this template use File | Settings | File Templates.
 */
public class MasterServer implements IService {
    private static final Logger log = LoggerFactory.getLogger(MasterServer.class);

    /**
     * 读超时时间（心跳信号超时时间）
     */
    private final int readTimeout = 10;
    private volatile boolean running = false;
    private volatile boolean clientConnected = false;
    private int port;

    private ServerBootstrap bootstrap;
    private Channel channel;

    public MasterServer(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        log.info("启动主机监听服务.");
        if (running) {
           return;
        }
        running = true;
        EventLoopGroup group = new NioEventLoopGroup(2);
        bootstrap = new ServerBootstrap()
                //.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .group(group, group)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new ReadTimeoutHandler(readTimeout),
                                new LineBasedFrameDecoder(80),
                                new StringDecoder(CharsetUtil.UTF_8),
                                new MasterServerHandler(new CallbackNotifier() {
                                    @Override
                                    public void onActive(ChannelHandlerContext outboundCtx) {
                                        clientConnected = true;
                                    }

                                    @Override
                                    public void onUnregistered(ChannelHandlerContext outboundCtx) {
                                        clientConnected = false;
                                    }
                                }));
                    }
                });

        // Bind and start to accept incoming connections.
        channel = bootstrap.bind(port).channel();
    }

    @Override
    public void stop() {
        log.info("停止主机监听服务.");
        if (running) {
            running = false;
            if (channel != null) {
                channel.close();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public boolean isClientConnected() {
        return clientConnected;
    }

    public static void main(String[] args) {
        MasterServer server = new MasterServer(4660);
        server.start();
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
