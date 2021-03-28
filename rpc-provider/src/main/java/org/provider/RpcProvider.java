package org.provider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.codec.SimpleDecoder;
import org.core.ServiceMeta;
import org.codec.SimpleEncoder;
import org.handler.RpcRequestHandler;
import org.provider.annotation.Service;
import org.registry.RegistryService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RpcProvider implements InitializingBean, BeanPostProcessor {


    private final Map<String, Object> rpcServiceMap = new HashMap<>();

    private String host;
    private int port;

    private RegistryService registryService;

    public RpcProvider(int port, RegistryService registryService) {
        this.port = port;
        this.registryService = registryService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        host = InetAddress.getLocalHost().getHostAddress();
        new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline()
                                        .addLast(new SimpleEncoder())
                                        .addLast(new SimpleDecoder())
                                        .addLast(new RpcRequestHandler(rpcServiceMap));
                            }
                        });
                ChannelFuture channelFuture = bootstrap.bind(this.host, this.port).sync();
                log.info("server addr {} started on port {}", this.host, this.port);
                System.out.println("服务端启动完成");
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Service annotation = bean.getClass().getAnnotation(Service.class);
        if (annotation != null) {
            Class<?>[] interfaces = bean.getClass().getInterfaces();
            try {
                for (Class<?> anInterface : interfaces) {
                    ServiceMeta serviceMeta = new ServiceMeta();
                    serviceMeta.setServiceName(anInterface.getName());
                    serviceMeta.setHost(this.host);
                    serviceMeta.setPort(this.port);
                    registryService.register(serviceMeta);
                    rpcServiceMap.put(anInterface.getName(), bean);
                }
            }catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return bean;
    }
}
