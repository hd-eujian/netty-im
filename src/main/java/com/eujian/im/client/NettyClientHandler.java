package com.eujian.im.client;

import com.alibaba.fastjson.JSONObject;
import com.eujian.im.MsgBody;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.UnsupportedEncodingException;

public class NettyClientHandler extends SimpleChannelInboundHandler {
    private ByteBuf firstMessage;
    private ChannelHandlerContext ctx;

    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void sendMsg(String str){
        byte[] data = str.getBytes();
        firstMessage = Unpooled.buffer();
        firstMessage.writeBytes(data);
        ctx.writeAndFlush(firstMessage);
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx= ctx;
        MsgBody msgBody = new MsgBody();
        msgBody.setSendUserName(userName);
        msgBody.setMsg("进入聊天室");
        byte[] data = JSONObject.toJSONString(msgBody).getBytes();
        firstMessage = Unpooled.buffer();
        firstMessage.writeBytes(data);
        ctx.writeAndFlush(firstMessage);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg){
        ByteBuf buf = (ByteBuf) msg;
        String rev = getMessage(buf);
        MsgBody msgBody = JSONObject.parseObject(rev, MsgBody.class);
        String format = String.format("客户端收到服务器消息，发送人：%s，发送信息：%s", msgBody.getSendUserName(), msgBody.getMsg());
        System.err.println(format);
    }

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
}
