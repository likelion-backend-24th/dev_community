import { useCallback, useEffect, useState } from "react";

/**
 * 라이트/다크 테마 전환. index.html의 인라인 스크립트가 렌더 전에
 * <html data-theme>을 이미 정해두므로, 여기서는 그 값을 읽어 상태로
 * 동기화하고 토글 시 localStorage에 선택을 저장한다.
 */
export function useTheme() {
  const [theme, setTheme] = useState(
    () => document.documentElement.getAttribute("data-theme") || "light",
  );

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("theme", theme);
  }, [theme]);

  const toggleTheme = useCallback(() => {
    setTheme((prev) => (prev === "dark" ? "light" : "dark"));
  }, []);

  return { theme, toggleTheme };
}
