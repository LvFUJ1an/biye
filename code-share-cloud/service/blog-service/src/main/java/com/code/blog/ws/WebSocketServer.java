package com.code.blog.ws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    // 存储用户会话信息（线程安全）
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    // 存储在线用户信息（用户ID为key）
    private static final ConcurrentHashMap<Integer, OnlineUser> onlineUsers = new ConcurrentHashMap<>();

    /**
     * 发送评论通知给目标用户
     * @param toUserId      被评论的用户ID
     * @param fromUser      评论者信息
     * @param content       评论内容
     * @param targetType    被评论目标类型
     * @param targetId      被评论目标ID
     */
    public static void sendCommentNotification(
            Integer toUserId,
            OnlineUser fromUser,
            String content,
            String targetType,
            Integer targetId
    ) {
        String targetUserKey = String.valueOf(toUserId);
        Session targetSession = sessionMap.get(targetUserKey);

        if (targetSession != null && targetSession.isOpen()) {
            JSONObject notification = new JSONObject();
            notification.put("type", "comment");
            notification.put("fromUserId", fromUser.getSenderId());
            notification.put("fromUserName", fromUser.getSenderName());
            notification.put("fromUserAvatar", fromUser.getSenderAvatar());
            notification.put("content", content);
            notification.put("targetType", targetType);
            notification.put("targetId", targetId);
            notification.put("timestamp", System.currentTimeMillis());

            sendNotificationSafely(targetSession, notification, targetUserKey);
        }
    }



    /**
     * 发送点赞通知给目标用户
     * @param toUserId      被点赞的用户ID
     * @param fromUser      点赞者信息
     * @param targetType    被点赞目标类型
     * @param targetId      被点赞目标ID
     */
    public static void sendLikeNotification(
            Integer toUserId,
            OnlineUser fromUser,
            String targetType,
            Integer targetId
    ) {
        String targetUserKey = String.valueOf(toUserId);
        Session targetSession = sessionMap.get(targetUserKey);

        if (targetSession != null && targetSession.isOpen()) {
            JSONObject notification = new JSONObject();
            notification.put("type", "like");
            notification.put("fromUserId", fromUser.getSenderId());
            notification.put("fromUserName", fromUser.getSenderName());
            notification.put("fromUserAvatar", fromUser.getSenderAvatar());
            notification.put("targetType", targetType);
            notification.put("targetId", targetId);
            notification.put("timestamp", System.currentTimeMillis());

            sendNotificationSafely(targetSession, notification, targetUserKey);
        }
    }

    /**
     * 安全发送消息（复用逻辑）
     */
    private static void sendNotificationSafely(Session session, JSONObject notification, String userKey) {
        synchronized (session) {
            try {
                session.getBasicRemote().sendText(notification.toJSONString());
            } catch (Exception e) {
                System.err.println("通知发送失败: " + e.getMessage());
                sessionMap.remove(userKey);
            }
        }
    }


    /**
     * 发送关注通知给被关注的用户
     * @param targetUserId 被关注的用户ID
     * @param fromUser 关注者的用户信息
     */
    public static void sendFollowNotification(Integer targetUserId, OnlineUser fromUser) {
        String targetUserSessionKey = String.valueOf(targetUserId);
        Session targetSession = sessionMap.get(targetUserSessionKey);

        if (targetSession != null && targetSession.isOpen()) {
            JSONObject notification = new JSONObject();
            notification.put("type", "follow");
            notification.put("fromUserId", fromUser.getSenderId());
            notification.put("fromUserName", fromUser.getSenderName());
            notification.put("fromUserAvatar", fromUser.getSenderAvatar());
            notification.put("timestamp", System.currentTimeMillis());

            // 安全发送消息
            synchronized (targetSession) {
                try {
                    targetSession.getBasicRemote().sendText(notification.toJSONString());
                } catch (Exception e) {
                    System.err.println("关注通知发送失败: " + e.getMessage());
                    sessionMap.remove(targetUserSessionKey); // 清理无效连接
                }
            }
        } else {
            // 用户离线时存储到数据库（可选）
            System.out.println("用户" + targetUserId + "当前不在线");
        }
    }



    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String userId) {
        try {
            Long uid = Long.parseLong(userId);
            sessionMap.put(userId, session);
            System.out.println("客户端连接建立：" + userId);
        } catch (NumberFormatException e) {
            System.out.println("非法的用户ID格式");
        }
    }

    @OnMessage
    public void onMessage(String message, @PathParam("sid") Integer userId) {
        JSONObject msgObj = JSON.parseObject(message);
        switch (msgObj.getString("type")) {
            case "join":
                handleJoin(msgObj, userId);
                break;
            case "message":
                handleMessage(msgObj, userId);
                break;
            case "leave":
                handleLeave(userId);
                break;
        }
    }

    @OnClose
    public void onClose(@PathParam("sid") Integer userId) {
        handleLeave(userId);
        sessionMap.remove(userId);
        System.out.println("连接关闭：" + userId);
    }

    private void handleJoin(JSONObject msg, Integer userId) {
        OnlineUser user = new OnlineUser(
                userId,
                msg.getString("senderName"),
                msg.getString("senderAvatar")
        );
        onlineUsers.put(userId, user);
        broadcastOnlineUsers();
        broadcastSystemMessage(user.getSenderName() + " 加入了聊天室");
    }

    private void handleMessage(JSONObject msg, Integer userId) {
        msg.put("timestamp", System.currentTimeMillis());
        broadcastMessage(msg.toJSONString());
    }

    private void handleLeave(Integer userId) {
        OnlineUser user = onlineUsers.get(userId);
        if (user != null) {
            onlineUsers.remove(userId);
            broadcastOnlineUsers();
            broadcastSystemMessage(user.getSenderName() + " 离开了聊天室");
        }
    }

    // 广播在线用户列表
    private void broadcastOnlineUsers() {
        JSONObject msg = new JSONObject();
        msg.put("type", "onlineUsers");
        msg.put("users", new ArrayList<>(onlineUsers.values()));
        broadcastMessage(msg.toJSONString());
    }

    // 广播系统消息
    private void broadcastSystemMessage(String content) {
        JSONObject msg = new JSONObject();
        msg.put("type", "system");
        msg.put("content", content);
        msg.put("timestamp", System.currentTimeMillis());
        broadcastMessage(msg.toJSONString());
    }

    // 通用广播方法
    private void broadcastMessage(String message) {
        sessionMap.forEach((id, session) -> {
            if (session.isOpen()) {
                synchronized (session) {
                    try {
                        session.getBasicRemote().sendText(message); // 同步发送
                    } catch (Exception e) {
                        System.err.println("消息发送失败: " + e.getMessage());
                        sessionMap.remove(id); // 清理无效连接
                    }
                }
            }
        });
    }

    // 在线用户数据类
    public static class OnlineUser {
        private final Integer senderId;
        private final String senderName;
        private final String senderAvatar;

        public OnlineUser(Integer senderId, String senderName, String senderAvatar) {
            this.senderId = senderId;
            this.senderName = senderName;
            this.senderAvatar = senderAvatar;
        }

        // getters
        public Integer getSenderId() { return senderId; }
        public String getSenderName() { return senderName; }
        public String getSenderAvatar() { return senderAvatar; }
    }
}