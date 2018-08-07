package com.ozy.rpc.serviceImpl;

import com.ozy.rpc.core.annotation.RpcService;

@RpcService(value = HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
