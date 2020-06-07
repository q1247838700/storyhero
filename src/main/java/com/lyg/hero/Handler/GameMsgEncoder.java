package com.lyg.hero.Handler;

import com.google.protobuf.GeneratedMessageV3;
import com.lyg.hero.msg.GameMsgProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import sun.font.FontManagerForSGE;

/**
 * 消息编码
 *
 * @author lyg
 * @create 2020-06-07-16:49
 */
public class GameMsgEncoder extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg == null || !(msg instanceof GeneratedMessageV3)) {
            super.write(ctx, msg, promise);
            return;
        }
        int msgCode = -1;
        if (msg instanceof GameMsgProtocol.UserEntryResult) {
            msgCode = GameMsgProtocol.MsgCode.USER_ENTRY_RESULT_VALUE;
        } else if (msg instanceof GameMsgProtocol.UserMoveToResult) {
            msgCode = GameMsgProtocol.MsgCode.USER_MOVE_TO_RESULT_VALUE;
        } else if (msg instanceof GameMsgProtocol.WhoElseIsHereResult) {
            msgCode = GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_RESULT_VALUE;

        } else if (msg instanceof GameMsgProtocol.UserQuitResult) {
            msgCode = GameMsgProtocol.MsgCode.USER_QUIT_RESULT_VALUE;
        } else {
            System.out.println("无法识别的类型");
        }
        //将消息转化为二进制流
        byte[] msgBody = ((GeneratedMessageV3) msg).toByteArray();
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeShort(0);
        buffer.writeShort(msgCode);
        buffer.writeBytes(msgBody);
        //生成二进制socket框架类
        BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
        //写出就完事了
        super.write(ctx, frame, promise);
    }
}
