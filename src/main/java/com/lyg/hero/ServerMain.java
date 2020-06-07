package com.lyg.hero;

import com.lyg.hero.Handler.GameMsgDecoder;
import com.lyg.hero.Handler.GameMsgEncoder;
import com.lyg.hero.Handler.GameMsgHandler;
import com.sun.corba.se.spi.orbutil.threadpool.NoSuchWorkQueueException;
import com.sun.org.apache.xerces.internal.util.EntityResolverWrapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * @author lyg
 * @create 2020-06-06-15:45
 */
public class ServerMain {
    public static void main(String[] args) {
        //创建两个线程池
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        //初始化服务器启动类
        ServerBootstrap bootstrap = new ServerBootstrap();
        //添加线程池
        bootstrap.group(bossGroup, workGroup);
        //添加通道类型
        bootstrap.channel(NioServerSocketChannel.class);
        //添加消息处理类(一个连接抽象成一个channel)
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                        //http服务器编解码器
                        new HttpServerCodec(),
                        //内容长度限制
                        new HttpObjectAggregator(65535),
                        //websocket协议处理器
                        new WebSocketServerProtocolHandler("/websocket"),
                        new GameMsgDecoder(),

                        new GameMsgEncoder(),
                        new GameMsgHandler()
                );
            }
        });
        try {
            ChannelFuture future = bootstrap.bind(12345).sync();
            if (future.isSuccess()) {
                System.out.println("服务器启动成功");
            }
            // 等待服务器信道关闭,
            // 也就是不要退出应用程序,
            // 让应用程序可以一直提供服务
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
