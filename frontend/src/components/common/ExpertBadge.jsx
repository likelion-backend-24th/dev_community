// 전문가 표시 배지: 무지개색 원 안에 흰색 별
function ExpertBadge({ className = "" }) {
  return (
    <svg
      className={`expert-badge${className ? ` ${className}` : ""}`}
      viewBox="0 0 20 20"
      width="18"
      height="18"
      role="img"
      aria-label="전문가"
    >
      <defs>
        <linearGradient id="expert-badge-rainbow" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#ff5252" />
          <stop offset="20%" stopColor="#ffb347" />
          <stop offset="40%" stopColor="#ffe45e" />
          <stop offset="60%" stopColor="#57f287" />
          <stop offset="80%" stopColor="#5865f2" />
          <stop offset="100%" stopColor="#c77dff" />
        </linearGradient>
      </defs>
      <circle cx="10" cy="10" r="10" fill="url(#expert-badge-rainbow)" />
      {/* 통통한 5각별 + 어두운 테두리로 밝은 그라디언트 구간에서도 대비 확보 */}
      <path
        d="M10,3.7 L11.59,7.82 L15.99,8.05 L12.57,10.83 L13.7,15.1 L10,12.7 L6.3,15.1 L7.43,10.83 L4.01,8.05 L8.41,7.82 Z"
        fill="#ffffff"
        stroke="rgba(0,0,0,0.45)"
        strokeWidth="0.6"
        strokeLinejoin="round"
      />
    </svg>
  );
}

export default ExpertBadge;
