function AlertModal({ message, onClose }) {
  return (
    <div className="modal-overlay">
      <div className="modal" role="alertdialog" aria-modal="true" aria-label="알림">
        <div className="modal__body">
          <p className="modal__desc">{message}</p>
        </div>
        <div className="modal__footer">
          <button type="button" className="btn btn-primary" onClick={onClose}>
            확인
          </button>
        </div>
      </div>
    </div>
  );
}

export default AlertModal;
