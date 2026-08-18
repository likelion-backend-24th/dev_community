import { Link } from "react-router-dom";

function Footer() {
  return (
    <footer className="footer">
      <div className="footer__inner">
        <span className="footer__brand">Dev_Community</span>
        <nav className="footer__links">
          <Link to="/terms">이용약관</Link>
        </nav>
      </div>
    </footer>
  );
}

export default Footer;
