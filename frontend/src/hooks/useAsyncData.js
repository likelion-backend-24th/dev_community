import { useEffect, useState } from 'react'

const DEFAULT_ERROR_MESSAGE = '데이터를 불러오지 못했습니다.'

// 대시보드 패널들이 각자 반복하던 "마운트 시 조회 + 언마운트 시 취소 + 에러 메시지 추출"
// 패턴을 공통화한 훅. data가 null이면 아직 로딩 중이라는 뜻이다.
export function useAsyncData(fetcher, deps = [], errorMessage = DEFAULT_ERROR_MESSAGE) {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false

    fetcher()
      .then((result) => {
        if (!cancelled) setData(result)
      })
      .catch((err) => {
        if (!cancelled) setError(err.response?.data?.message ?? errorMessage)
      })

    return () => {
      cancelled = true
    }
    // fetcher는 매 렌더마다 새로 만들어지는 인라인 함수라 의존성에 넣으면 무한 루프가 된다.
    // 재조회가 필요한 값만 호출부에서 deps로 넘긴다.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  return { data, error }
}
