package it.cimadomo.transactions.config;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import it.cimadomo.transactions.service.RequestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TcpConfig {

    @Value("${blockchain.node-host}")
    private String serverHost;

    @Value("${blockchain.node-port}")
    private int serverPort;

    @Bean
    public Bootstrap bootstrap(RequestService requestService) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        return new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_RCVBUF, 1048576)
                .option(ChannelOption.SO_SNDBUF, 1048576)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new DelimiterBasedFrameDecoder(65536, Unpooled.wrappedBuffer("\n".getBytes())));
                        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
                        pipeline.addLast(requestService);
                    }
                });
    }

    @Bean
    public ChannelFuture tcpClient(Bootstrap bootstrap) throws InterruptedException {
        return bootstrap.connect(serverHost, serverPort).sync();
    }
}
