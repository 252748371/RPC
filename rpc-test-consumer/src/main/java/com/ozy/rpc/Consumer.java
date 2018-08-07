package com.ozy.rpc;

import com.ozy.rpc.registry.ZooKeeperDiscoveryService;
import com.ozy.rpc.remoting.netty4.RpcProxy;
import com.ozy.rpc.serviceImpl.HelloService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class Consumer {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);
        RpcProxy rpcProxy = (RpcProxy) ctx.getBean("rpcProxy");
        HelloService helloService = rpcProxy.create(HelloService.class);
        for (int i = 0; i< 10 ; i++ ) {
            String world = helloService.hello("world");
            System.out.println(world);
        }
    }
}

@Configuration
class Config {

    @Bean
    public RpcProxy rpcProxy() {
        return new RpcProxy(new ZooKeeperDiscoveryService("192.168.99.100:2181"));
    }

}
