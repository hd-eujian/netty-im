package com.eujian.im.server;


import com.alibaba.fastjson.JSONObject;
import com.eujian.im.MsgBody;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务器的处理器
 * @Author: yeyongjian
 * @Date: 2020-05-03 23:35
 */
public class ServerHandler  extends SimpleChannelInboundHandler {

    //连接id与容器的关系
    private static Map<String, ChannelHandlerContext> map = new HashMap<>();


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Channel channel = ctx.channel();
        ChannelId id = channel.id();
        map.put(id.toString(),ctx);
        ByteBuf buf = (ByteBuf) msg;
        String recieved = getMessage(buf);
        MsgBody msgBody = JSONObject.parseObject(recieved, MsgBody.class);
        String format = String.format("服务器接收到客户端消息，发送人：%s，发送信息：%s", msgBody.getSendUserName(), msgBody.getMsg());
        System.err.println(format);

        map.forEach((k,v)->{
            try {
                if(id.toString().equals(k)){
                    return;
                }

                MsgBody sendMsgBody = new MsgBody();
                sendMsgBody.setSendUserName(msgBody.getSendUserName());
                sendMsgBody.setMsg(msgBody.getMsg());
                v.writeAndFlush(getSendByteBuf(JSONObject.toJSONString(sendMsgBody)));
                System.err.println("服务器回复消息："+JSONObject.toJSONString(sendMsgBody));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    }

    /*
     * 从ByteBuf中获取信息 使用UTF-8编码返回
     */
    private String getMessage(ByteBuf buf) {

        byte[] con = new byte[buf.readableBytes()];
        buf.readBytes(con);
        try {
            return new String(con, "UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ByteBuf getSendByteBuf(String message)
            throws UnsupportedEncodingException {

        byte[] req = message.getBytes("UTF-8");
        ByteBuf pingMessage = Unpooled.buffer();
        pingMessage.writeBytes(req);

        return pingMessage;
    }
}
