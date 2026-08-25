import { Link } from "react-router-dom";

function Footer() {
  return (
    <footer className="footer">
      <div className="footer__inner">
        <span className="footer__brand">
          <span className="footer__brand-arrow" aria-hidden="true">
            &gt;
          </span>
          dev_com_
        </span>
        <nav className="footer__links">
          <Link to="/terms">이용약관</Link>
          <Link to="/privacy">개인정보처리방침</Link>
        </nav>
      </div>
    </footer>
  );
}

export default Footer;
