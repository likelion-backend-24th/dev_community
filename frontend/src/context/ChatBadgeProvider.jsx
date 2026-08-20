import { useCallback, useEffect, useState } from "react";
import ChatBadgeContext from "./ChatBadgeContext";
import { useAuth } from "../hooks/useAuth";
import { useChatSocket } from "../hooks/useChatSocket";
import { getUnreadChatRoomCount } from "../api/chatApi";

export function ChatBadgeProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);

  const refreshUnreadCount = useCallback(() => {
    if (!isAuthenticated) return;
    getUnreadChatRoomCount()
      .then(setUnreadCount)
      .catch(() => {});
  }, [isAuthenticated]);

  useEffect(() => {
    if (isAuthenticated) refreshUnreadCount();
  }, [isAuthenticated, refreshUnreadCount]);

  // 새 채팅 메시지가 오거나 채팅방 상태(수락/채택/종료)가 바뀔 때마다 뱃지 카운트를 다시 불러온다.
  useChatSocket({
    onMessage: refreshUnreadCount,
    onRoomUpdate: refreshUnreadCount,
  });

  return (
    <ChatBadgeContext.Provider value={{ unreadCount, refreshUnreadCount }}>
      {children}
    </ChatBadgeContext.Provider>
  );
}
