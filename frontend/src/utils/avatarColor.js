// 닉네임 첫 글자 아바타의 배경색. 매번 다시 뽑는 진짜 난수가 아니라,
// 닉네임 문자열을 해시해서 고정 팔레트의 인덱스로 매핑한다 — 같은 닉네임은
// 항상 같은 색이 나오고, 닉네임이 다르면(대체로) 다른 색이 나온다.
// 전부 흰 글자 위에서 명암비 4.5:1 이상을 만족하는 색만 골랐다.
const AVATAR_COLORS = [
  "#dc2626", // red
  "#c2410c", // orange
  "#15803d", // green
  "#0f766e", // teal
  "#2563eb", // blue
  "#9333ea", // purple
  "#db2777", // pink
  "#4f46e5", // indigo
];

export function getAvatarColor(seed) {
  const str = seed || "?";
  let hash = 0;
  for (let i = 0; i < str.length; i += 1) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0; // 32비트 정수로 유지
  }
  const index = Math.abs(hash) % AVATAR_COLORS.length;
  return AVATAR_COLORS[index];
}
