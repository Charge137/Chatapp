package com.example.demo;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

public class ChatHandler extends TextWebSocketHandler {

    private static final Map<WebSocketSession, String> users = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // まだ名前未登録
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String msg = message.getPayload();

        // 入室
        if (msg.startsWith("join:")) {
            String name = msg.substring(5);
            users.put(session, name);

            broadcast("system:" + name + " が入室しました");
            return;
        }

        // 名前変更
        if (msg.startsWith("rename:")) {
            String name = msg.substring(7);
            users.put(session, name);
            return;
        }

        // ❗削除（ここが重要：退出と絶対に混ぜない）
        if (msg.startsWith("delete:")) {
            String id = msg.substring(7);

            // 削除イベントだけ送る（systemにしない）
            broadcast("delete:" + id);
            return;
        }

        // 通常メッセージ
        String name = users.get(session);
        if (name == null) return;

        String id = String.valueOf(System.currentTimeMillis());

        broadcast(id + ":" + name + " : " + msg);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        String name = users.remove(session);

        if (name != null) {
            broadcast("system:" + name + " が退出しました");
        }
    }

    private void broadcast(String msg) throws Exception {
        for (WebSocketSession s : users.keySet()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(msg));
            }
        }
    }
}