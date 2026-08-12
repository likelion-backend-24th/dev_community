import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  getDailyTrend,
  getResolutionRate,
  getStaleQuestions,
  getTopQuestions,
} from "../../api/adminApi";
import {
  ResponsiveContainer,
  LineChart,
  Line,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
} from "recharts";
import "../../styles/admin.css";
import "../../styles/dashboard.css";

// index.css의 디자인 토큰과 맞춘 차트 색상 (SVG 속성엔 var() 대신 고정값 사용)
const COLOR_BLURPLE = "#5865f2";
const COLOR_GREEN = "#23a55a";
const COLOR_GRID = "rgba(255, 255, 255, 0.06)";
const COLOR_MUTED = "#949ba4";

const TABS = [
  { key: "trend", label: "가입/질문 추이" },
  { key: "resolution", label: "질문 해결률" },
  { key: "stale", label: "미답변 질문" },
  { key: "top", label: "인기 질문 Top 5" },
];

function useCountUp(target, durationMs = 600) {
  const [value, setValue] = useState(0);

  useEffect(() => {
    let frameId;
    const start = performance.now();

    const tick = (now) => {
      const progress = Math.min(1, (now - start) / durationMs);
      const eased = 1 - Math.pow(1 - progress, 3); // ease-out cubic
      setValue(Math.round(target * eased));
      if (progress < 1) frameId = requestAnimationFrame(tick);
    };

    frameId = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(frameId);
  }, [target, durationMs]);

  return value;
}

function DailyTrendPanel() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    getDailyTrend()
      .then((res) => !cancelled && setData(res))
      .catch(
        (err) =>
          !cancelled &&
          setError(err.response?.data?.message ?? "데이터를 불러오지 못했습니다."),
      );
    return () => {
      cancelled = true;
    };
  }, []);

  if (error) return <p className="inline-error" role="alert">{error}</p>;
  if (!data) return <p className="state-text">불러오는 중...</p>;

  return (
    <div className="stat-card">
      <h2 className="stat-card__title">최근 30일 가입자 / 질문 추이</h2>
      <ResponsiveContainer width="100%" height={340}>
        <LineChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
          <CartesianGrid stroke={COLOR_GRID} vertical={false} />
          <XAxis
            dataKey="date"
            tick={{ fill: COLOR_MUTED, fontSize: 12 }}
            tickFormatter={(d) => d.slice(5)}
          />
          <YAxis allowDecimals={false} tick={{ fill: COLOR_MUTED, fontSize: 12 }} />
          <Tooltip
            contentStyle={{ background: "#111214", border: "none", borderRadius: 8 }}
            labelStyle={{ color: "#f2f3f5" }}
          />
          <Legend wrapperStyle={{ fontSize: 12 }} />
          <Line
            type="monotone"
            dataKey="signupCount"
            name="신규 가입자"
            stroke={COLOR_GREEN}
            strokeWidth={2}
            dot={false}
            animationDuration={800}
            animationEasing="ease-out"
          />
          <Line
            type="monotone"
            dataKey="questionCount"
            name="신규 질문"
            stroke={COLOR_BLURPLE}
            strokeWidth={2}
            dot={false}
            animationDuration={800}
            animationEasing="ease-out"
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

function ResolutionRatePanel() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    getResolutionRate()
      .then((res) => !cancelled && setData(res))
      .catch(
        (err) =>
          !cancelled &&
          setError(err.response?.data?.message ?? "데이터를 불러오지 못했습니다."),
      );
    return () => {
      cancelled = true;
    };
  }, []);

  if (error) return <p className="inline-error" role="alert">{error}</p>;
  if (!data) return <p className="state-text">불러오는 중...</p>;

  const pieData = [
    { name: "해결됨", value: data.resolvedQuestions },
    { name: "미해결", value: data.totalQuestions - data.resolvedQuestions },
  ];

  return (
    <div className="stat-card">
      <h2 className="stat-card__title">질문 해결률</h2>
      <div className="resolution-rate">
        <ResponsiveContainer width={260} height={260}>
          <PieChart>
            <Pie
              data={pieData}
              dataKey="value"
              innerRadius={80}
              outerRadius={110}
              startAngle={90}
              endAngle={-270}
              animationDuration={800}
              animationEasing="ease-out"
            >
              <Cell fill={COLOR_GREEN} />
              <Cell fill={COLOR_GRID} />
            </Pie>
            <Tooltip contentStyle={{ background: "#111214", border: "none", borderRadius: 8 }} />
          </PieChart>
        </ResponsiveContainer>
        <div className="resolution-rate__center">
          <span className="resolution-rate__percent">{data.resolutionRate}%</span>
          <span className="resolution-rate__desc">
            {data.resolvedQuestions} / {data.totalQuestions}건 해결
          </span>
        </div>
      </div>
    </div>
  );
}

function StaleQuestionsPanel() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const animatedCount = useCountUp(data?.count ?? 0);

  useEffect(() => {
    let cancelled = false;
    getStaleQuestions()
      .then((res) => !cancelled && setData(res))
      .catch(
        (err) =>
          !cancelled &&
          setError(err.response?.data?.message ?? "데이터를 불러오지 못했습니다."),
      );
    return () => {
      cancelled = true;
    };
  }, []);

  if (error) return <p className="inline-error" role="alert">{error}</p>;
  if (!data) return <p className="state-text state-text--plain">불러오는 중...</p>;

  return (
    <div className="stat-card">
      <h2 className="stat-card__title">7일 이상 답변 없는 질문</h2>
      <p className="stale-count">{animatedCount}건</p>

      {data.questions.length === 0 ? (
        <p className="state-text state-text--plain">방치된 질문이 없어요.</p>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>제목</th>
                <th>작성일</th>
              </tr>
            </thead>
            <tbody>
              {data.questions.map((q) => (
                <tr key={q.id}>
                  <td>{q.title}</td>
                  <td>{new Date(q.createdAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function TopQuestionsPanel() {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    getTopQuestions()
      .then((res) => !cancelled && setData(res))
      .catch(
        (err) =>
          !cancelled &&
          setError(err.response?.data?.message ?? "데이터를 불러오지 못했습니다."),
      );
    return () => {
      cancelled = true;
    };
  }, []);

  if (error) return <p className="inline-error" role="alert">{error}</p>;
  if (!data) return <p className="state-text">불러오는 중...</p>;

  const chartData = data.map((q) => ({
    title: q.title.length > 16 ? `${q.title.slice(0, 16)}...` : q.title,
    조회수: q.viewCount,
    추천수: q.likeCount,
  }));

  const renderTitleTick = ({ x, y, payload, index }) => {
    const questionId = data[index]?.id;
    return (
      <text
        x={x}
        y={y}
        dy={4}
        textAnchor="end"
        fill={COLOR_MUTED}
        fontSize={12}
        className="top-question-tick"
        onClick={() => questionId && navigate(`/questions/${questionId}`)}
      >
        {payload.value}
      </text>
    );
  };

  return (
    <div className="stat-card">
      <h2 className="stat-card__title">조회수 / 추천수 상위 질문 Top 5</h2>
      <ResponsiveContainer width="100%" height={320}>
        <BarChart data={chartData} layout="vertical" margin={{ top: 8, right: 24, left: 8, bottom: 0 }}>
          <CartesianGrid stroke={COLOR_GRID} horizontal={false} />
          <XAxis type="number" allowDecimals={false} tick={{ fill: COLOR_MUTED, fontSize: 12 }} />
          <YAxis type="category" dataKey="title" width={140} tick={renderTitleTick} />
          <Tooltip cursor={false} contentStyle={{ background: "#111214", border: "none", borderRadius: 8 }} />
          <Legend wrapperStyle={{ fontSize: 12 }} />
          <Bar dataKey="조회수" fill={COLOR_BLURPLE} animationDuration={800} animationEasing="ease-out" style={{ cursor: "default" }} />
          <Bar dataKey="추천수" fill={COLOR_GREEN} animationDuration={800} animationEasing="ease-out" style={{ cursor: "default" }} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

function AdminDashboardPage() {
  const [activeTab, setActiveTab] = useState(TABS[0].key);

  return (
    <div className="page">
      <div className="page__header">
        <h1 className="page__title">관리자 대시보드</h1>
      </div>

      <nav className="tabs">
        {TABS.map((tab) => (
          <button
            key={tab.key}
            type="button"
            className="tab"
            onClick={() => setActiveTab(tab.key)}
            disabled={activeTab === tab.key}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      {activeTab === "trend" && <DailyTrendPanel />}
      {activeTab === "resolution" && <ResolutionRatePanel />}
      {activeTab === "stale" && <StaleQuestionsPanel />}
      {activeTab === "top" && <TopQuestionsPanel />}
    </div>
  );
}

export default AdminDashboardPage;
