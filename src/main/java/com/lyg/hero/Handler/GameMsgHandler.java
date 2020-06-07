package com.lyg.hero.Handler;

import com.lyg.hero.entity.User;
import com.lyg.hero.msg.GameMsgProtocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;

import io.netty.util.AttributeKey;

import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lyg
 * @create 2020-06-07-12:13
 */
public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {
    /**
     * 所有的group
     */
    private static ChannelGroup allGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    /**
     * 所有的用户
     */
    private static Map<Integer, User> userMap = new HashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        //这里将活跃的用户放入用户端信道数组里面
        allGroup.add(ctx.channel());
    }

    /**
     * 请求离开的方法(刷新||掉线)
     *
     * @param ctx 环境
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        //将信道移除
        allGroup.remove(ctx.channel());

        //将用户列表移除
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        userMap.remove(userId);

    }

    /**
     * 读取信号
     *
     * @param ctx 环境
     * @param msg 传入消息
     * @throws Exception 抛异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("接收到的消息类型是:" + msg.getClass().getName() + "消息是:" + msg);

        //用户型消息
        if (msg instanceof GameMsgProtocol.UserEntryCmd) {
            //构建一个用户
            GameMsgProtocol.UserEntryCmd userEntryCmd = (GameMsgProtocol.UserEntryCmd) msg;
            Integer userId = userEntryCmd.getUserId();
            String heroAvatar = userEntryCmd.getHeroAvatar();
            User user = new User(userId, heroAvatar);
            //放入用户缓冲
            userMap.put(userId, user);
            //将用户附着在channel上
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(userId);
            //创建发送消息
            GameMsgProtocol.UserEntryResult.Builder resultBuilder = GameMsgProtocol.UserEntryResult.newBuilder();
            resultBuilder.setUserId(userId);
            resultBuilder.setHeroAvatar(heroAvatar);
            GameMsgProtocol.UserEntryResult entryResult = resultBuilder.build();
            System.out.println(userId + "加入用户组");
            //广播消息
            allGroup.writeAndFlush(entryResult);
        } else if (msg instanceof GameMsgProtocol.UserMoveToCmd) {

            //从通道里面取出是谁移动的消息
            Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
            if (userId == null) {
                return;
            }
            System.out.println(userId + "移动中");
            //移动性消息实例
            GameMsgProtocol.UserMoveToCmd moveToCmd = (GameMsgProtocol.UserMoveToCmd) msg;
            float moveX = moveToCmd.getMoveToPosX();
            float moveY = moveToCmd.getMoveToPosY();
            //封装发送结果
            GameMsgProtocol.UserMoveToResult.Builder moveBuilder = GameMsgProtocol.UserMoveToResult.newBuilder();
            moveBuilder.setMoveUserId(userId);
            moveBuilder.setMoveToPosX(moveX);
            moveBuilder.setMoveToPosY(moveY);
            GameMsgProtocol.UserMoveToResult moveToResult = moveBuilder.build();

            //广播消息
            allGroup.writeAndFlush(moveToResult);
        } else if (msg instanceof GameMsgProtocol.WhoElseIsHereCmd) {
            //查询当前用户
            GameMsgProtocol.WhoElseIsHereResult.Builder whoIsHereResult = GameMsgProtocol.WhoElseIsHereResult.newBuilder();
            //循环用户

            for (User user : userMap.values()) {
                if (user == null) {
                    continue;
                }
                GameMsgProtocol.WhoElseIsHereResult.UserInfo.Builder userInfoBuild = GameMsgProtocol.WhoElseIsHereResult.UserInfo.newBuilder();
                userInfoBuild.setUserId(user.getUserId());
                userInfoBuild.setHeroAvatar(user.getHeroAvatar());
                whoIsHereResult.addUserInfo(userInfoBuild);

            }
            GameMsgProtocol.WhoElseIsHereResult hereResult = whoIsHereResult.build();
            ctx.writeAndFlush(hereResult);
        }


    }
}
