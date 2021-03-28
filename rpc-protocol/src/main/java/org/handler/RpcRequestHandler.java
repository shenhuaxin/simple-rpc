package org.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.core.MiniRpcRequest;
import org.core.MiniRpcResponse;
import org.protocol.MsgHeader;
import org.protocol.MsgStatus;
import org.protocol.MsgType;
import org.protocol.SimpleRpcProtocol;
import org.springframework.cglib.reflect.FastClass;

import java.util.Map;

@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<SimpleRpcProtocol<MiniRpcRequest>> {

    private final Map<String, Object> rpcServiceMap;

    public RpcRequestHandler(Map<String, Object> rpcServiceMap) {
        this.rpcServiceMap = rpcServiceMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SimpleRpcProtocol<MiniRpcRequest> protocol) throws Exception {
        RpcRequestProcessor.submitRequest(() -> {
            SimpleRpcProtocol<MiniRpcResponse> resProtocol = new SimpleRpcProtocol<>();
            MiniRpcResponse response = new MiniRpcResponse();
            MsgHeader header = protocol.getHeader();
            header.setMsgType((byte) MsgType.RESPONSE.getType());

            try {
                Object result = handle(protocol.getBody());
                response.setData(result);

                header.setStatus((byte) MsgStatus.SUCCESS.getCode());

                resProtocol.setHeader(header);
                resProtocol.setBody(response);

            }catch (Throwable throwable) {
                System.out.println(throwable.getMessage());
                header.setStatus((byte) MsgStatus.FAIL.getCode());
                response.setMessage(throwable.toString());

                resProtocol.setHeader(header);
                resProtocol.setBody(response);
            }
            ctx.writeAndFlush(resProtocol);
        });
    }


    private Object handle(MiniRpcRequest request) throws Throwable {
        String className = request.getClassName();

        Object serviceBean = rpcServiceMap.get(className);

        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }

        Class<?> serviceClass = serviceBean.getClass();

        String methodName = request.getMethodName();
        Object[] params = request.getParams();
        Class<?>[] parameterTypes = request.getParameterTypes();

        FastClass fastClass = FastClass.create(serviceClass);
        int index = fastClass.getIndex(methodName, parameterTypes);

        return fastClass.invoke(index, serviceBean, params);
    }
}
