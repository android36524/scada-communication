package com.ht.scada.communication.cluser;

import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.IService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.BufType;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: bcw
 * Date: 13-4-25
 * Time: 下午5:00
 * To change this template use File | Settings | File Templates.
 */
public class SlaverClient implements IService {

    private final Logger log = LoggerFactory.getLogger(SlaverClient.class);
    private final int heartBeatInterval = 5;
    private final int reConnectInterval = 5;
    private final int idleTime = 20;
    private final String host;
    private final int port;

    private volatile boolean running = false;
    private final Object lock = new Object();

    private Bootstrap bootstrap;
    private Channel channel;
    private EventLoopGroup eventLoopGroup;

    private CommunicationManager communicationController;

    private long lastConnectTime = -1;
    private volatile ScheduledFuture<?> timer;

    public SlaverClient(String host, int port, CommunicationManager communicationController) {
        this.host = host;
        this.port = port;
        this.communicationController = communicationController;
    }

    @Override
    public void start() {
        if (!running) {
            running = true;
            eventLoopGroup = new NioEventLoopGroup();
            connect();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void connect() {
        if (!running) {
            return;
        }
        log.info("备机模式：请求与主机建立连接");
        bootstrap = new Bootstrap().group(new NioEventLoopGroup(2))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new LoggingHandler(LogLevel.INFO),
                                new IdleStateHandler(0,0,idleTime),
                                new LineBasedFrameDecoder(80),
                                new StringDecoder(CharsetUtil.UTF_8),
                                new StringEncoder(BufType.BYTE, CharsetUtil.UTF_8),
                                new SlaverClientHandler(new CallbackNotifier() {
                                    @Override
                                    public void onActive(ChannelHandlerContext ctx) {
                                        lastConnectTime = System.currentTimeMillis();
                                        // 已连接到主机，停止当前所有采集通道
                                        if (communicationController != null) {
                                            communicationController.stopAllChannel();
                                        }
                                        timer = ctx.executor().scheduleAtFixedRate(new HeartBeatTask(ctx), 0, heartBeatInterval, TimeUnit.SECONDS);
                                    }

                                    @Override
                                    public void onUnregistered(ChannelHandlerContext ctx) {
                                        if (timer != null) {
                                            timer.cancel(false);
                                            timer = null;
                                        }
                                        // 5秒后重连
                                        log.info("备机模式：未连接到主机建立，{}秒后重连.", reConnectInterval);
                                        ctx.executor().schedule(new Runnable() {
                                            @Override
                                            public void run() {
                                                //To change body of implemented methods use File | Settings | File Templates.
                                                if (running) {
                                                    connect();
                                                    if (communicationController != null && System.currentTimeMillis() - lastConnectTime > 10) {
                                                        communicationController.startAllChannel();
                                                    }
                                                }
                                            }
                                        }, reConnectInterval, TimeUnit.SECONDS);
                                    }
                                })
                        );
                    }
                });

        try {
            channel = bootstrap.connect(host, port).sync().channel();
        } catch (Exception e) {
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void stop() {
        log.info("备机模式：断开与主机之间的连接");
        running = false;
        if (channel != null) {
            channel.close();
            bootstrap.shutdown();
        }
    }

    public boolean isConnected() {
        return channel == null ? false : channel.isActive();
    }

    private final class HeartBeatTask implements Runnable {

        private final ChannelHandlerContext ctx;

        private HeartBeatTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!ctx.channel().isOpen()) {
                return;
            }
            ctx.write("HELLO\r\n");
        }
    }

    public static void main(String[] args) {
        SlaverClient client = new SlaverClient("127.0.0.1", 4660, null);
        client.start();
        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
        }
        client.stop();
    }
}
