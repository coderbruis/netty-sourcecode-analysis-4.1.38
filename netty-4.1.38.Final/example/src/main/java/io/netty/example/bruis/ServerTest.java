package io.netty.example.bruis;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author lhy
 * @date 2021/8/19
 */
public class ServerTest {
    public static void main(String[] args) throws InterruptedException {
        //就是一个死循环，不停地检测IO事件，处理IO事件，执行任务
        //创建一个线程组:接受客户端连接   主线程
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //创建一个线程组:接受网络操作   工作线程
        //cpu核心数*2
        EventLoopGroup workerGroup=new NioEventLoopGroup();
        //是服务端的一个启动辅助类，通过给他设置一系列参数来绑定端口启动服务
        ServerBootstrap serverBootstrap=new ServerBootstrap();
        // 我们需要两种类型的人干活，一个是老板，一个是工人，老板负责从外面接活，
        // 接到的活分配给工人干，放到这里，bossGroup的作用就是不断地accept到新的连接，将新的连接丢给workerGroup来处理
        serverBootstrap.group(bossGroup,workerGroup)
                //设置使用NioServerSocketChannel作为服务器通道的实现
                .channel(NioServerSocketChannel.class)
                //设置线程队列中等待连接的个数
                .option(ChannelOption.SO_BACKLOG,128)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_SNDBUF, 65535)
                .childOption(ChannelOption.SO_RCVBUF, 65535)
                //保持活动连接状态
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                //表示一条新的连接进来之后，该怎么处理，也就是上面所说的，老板如何给工人配活
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline()
                                .addLast(new NettyEncoder(),
                                        new NettyDecoder(),
                                        new ConnectServerHandler());

                    }
                });
        System.out.println(".........server  init..........");

        // 这里就是真正的启动过程了，绑定9090端口，等待服务器启动完毕，才会进入下行代码
        ChannelFuture future = serverBootstrap.bind(9090).sync();
        System.out.println(".........server start..........");
        //等待服务端关闭socket
        future.channel().closeFuture().sync();
        // 关闭两组死循环
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public static class NettyServerHandler extends ChannelInboundHandlerAdapter {

        //读取数据事件
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("NettyServerHandler#channelRead: " + msg);
//            ByteBuf requestBuf = Unpooled.copiedBuffer("同好" + System.getProperty("line.separator"), CharsetUtil.UTF_8);
            ctx.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        System.out.println("Server服务端发送消息完成...");
                    } else {
                        System.out.println("Server服务端发送消息未完成...");
                    }
                }
            });
            super.channelRead(ctx, msg);
        }
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Netty Server channelRegistered");
            ctx.fireChannelRegistered();
        }
        //读取数据完毕事件
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.fireChannelReadComplete();
        }
        //异常发生回调
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }

    public static class ConnectServerHandler extends SimpleChannelInboundHandler<RemotingCommand> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
            RemotingCommand response = new RemotingCommand();
            response.setBody("abc".getBytes());
            response.setCode(401);
            ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
//                        System.out.println("ConnectServerHandler#channelRead0: Server服务端发送消息完成...");
                    } else {
//                        System.out.println("ConnectServerHandler#channelRead0: Server服务端发送消息未完成...");
                    }
                }
            });
        }
    }
}
