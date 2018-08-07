package com.ozy.rpc.remoting.netty4.handle;

import com.ozy.rpc.core.vo.Response;
import com.ozy.rpc.remoting.netty4.NettyClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<Response> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
        NettyClient.RESPONSE_MAP.put(response.getId(), response);
    }
}
