package com.ozy.rpc.remoting.netty4;

import com.ozy.rpc.core.annotation.RpcService;
import com.ozy.rpc.core.vo.Request;
import com.ozy.rpc.registry.RegistryService;
import com.ozy.rpc.remoting.netty4.handle.ServerHandler;
import com.ozy.rpc.serialization.RpcDecoder;
import com.ozy.rpc.serialization.RpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

public class NettyServer implements ApplicationContextAware {

    private final String host;

    private final int port;

    private final RegistryService registryService;

    public NettyServer(String host, int port, RegistryService registryService) {
        this.host = host;
        this.port = port;
        this.registryService = registryService;
    }

    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("NettyServerBoss", true));

    private final EventLoopGroup workerGroup = new NioEventLoopGroup(Math.min(Runtime.getRuntime().availableProcessors() + 1, 32),
            new DefaultThreadFactory("NettyServerWorker", true));

    private final ServerBootstrap bootstrap = new ServerBootstrap();

    private ChannelFuture future;

    public final static Map<String, Object> RPCSERVICE_MAP = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if (serviceBeanMap != null) {
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
                String serviceName = rpcService.value().getName();
                RPCSERVICE_MAP.put(serviceName, serviceBean);
            }
        }

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(
                                new RpcDecoder(Request.class),
                                new RpcEncoder(),
                                new ServerHandler()
                        );
                    }
                });
        try {
            future = bootstrap.bind(host, port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (registryService != null) {
            for (String name : RPCSERVICE_MAP.keySet()) {
                registryService.register(name, host+":"+port);
            }
        }

        future.addListener(f -> System.out.println("netty is bind "+host+":"+port));
    }

    @PreDestroy
    public void destory() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
