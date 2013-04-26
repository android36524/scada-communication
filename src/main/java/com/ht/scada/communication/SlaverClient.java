package com.ht.scada.communication;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: bcw
 * Date: 13-4-25
 * Time: 下午5:00
 * To change this template use File | Settings | File Templates.
 */
public class SlaverClient extends SimpleChannelHandler implements IService {

    private static final ChannelFactory factory = new NioClientSocketChannelFactory(
            Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());
    private final Logger log = LoggerFactory.getLogger(SlaverClient.class);

    private final String host;
    private final int port;

    private Timer timer;
    private Channel channel;
    private boolean running = false;
    private final Object lock = new Object();

    private ClientBootstrap bootstrap;

    public SlaverClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void start() {
        synchronized (lock) {
            if (!running) {
                running = true;
                connect();
            }
        }
    }

    private void connect() {
        log.info("请求与主机建立连接");
        bootstrap = new ClientBootstrap(factory);

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                                         public ChannelPipeline getPipeline() throws Exception {
                                                 return Channels.pipeline(SlaverClient.this);
                                                                                                                                  }
                                                                                                                                  });
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
        future.getChannel().getCloseFuture().awaitUninterruptibly();
    }

    @Override
    public void stop() {
        synchronized (lock) {
            if (running) {
                running = false;
                log.info("停止与主机之间的连接");
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                if (channel != null) {
                    channel.close();
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        super.exceptionCaught(ctx, e);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("建立连接");
        channel = ctx.getChannel();
        startHeartBeatTimer();
    }

    private void startHeartBeatTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(channel != null) {
                    channel.write(ChannelBuffers.copiedBuffer("hello".getBytes()));
                }
            }
        }, 0, 6000);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("连接断开");
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("通道关闭");
        channel = null;
        synchronized (lock) {
            if (running) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                connect();
            }
        }
    }

    public static void main(String[] args) {
        SlaverClient client = new SlaverClient("127.0.0.1", 4660);
        client.start();
        try {
            Thread.sleep(300000);
        } catch (InterruptedException e) {
        }
    }
}
