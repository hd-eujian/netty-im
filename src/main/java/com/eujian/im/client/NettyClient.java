package com.eujian.im.client;

import com.alibaba.fastjson.JSONObject;
import com.eujian.im.MsgBody;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Scanner;

public class NettyClient {
    public NettyClientHandler nettyClientHandler;
    /*
     * 服务器端口号
     */
    private int port;

    private String sendUserName;
    /*
     * 服务器IP
     */
    private String host;

    public NettyClient(int port, String host, String sendUserName) throws InterruptedException {
        this.port = port;
        this.host = host;
        this.sendUserName = sendUserName;
        start();
    }

    private void start() throws InterruptedException {

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.group(eventLoopGroup);
            bootstrap.remoteAddress(host, port);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel)
                        throws Exception {
                    nettyClientHandler = new NettyClientHandler();
                    nettyClientHandler.setUserName(sendUserName);
                    socketChannel.pipeline().addLast(nettyClientHandler);
                }
            });
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            if (channelFuture.isSuccess()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Scanner sc = new Scanner(System.in);
                        while (sc.hasNext()){

                            MsgBody msgBody = new MsgBody();
                            msgBody.setSendUserName(sendUserName);
                            msgBody.setMsg(sc.next());
                            nettyClientHandler.sendMsg(JSONObject.toJSONString(msgBody));
                        }
                    }
                }).start();
                System.err.println(sendUserName+"连接服务器成功");
            }
            channelFuture.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }


}
