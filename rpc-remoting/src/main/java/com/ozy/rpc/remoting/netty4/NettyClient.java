package com.ozy.rpc.remoting.netty4;

import com.ozy.rpc.core.vo.Request;
import com.ozy.rpc.core.vo.Response;
import com.ozy.rpc.remoting.netty4.handle.ClientHandler;
import com.ozy.rpc.serialization.RpcDecoder;
import com.ozy.rpc.serialization.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyClient {

    private final String host;

    private final int port;

    public final static Map<String, Object> RESPONSE_MAP = new ConcurrentHashMap<>();

    private final static int nThreads = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);

    private final NioEventLoopGroup group = new NioEventLoopGroup(nThreads, new DefaultThreadFactory("NettyClientWorker", true));

    private final Bootstrap bootstrap = new Bootstrap();

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void send(Request request) throws InterruptedException {
        bootstrap.group(group)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(
                                new RpcDecoder(Response.class),
                                new RpcEncoder(),
                                new ClientHandler());
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            Channel channel = future.channel();
            channel.writeAndFlush(request).sync();
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
