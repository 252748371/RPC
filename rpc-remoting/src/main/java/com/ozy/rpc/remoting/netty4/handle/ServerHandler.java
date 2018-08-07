package com.ozy.rpc.remoting.netty4.handle;

import com.ozy.rpc.core.vo.Request;
import com.ozy.rpc.core.vo.Response;
import com.ozy.rpc.remoting.netty4.NettyServer;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

public class ServerHandler extends SimpleChannelInboundHandler<Request> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        System.out.println(request.toString());
        String k = request.getInterfaceName();
        Object service = NettyServer.RPCSERVICE_MAP.get(k);
        if (service == null) {
            throw new RuntimeException(String.format("can not find service by key: %s", k));
        }
        Class<?> serviceClass = service.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        Method method = serviceClass.getMethod(methodName, parameterTypes);
        Object result = method.invoke(service, parameters);
        Response response = new Response();
        response.setId(request.getId());
        response.setResult(result);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
