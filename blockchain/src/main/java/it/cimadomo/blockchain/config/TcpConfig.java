package it.cimadomo.blockchain.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import it.cimadomo.blockchain.service.blockchain.BlockchainService;
import it.cimadomo.blockchain.service.blockchain.MempoolService;
import it.cimadomo.blockchain.service.client.TcpClientHandlerService;
import it.cimadomo.blockchain.service.server.TcpServerHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class TcpConfig {

    private final MempoolService mempoolService;
    private final BlockchainService blockchainService;

    @Value("${blockchain.server.host}")
    private String serverHost;
    @Value("${blockchain.server.port}")
    private int serverPort;

    @Bean
    public ChannelFuture tcpServer(){
        NioEventLoopGroup tcpBossGroup = new NioEventLoopGroup();
        NioEventLoopGroup tcpWorkerGroup = new NioEventLoopGroup();
        ServerBootstrap tcpBootstrap = new ServerBootstrap();
        tcpBootstrap.group(tcpBossGroup, tcpWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 1048576)
                .option(ChannelOption.SO_SNDBUF, 1048576)
                .childOption(ChannelOption.SO_RCVBUF, 1048576)
                .childOption(ChannelOption.SO_SNDBUF, 1048576)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new DelimiterBasedFrameDecoder(65536, Unpooled.wrappedBuffer("\n".getBytes())));
                        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
                        pipeline.addLast(new TcpServerHandlerService(mempoolService, blockchainService));
                    }
                });
        return tcpBootstrap.bind(new InetSocketAddress(serverHost, serverPort));
    }

    @Bean
    public Bootstrap tcpClientBootstrap(ObjectMapper objectMapper, BlockchainService blockchainService, MempoolService mempoolService) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        return new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                                new DelimiterBasedFrameDecoder(65536, Unpooled.wrappedBuffer("\n".getBytes())),
                                new StringDecoder(CharsetUtil.UTF_8),
                                new StringEncoder(CharsetUtil.UTF_8),
                                new TcpClientHandlerService(objectMapper, blockchainService, mempoolService)
                        );
                    }
                });
    }


}
