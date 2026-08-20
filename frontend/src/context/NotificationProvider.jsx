import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import NotificationContext from "./NotificationContext";
import { useAuth } from "../hooks/useAuth";
import { resolveWsUrl } from "../utils/wsUrl";

export function NotificationProvider({ children }) {
  const { accessToken } = useAuth();
  const [notification, setNotification] = useState(null);
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
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [accessToken]);

  const dismiss = () => setNotification(null);

  return (
    <NotificationContext.Provider value={{ notification, dismiss }}>
      {children}
    </NotificationContext.Provider>
  );
}
