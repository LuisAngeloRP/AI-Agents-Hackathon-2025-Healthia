import React from 'react';
import ReactDOM from 'react-dom';
import '../../../styles/Nutrition/LocationModal.css';

const ModalRestaurant = ({ children, onClose }) => {
  return ReactDOM.createPortal(
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        {children}
      </div>
    </div>,
    document.body
  );
};

export default ModalRestaurant;
