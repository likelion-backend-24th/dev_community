import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useNotification } from "../../hooks/useNotification";

const AUTO_DISMISS_MS = 6000;

function NotificationToast() {
  const { notification, toastVisible, hideToast, dismiss } = useNotification();
  const navigate = useNavigate();

  useEffect(() => {
    if (!toastVisible) return undefined;
    const timer = setTimeout(hideToast, AUTO_DISMISS_MS);
    return () => clearTimeout(timer);
  }, [toastVisible, hideToast]);

  if (!toastVisible || !notification) return null;

  const handleClick = () => {
    navigate(notification.link);
    dismiss();
  };

  return (
    <div className="notification-toast" onClick={handleClick} role="alert">
      {notification.message}
    </div>
  );
}

export default NotificationToast;
