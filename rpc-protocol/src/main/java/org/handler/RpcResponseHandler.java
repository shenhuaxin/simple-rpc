package org.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import org.core.MiniRpcRequestHolder;
import org.core.MiniRpcResponse;
import org.core.RpcFuture;
import org.protocol.MsgHeader;
import org.protocol.SimpleRpcProtocol;

public class RpcResponseHandler extends SimpleChannelInboundHandler<SimpleRpcProtocol<MiniRpcResponse>> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SimpleRpcProtocol<MiniRpcResponse> msg) throws Exception {
        MsgHeader header = msg.getHeader();
        long requestId = header.getRequestId();
        RpcFuture<MiniRpcResponse> future = MiniRpcRequestHolder.REQUEST_MAP.get(requestId);
        Promise<MiniRpcResponse> promise = future.getPromise();

        promise.setSuccess(msg.getBody());
    }
}
