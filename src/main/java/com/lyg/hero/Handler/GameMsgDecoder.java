package com.lyg.hero.Handler;

import com.google.protobuf.GeneratedMessageV3;
import com.lyg.hero.msg.GameMsgProtocol;
import com.sun.xml.internal.ws.wsdl.writer.document.StartWithExtensionsType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * @author lyg
 * @create 2020-06-06-23:10
 */
public class GameMsgDecoder extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //防御编程
        if (!(msg instanceof BinaryWebSocketFrame)) {
            return;
        }
        //将消息转化为bytebuf
        BinaryWebSocketFrame socketFrame = (BinaryWebSocketFrame) msg;
        ByteBuf byteBuf = socketFrame.content();

        //提取消息长度
        byteBuf.readShort();
        //读取消息类型
        short msgType = byteBuf.readShort();
        //读取消息体
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        GeneratedMessageV3 cmd = null;

        switch (msgType) {
            //传来的消息是用户消息
            case GameMsgProtocol.MsgCode.USER_ENTRY_CMD_VALUE:
                cmd = GameMsgProtocol.UserEntryCmd.parseFrom(bytes);
                break;
            //移动解析
            case GameMsgProtocol.MsgCode.USER_MOVE_TO_CMD_VALUE:
                cmd = GameMsgProtocol.UserMoveToCmd.parseFrom(bytes);
                break;
            //攻击移动
            case GameMsgProtocol.MsgCode.USER_ATTK_CMD_VALUE:
                cmd = GameMsgProtocol.UserAttkCmd.parseFrom(bytes);
                break;
            case GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_CMD_VALUE:
                cmd = GameMsgProtocol.WhoElseIsHereCmd.parseFrom(bytes);
                break;
        }


        //赋予值
        if (null != cmd || !"".equals(cmd)) {
            ctx.fireChannelRead(cmd);
        }

    }
}
