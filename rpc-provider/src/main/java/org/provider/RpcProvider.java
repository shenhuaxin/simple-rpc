package org.provider;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.core.ServiceMeta;
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
    private String port;

    private RegistryService registryService;

    public RpcProvider(String port, RegistryService registryService) {
        this.port = port;
        this.registryService = registryService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        host = InetAddress.getLocalHost().getHostAddress();
        new Thread(() -> {
            // todo 开启服务端
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new SimpleDecoder())
                                    .addLast(new SimpleEncoder())
                                    .addLast(new RpcRequestHandler(rpcServiceMap));
                        }
                    });
        }).start();
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Service annotation = bean.getClass().getAnnotation(Service.class);
        if (annotation != null) {
            Class<?>[] interfaces = bean.getClass().getInterfaces();
            for (Class<?> anInterface : interfaces) {
                ServiceMeta serviceMeta = new ServiceMeta();
                serviceMeta.setServiceName(anInterface.getName());
                serviceMeta.setHost(this.host);
                serviceMeta.setPort(this.port);
                registryService.register(serviceMeta);
                rpcServiceMap.put(String.join("#", anInterface.getName(), annotation.version()), bean);
            }
        }
        return bean;
    }
}
