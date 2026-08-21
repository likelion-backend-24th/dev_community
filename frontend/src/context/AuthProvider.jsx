import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthContext from "./AuthContext";
import client, { refreshAccessToken } from "../api/client";
import {
  getAccessToken,
  setAccessToken as persistAccessToken,
  subscribeAccessToken,
} from "../api/tokenStore";
import { decodeToken } from "../utils/jwt";

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(() => getAccessToken());
  const navigate = useNavigate();

  // navigate가 라우팅할 때마다 새 참조로 바뀌어서(react-router 버전 이슈로 보임),
  // 이 값을 아래 interceptor 등록 effect의 의존성에 직접 넣으면 페이지 전환마다
  // interceptor가 eject -> 재등록을 반복하다가 실제 요청이 실패하는 타이밍에
  // 하필 등록이 빠져있는 상태가 되어버림(실제 배포 환경에서 확인됨: 미구독자가
  // 멤버십 게시판 접근 시 403이 와도 리다이렉트가 전혀 동작하지 않는 문제로 발현).
  // ref로 최신 navigate만 추적하고, interceptor는 마운트 시 한 번만 등록한다.
  const navigateRef = useRef(navigate);
  useEffect(() => {
    navigateRef.current = navigate;
  }, [navigate]);

  useEffect(() => subscribeAccessToken(setAccessToken), []);

  const login = useCallback((token) => {
    persistAccessToken(token);
  }, []);

  const logout = useCallback(() => {
    persistAccessToken(null);
  }, []);

  useEffect(() => {
    const redirectToLogin = () => {
      logout();
      navigateRef.current("/login", {
        state: { message: "로그인이 필요합니다. 다시 로그인해주세요." },
      });
    };

    const interceptorId = client.interceptors.response.use(
      (response) => response,
      async (error) => {
        const status = error.response?.status;
        const url = error.config?.url ?? "";
        const isAuthEndpoint =
          url.includes("/api/auth/login") ||
          url.includes("/api/auth/reissue") ||
          url.includes("/api/auth/oauth/");

        // 비밀번호 변경/회원 탈퇴는 "현재 비밀번호가 틀림" 때문에도 401(INVALID_CREDENTIALS)이 내려옴.
        // 이건 토큰 만료가 아니라 입력값 검증 실패라서 로그아웃시키면 안 되고,
        // 각 화면(PasswordModal, WithdrawModal)의 인라인 에러 처리에 맡겨야 함.
        const isPasswordCheckEndpoint =
          (url.includes("/api/members/me/password") &&
            error.config?.method === "put") ||
          (url.includes("/api/members/me") &&
            !url.includes("/password") &&
            error.config?.method === "delete");

        if (isAuthEndpoint || isPasswordCheckEndpoint) {
          return Promise.reject(error);
        }

        // 미구독자가 멤버십 게시판 접근 시 403 페이지 대신 멤버십 가입 페이지로 안내해 가입 유도.
        const isPremiumBoardEndpoint = url.includes("/api/questions/premium");

        if (status === 403 && isPremiumBoardEndpoint) {
          navigateRef.current("/membership", {
            state: { message: "멤버십 가입 후 이용할 수 있어요." },
          });
          return Promise.reject(error);
        }

        if (status === 403) {
          navigateRef.current("/403");
          return Promise.reject(error);
        }

        if (status !== 401) {
          return Promise.reject(error);
        }

        // accessToken 만료로 401이 온 첫 시도에만 재발급을 시도,
        // 재발급한 토큰으로도 401이면(리프레시까지 만료 등) 바로 재로그인 시킴.
        if (!error.config._retry) {
          error.config._retry = true;
          try {
            await refreshAccessToken();
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
  }, [logout]);

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
