import { useEffect, useState } from "react";

const DEFAULT_ERROR_MESSAGE = "데이터를 불러오지 못했습니다.";

// 대시보드 패널들이 각자 반복하던 "마운트 시 조회 + 언마운트 시 취소 + 에러 메시지 추출"
// 패턴을 공통화한 훅. data가 null이면 아직 로딩 중이라는 뜻이다.
export function useAsyncData(
  fetcher,
  deps = [],
  errorMessage = DEFAULT_ERROR_MESSAGE,
) {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    fetcher()
      .catch(() => fetcher())
      .then((result) => {
        if (!cancelled) setData(result);
      })
      .catch((err) => {
        if (!cancelled) setError(err.response?.data?.message ?? errorMessage);
      });

    return () => {
      cancelled = true;
    };
  }, deps);

  return { data, error };
}
