package org.consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.codec.SimpleDecoder;
import org.codec.SimpleEncoder;
import org.core.MiniRpcRequest;
import org.core.ServiceMeta;
import org.handler.RpcResponseHandler;
import org.protocol.SimpleRpcProtocol;
import org.registry.RegistryService;

@Slf4j
public class RpcConsumer {


    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public RpcConsumer() {
        this.bootstrap = new Bootstrap();
        this.eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new SimpleEncoder())
                                .addLast(new SimpleDecoder())
                                .addLast(new RpcResponseHandler());
                    }
                });
    }


    public void sendRequest(SimpleRpcProtocol<MiniRpcRequest> protocol, RegistryService registryService) throws Exception {
        MiniRpcRequest request = protocol.getBody();

        ServiceMeta serviceMeta = registryService.discovery(request.getClassName(), 0);

        if (serviceMeta != null) {
            ChannelFuture future = bootstrap.connect(serviceMeta.getHost(), serviceMeta.getPort()).sync();
            future.addListener(args0 -> {
                if (future.isSuccess()) {
                }else {
                    log.error("connect rpc server {} on port {} failed.");
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            });
            future.channel().writeAndFlush(protocol);
        }
    }
}
