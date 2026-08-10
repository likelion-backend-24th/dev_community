import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthContext from "./AuthContext";
import client from "../api/client";
import { reissue } from "../api/authApi";
import { decodeToken } from "../utils/jwt";

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(() =>
    localStorage.getItem("accessToken"),
  );
  const navigate = useNavigate();

  const login = useCallback((token) => {
    localStorage.setItem("accessToken", token);
    setAccessToken(token);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("accessToken");
    setAccessToken(null);
  }, []);

  useEffect(() => {
    let refreshPromise = null;

    const redirectToLogin = () => {
      logout();
      navigate("/401", {
        state: { message: "로그인이 필요합니다. 다시 로그인해주세요." },
      });
    };

    const interceptorId = client.interceptors.response.use(
      (response) => response,
      async (error) => {
        const status = error.response?.status;
        const url = error.config?.url ?? "";
        const isAuthEndpoint =
          url.includes("/api/auth/login") || url.includes("/api/auth/reissue");

        if (status === 403) {
          navigate("/403");
          return Promise.reject(error);
        }

        if (status !== 401 || isAuthEndpoint) {
          return Promise.reject(error);
        }

        // accessToken 만료로 401이 온 첫 시도에만 재발급을 시도,
        // 재발급한 토큰으로도 401이면(리프레시까지 만료 등) 바로 재로그인 시킴.
        if (!error.config._retry) {
          error.config._retry = true;
          try {
            if (!refreshPromise) {
              refreshPromise = reissue().finally(() => {
                refreshPromise = null;
              });
            }
            const { accessToken: newAccessToken } = await refreshPromise;
            login(newAccessToken);
            return client(error.config);
          } catch {
            // 재발급 실패 -> 아래 로그아웃 처리로 진행
          }
        }

        redirectToLogin();
        return Promise.reject(error);
      },
    );

    return () => client.interceptors.response.eject(interceptorId);
  }, [login, logout, navigate]);

  const user = useMemo(() => {
    const claims = decodeToken(accessToken);
    if (!claims) return null;
    return {
      id: Number(claims.sub),
      username: claims.username,
      nickname: claims.nickname,
      roles: claims.roles ?? [],
    };
  }, [accessToken]);

  const value = {
    accessToken,
    isAuthenticated: Boolean(accessToken),
    user,
    isAdmin: Boolean(user?.roles.includes("ADMIN")),
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
