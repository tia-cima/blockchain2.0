package it.cimadomo.blockchain.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import it.cimadomo.blockchain.model.Block;
import it.cimadomo.blockchain.model.Transaction;
import it.cimadomo.blockchain.model.protocol.Message;
import it.cimadomo.blockchain.model.protocol.MessageType;
import it.cimadomo.blockchain.service.blockchain.BlockchainService;
import it.cimadomo.blockchain.service.blockchain.MempoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TcpClientHandlerService extends ChannelInboundHandlerAdapter {

    private final ObjectMapper objectMapper;
    private final BlockchainService blockchainService;
    private final MempoolService mempoolService;
    private static final List<Channel> connectedPeers = new ArrayList<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        connectedPeers.add(ctx.channel());
        log.debug("New peer connected: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        connectedPeers.remove(ctx.channel());
        log.debug("Peer disconnected: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof String messageString) {
            try {
                Message message = objectMapper.readValue(messageString, Message.class);
                handleMessage(message);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse incoming message: {}", messageString, e);
                sendErrorMessage(ctx, "Error parsing incoming message: " + e.getOriginalMessage());
            } catch (Exception e) {
                log.error("Unexpected error while processing message: {}", messageString, e);
                sendErrorMessage(ctx, "Unexpected error: " + e.getMessage());
            }
        } else {
            log.warn("Received unexpected message type: {}", msg.getClass().getName());
            sendErrorMessage(ctx, "Unsupported message type received.");
        }
    }

    private void handleMessage(Message message) throws JsonProcessingException {
        switch (message.getType()) {
            case REQUEST_MEMPOOL:
                log.info("Received request for mempool.");
                List<Transaction> transactions = objectMapper.readValue(
                        message.getPayload(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Transaction.class)
                );
                log.info("Adding received transactions to mempool: {}", transactions);
                for (Transaction transaction : transactions) {
                    if (mempoolService.isTransactionValid(transaction)) {
                        mempoolService.addTransaction(transaction);
                    } else {
                        log.warn("Invalid transaction received: {}", transaction);
                    }
                }
                break;
                
            case REQUEST_BLOCKCHAIN:
                log.info("Received request for blockchain.");
                List<Block> blocks = objectMapper.readValue(
                        message.getPayload(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Block.class)
                );
                blockchainService.setBlockchain(blocks);
                log.info("Blockchain set to: {}", blocks);
                break;

            case ERROR:
                log.error("Received error message: {}", message.getPayload());
                break;

            default:
                log.warn("Unknown message type: {}", message.getType());
        }
    }

    public void sendMessage(Channel channel, Message message) {
        try {
            String serializedMessage = objectMapper.writeValueAsString(message);
            channel.writeAndFlush(serializedMessage + "\n");
            log.info("Message sent: {}", message);
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
    }

    private void sendErrorMessage(ChannelHandlerContext ctx, String errorMessage) {
        try {
            Message errorResponse = new Message();
            errorResponse.setType(MessageType.ERROR);
            errorResponse.setPayload(errorMessage);
            String responseString = objectMapper.writeValueAsString(errorResponse);
            ctx.writeAndFlush(responseString + "\n");
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response: {}", errorMessage, e);
        }
    }
}
