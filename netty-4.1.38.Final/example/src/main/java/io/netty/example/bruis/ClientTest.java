package io.netty.example.bruis;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lhy
 * @date 2021/8/19
 */
public class ClientTest {

    public static final EventLoopGroup myEventLoopGroup = new NioEventLoopGroup(1, new ThreadFactory() {

        private AtomicInteger threadIndex = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, String.format("MyNettyClientSelector_%d", this.threadIndex.incrementAndGet()));
        }
    });

    public static final DefaultEventExecutorGroup nettyHandlerExecutorGroup = new DefaultEventExecutorGroup(1,
            new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "nettyHandlerThread_" + this.threadIndex.incrementAndGet());
                }
            });

    public static void main(String[] args) {

        Bootstrap bootstrap = new Bootstrap()
                .group(myEventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.SO_SNDBUF, 65535)
                .option(ChannelOption.SO_RCVBUF, 65535)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(nettyHandlerExecutorGroup,
                                new NettyEncoder(),
                                new NettyDecoder(),
                                new ConnectResponseHandler());
                    }
                });

        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 9090);

        ChannelFuture channelFuture = bootstrap.connect(inetSocketAddress);

        if (channelFuture.awaitUninterruptibly(2, TimeUnit.MINUTES)) {
            Channel channel = channelFuture.channel();
            String request = "bcdccccccccccccccc";
            RemotingCommand remotingCommand = new RemotingCommand();
            remotingCommand.setBody(request.getBytes());
            remotingCommand.setCode(404);
            channel.writeAndFlush(remotingCommand).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    System.out.println(future.isDone());
                }
            });
        }
    }

    public static class ConnectResponseHandler extends SimpleChannelInboundHandler<RemotingCommand> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
            System.out.println("服务端返回消息了：" + new String(msg.getBody()));
        }
    }
}
