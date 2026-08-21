import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import NotificationContext from "./NotificationContext";
import { useAuth } from "../hooks/useAuth";
import { resolveWsUrl } from "../utils/wsUrl";

export function NotificationProvider({ children }) {
  const { accessToken } = useAuth();
  const [notification, setNotification] = useState(null);
  const [toastVisible, setToastVisible] = useState(false);
  const clientRef = useRef(null);

  useEffect(() => {
    if (!accessToken) {
      clientRef.current?.deactivate();
      clientRef.current = null;
      return;
    }

    const client = new Client({
      brokerURL: resolveWsUrl(),
      connectHeaders: { Authorization: `Bearer ${accessToken}` },
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe("/user/queue/notifications", (message) => {
          setNotification(JSON.parse(message.body));
          setToastVisible(true);
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [accessToken]);

  const hideToast = () => setToastVisible(false);
  const dismiss = () => {
    setToastVisible(false);
    setNotification(null);
  };

  return (
    <NotificationContext.Provider
      value={{ notification, toastVisible, hideToast, dismiss }}
    >
      {children}
    </NotificationContext.Provider>
  );
}
