import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import QuestionCard from "./QuestionCard";

const question = {
  id: 7,
  title: "JPA N+1 문제 해결 방법",
  status: "OPEN",
  type: "CODE_REVIEW",
  tags: ["jpa", "spring"],
  authorNickname: "민규",
  authorAvatarColor: "#123456",
  authorIsExpert: true,
  viewCount: 12,
  likeCount: 3,
  answerCount: 5,
  createdAt: new Date().toISOString(),
};

const renderCard = (props) =>
  render(
    <MemoryRouter>
      <QuestionCard question={question} {...props} />
    </MemoryRouter>,
  );

describe("QuestionCard", () => {
  it("제목, 태그, 작성자, 통계를 렌더링한다", () => {
    renderCard();

    expect(screen.getByText("JPA N+1 문제 해결 방법")).toBeInTheDocument();
    expect(screen.getByText("jpa")).toBeInTheDocument();
    expect(screen.getByText("spring")).toBeInTheDocument();
    expect(screen.getByText("민규")).toBeInTheDocument();
    expect(screen.getByText("12")).toBeInTheDocument();
    expect(screen.getByText("3")).toBeInTheDocument();
    expect(screen.getByText("5")).toBeInTheDocument();
  });

  it("카드 전체가 상세 페이지로 가는 링크다", () => {
    renderCard();

    expect(screen.getByRole("link")).toHaveAttribute("href", "/questions/7");
  });

  it("showType이 없으면 글 유형 뱃지를 숨긴다", () => {
    renderCard();

    expect(screen.queryByText("코드리뷰")).not.toBeInTheDocument();
    expect(screen.getByText("미해결")).toBeInTheDocument();
  });

  it("showType이면 글 유형 뱃지를 함께 보여준다", () => {
    renderCard({ showType: true });

    expect(screen.getByText("코드리뷰")).toBeInTheDocument();
    expect(screen.getByText("미해결")).toBeInTheDocument();
  });
});
