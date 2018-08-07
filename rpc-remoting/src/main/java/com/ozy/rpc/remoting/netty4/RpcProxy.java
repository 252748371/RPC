package com.ozy.rpc.remoting.netty4;

import com.ozy.rpc.core.vo.Request;
import com.ozy.rpc.core.vo.Response;
import com.ozy.rpc.registry.DiscoveryService;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.UUID;

public class RpcProxy {

    private String address;

    private DiscoveryService discoveryService;

    public RpcProxy(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, ((proxy, method, args) -> {
            Request request = new Request();
            request.setId(UUID.randomUUID().toString());
            request.setInterfaceName(method.getDeclaringClass().getName());
            request.setMethodName(method.getName());
            request.setParameterTypes(method.getParameterTypes());
            request.setParameters(args);

            if (discoveryService != null) {
                String name = interfaceClass.getName();
                address = discoveryService.discover(name);
            }
            if (address == null || address.equals("")) {
                throw new RuntimeException("server address is empty");
            }
            String[] array = address.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);

            NettyClient nettyClient = new NettyClient(host, port);
            nettyClient.send(request);
            Response response = (Response)NettyClient.RESPONSE_MAP.get(request.getId());
            return response.getResult();
        }));
    }
}
