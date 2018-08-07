package com.ozy.rpc;

import com.ozy.rpc.registry.ZooKeeperRegistryService;
import com.ozy.rpc.remoting.netty4.NettyServer;
import org.springframework.context.annotation.*;

import java.io.IOException;
import java.util.Map;

public class Provider {

    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);
        NettyServer nettyServer = (NettyServer) ctx.getBean("nettyServer");
        Map<String, Object> handlerMap = NettyServer.RPCSERVICE_MAP;
        System.in.read();
    }
}

@Configuration
@ComponentScan(basePackages = "com.ozy.rpc.serviceImpl")
class Config {

    @Bean
    public NettyServer nettyServer() {
        return new NettyServer("127.0.0.1", 9000, new ZooKeeperRegistryService("192.168.99.100:2181"));
    }
}
