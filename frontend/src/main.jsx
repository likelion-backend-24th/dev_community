import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { AuthProvider } from "./context/AuthProvider.jsx";
import { NotificationProvider } from "./context/NotificationProvider.jsx";
import { ChatBadgeProvider } from "./context/ChatBadgeProvider.jsx";
import "./index.css";
import "./styles/common.css";
import "./styles/layout.css";
import App from "./App.jsx";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <NotificationProvider>
          <ChatBadgeProvider>
            <App />
          </ChatBadgeProvider>
        </NotificationProvider>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
);
