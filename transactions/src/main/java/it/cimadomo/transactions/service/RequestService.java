package it.cimadomo.transactions.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import it.cimadomo.transactions.model.protocol.Message;
import it.cimadomo.transactions.model.protocol.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class RequestService extends ChannelInboundHandlerAdapter {

    private final ChannelFuture channelFuture;
    private final ObjectMapper objectMapper;
    private CompletableFuture<String> responseFuture;

    public RequestService(@Lazy ChannelFuture channelFuture, ObjectMapper objectMapper) {
        this.channelFuture = channelFuture;
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<String> sendRequest(MessageType type, Object payload) {
        responseFuture = new CompletableFuture<>();
        try {
            Message messageObject = new Message();
            messageObject.setType(type);
            messageObject.setPayload((String) payload);
            String message = objectMapper.writeValueAsString(messageObject);
            if (channelFuture.channel().isActive()) {
                channelFuture.channel().writeAndFlush(message + "\n");
                log.debug("Sent request: {}", message);
            } else {
                log.error("Channel is not active. Cannot send request.");
                responseFuture.completeExceptionally(new IllegalStateException("Channel is not active."));
            }
        } catch (Exception e) {
            log.error("Failed to send request", e);
            responseFuture.completeExceptionally(e);
        }
        return responseFuture;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            String response = (String) msg;
            log.debug("Received response: {}", response);
            if (responseFuture != null) {
                responseFuture.complete(response);
            }
        } catch (Exception e) {
            log.error("Failed to process response", e);
            if (responseFuture != null) {
                responseFuture.completeExceptionally(e);
            }
        }
    }
}
