package it.cimadomo.blockchain.service.server;

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
public class TcpServerHandlerService extends ChannelInboundHandlerAdapter {

    private static final List<Channel> connectedPeers = new ArrayList<>();
    private final MempoolService mempoolService;
    private final BlockchainService blockchainService;

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
                Message message = new ObjectMapper().readValue(messageString, Message.class);
                handleMessage(ctx, message);
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


    private void handleMessage(ChannelHandlerContext ctx, Message message) throws JsonProcessingException {
        switch (message.getType()) {
            case REQUEST_MEMPOOL:
                log.info("Received request for mempool.");
                List<Transaction> mempool = mempoolService.getMempool();
                Message mempoolMessage = new Message();
                mempoolMessage.setType(MessageType.TRANSACTION);
                mempoolMessage.setPayload(new ObjectMapper().writeValueAsString(mempool));
                ctx.writeAndFlush(new ObjectMapper().writeValueAsString(mempoolMessage) + "\n");
                break;
            case TRANSACTION:
                log.info("Received transaction: {}", message.getPayload());
                Transaction transaction = new ObjectMapper().readValue(message.getPayload(), Transaction.class);
                if (mempoolService.isTransactionValid(transaction)) {
                    mempoolService.addTransaction(transaction);
                    broadcastTransaction(transaction);
                } else {
                    log.warn("Received invalid transaction: {}", transaction);
                    sendErrorMessage(ctx, "Invalid transaction received.");
                }
                break;
            case REQUEST_LATEST:
                log.info("Received request for latest block.");
                Block latestBlock = blockchainService.getLatestBlock();
                Message latestBlockMessage = new Message();
                latestBlockMessage.setType(MessageType.BLOCK);
                latestBlockMessage.setPayload(new ObjectMapper().writeValueAsString(latestBlock));
                ctx.writeAndFlush(new ObjectMapper().writeValueAsString(latestBlockMessage) + "\n");
                break;
            case BLOCK:
                log.info("Received block: {}", message.getPayload());
                Block block = new ObjectMapper().readValue(message.getPayload(), Block.class);
                Block latestBlockBlock = blockchainService.getLatestBlock();
                if (blockchainService.isBlockValid(block, latestBlockBlock)) {
                    log.info("Block added to blockchain: {}", block);
                    if(blockchainService.addBlock(block)){
                        Message rewardMessage = new Message();
                        rewardMessage.setType(MessageType.REWARD);
                        rewardMessage.setPayload(blockchainService.getReward());
                        ctx.writeAndFlush(new ObjectMapper().writeValueAsString(rewardMessage) + "\n");
                        broadcastBlock(block);
                    } else {
                        log.error("Failed to add block to blockchain");
                        sendErrorMessage(ctx, "Failed to add block to blockchain. Block might be invalid");
                    }
                } else {
                    log.error("Failed to add block to blockchain");
                    sendErrorMessage(ctx, "Failed to add block to blockchain. Block might be invalid");
                }
            default:
                log.warn("Unknown message type: {}", message.getType());
        }
    }


    private void broadcastTransaction(Transaction transaction) {
        try {
            Message transactionMessage = new Message();
            transactionMessage.setType(MessageType.TRANSACTION);
            transactionMessage.setPayload(new ObjectMapper().writeValueAsString(transaction));

            String serializedMessage = new ObjectMapper().writeValueAsString(transactionMessage) + "\n";
            for (Channel peer : connectedPeers) {
                if (peer.isActive()) {
                    peer.writeAndFlush(serializedMessage);
                    log.info("Broadcasted transaction to peer: {}", peer.remoteAddress());
                } else {
                    log.warn("Peer {} is inactive. Removing from the peer list.", peer.remoteAddress());
                    connectedPeers.remove(peer);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize transaction for broadcasting: {}", transaction, e);
        } catch (Exception e) {
            log.error("Unexpected error during transaction broadcast: {}", transaction, e);
        }
    }

    private void broadcastBlock(Block block) {
        try {
            Message blockMessage = new Message();
            blockMessage.setType(MessageType.BLOCK);
            blockMessage.setPayload(new ObjectMapper().writeValueAsString(block));

            String serializedMessage = new ObjectMapper().writeValueAsString(blockMessage) + "\n";

            for (Channel peer : connectedPeers) {
                if (peer.isActive()) {
                    peer.writeAndFlush(serializedMessage);
                    log.info("Broadcasted block to peer: {}", peer.remoteAddress());
                } else {
                    log.warn("Peer {} is inactive. Removing from the peer list.", peer.remoteAddress());
                    connectedPeers.remove(peer);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize block for broadcasting: {}", block, e);
        } catch (Exception e) {
            log.error("Unexpected error during block broadcast: {}", block, e);
        }
    }



    private void sendErrorMessage(ChannelHandlerContext ctx, String errorMessage) {
        try {
            Message errorResponse = new Message();
            errorResponse.setType(MessageType.ERROR);
            errorResponse.setPayload(errorMessage);
            String responseString = new ObjectMapper().writeValueAsString(errorResponse);
            ctx.writeAndFlush(responseString + "\n");
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response: {}", errorMessage, e);
        }
    }

}
