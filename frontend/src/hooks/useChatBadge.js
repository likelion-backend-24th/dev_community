import { useContext } from "react";
import ChatBadgeContext from "../context/ChatBadgeContext";

export function useChatBadge() {
  const context = useContext(ChatBadgeContext);
  if (!context) {
    throw new Error("useChatBadge must be used within a ChatBadgeProvider");
  }
  return context;
}
