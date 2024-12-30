package it.cimadomo.blockchain.service;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import it.cimadomo.blockchain.model.protocol.Message;
import it.cimadomo.blockchain.model.protocol.MessageType;
import it.cimadomo.blockchain.service.client.TcpClientHandlerService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BootstrapService {

    private final TcpClientHandlerService tcpClientHandlerService;
    private final ChannelFuture tcpServer;
    private final Bootstrap tcpClientBootstrap;
    private Channel tcpServerChannel;
    private Channel tcpClientChannel;

    @Value("${blockchain.peers}")
    private String initialPeers;
    @Value("${blockchain.genesis-node}")
    private boolean genesisNode;

    @PostConstruct
    public void init() {
        try {
            startServer();
            connectToPeers();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void startServer() throws InterruptedException {
        log.info("Bootstrapping blockchain...");
        tcpServer.sync();
        tcpServerChannel = tcpServer.channel();
        log.info("TCP server started on address: {}", tcpServerChannel.localAddress());
    }

    private void connectToPeers() {
        String[] knownPeers = initialPeers.split(",");
        
        for (String peer : knownPeers) {
            try {
                String[] hostPort = peer.split(":");
                String host = hostPort[0];
                int port = Integer.parseInt(hostPort[1]);
                log.info("Connecting to peer: {}:{}", host, port);
                ChannelFuture client = tcpClientBootstrap.connect(host, port).sync();
                log.info("Connected to peer: {}", client.channel().remoteAddress());
                if(!genesisNode){
                    requestBlockchain();
                    requestMempool();
                }
            } catch (InterruptedException e) {
                log.warn("Failed to connect to peer {}: {}", peer, e.getMessage());
            }
        }
    }

    private void requestMempool() {
        try {
            log.info("Requesting mempool from peer...");
            Message requestMempoolMessage = new Message();
            requestMempoolMessage.setType(MessageType.REQUEST_MEMPOOL);
            tcpClientHandlerService.sendMessage(tcpClientChannel, requestMempoolMessage);
            log.info("Mempool request sent.");
        } catch (Exception e) {
            log.error("Failed to request mempool", e);
        }
    }
    
    private void requestBlockchain() {
        try {
            log.info("Requesting blockchain from peer...");
            Message requestBlockchain = new Message();
            requestBlockchain.setType(MessageType.REQUEST_BLOCKCHAIN);
            tcpClientHandlerService.sendMessage(tcpClientChannel, requestBlockchain);
            log.info("Blockchain request sent.");
        } catch (Exception e) {
            log.error("Failed to request blockchain", e);
        }
    }
}