import { useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import { useAuth } from "./useAuth";
import { resolveWsUrl } from "../utils/wsUrl";

// 채팅방 화면에 실시간 메시지/상태 변경(수락/채택/종료)을 반영하기 위한 전용 STOMP 연결.
// 알림 토스트(NotificationProvider)와는 별개의 구독으로, 이 페이지에 머무는 동안만 유지된다.
export function useChatSocket({ onMessage, onRoomUpdate }) {
  const { accessToken } = useAuth();
  const clientRef = useRef(null);

  useEffect(() => {
    if (!accessToken) return undefined;

    const client = new Client({
      brokerURL: resolveWsUrl(),
      connectHeaders: { Authorization: `Bearer ${accessToken}` },
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe("/user/queue/chat-messages", (message) => {
          onMessage?.(JSON.parse(message.body));
        });
        client.subscribe("/user/queue/chat-room-updates", (message) => {
          onRoomUpdate?.(JSON.parse(message.body));
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [accessToken]);
}
