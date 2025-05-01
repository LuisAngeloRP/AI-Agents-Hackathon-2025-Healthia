import React, { useState } from "react";
import "../styles/ProfileScreen.css";
import "../styles/SharedContainer.css";
import backIcon from "../assets/icons/back.svg";
import moreIcon from "../assets/icons/more.svg";
import avatarFransua from "../assets/images/avatar_fransua.jpeg";
import heartbeatIcon from "../assets/icons/Heartbeat.svg";
import fireIcon from "../assets/icons/Fire.svg";
import barbellIcon from "../assets/icons/Barbell.svg";
import cameraIcon from "../assets/icons/camera.svg";
import espiralIcon from "../assets/icons/espiral.png";
import nameIcon from "../assets/icons/name.svg";
import emailIcon from "../assets/icons/email.svg";
import phoneIcon from "../assets/images/phone.png";
import deviceIcon from "../assets/images/device.png";
import passwordIcon from "../assets/icons/password.svg";
import logoutIcon from "../assets/icons/logout.svg";
import arrowIcon from "../assets/icons/arrow.svg";
import FitnessDashboard from "./FitnessDashboard";
import ActivitiesScreen from "./ActivitiesScreen";
import { useNavigate } from 'react-router-dom';
import emergencyIcon from "../assets/icons/emergency.svg";

const ProfileScreen = ({ onNavigate, previousScreen }) => {
  const navigate = useNavigate();
  const [showPrevious, setPrevious] = useState(false);
  const [activeModal, setActiveModal] = useState(null);
  // Se actualiza userData para incluir un arreglo de Emergency Contacts, inicialmente con un ejemplo.
  const [userData, setUserData] = useState({
    name: "Fransua Leon",
    email: "fransua@healthia.com",
    phone: "+51 982 260 586",
    username: "fransua_leon",
    emergencyContacts: [
      { name: "Sergio Yupanqui", relationship: "Amigo", phone: "+51 953 636 855" }
    ]
  });
  // Para otros modales usamos tempData; en caso de emergency, tempData se espera sea un arreglo.
  const [tempData, setTempData] = useState({});
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [showConfirmLogout, setShowConfirmLogout] = useState(false);
  const [isDashboardVisible, setIsDashboardVisible] = useState(true);

  const userStats = [
    { icon: heartbeatIcon, value: "215bpm", label: "Heart rate" },
    { icon: fireIcon, value: "756cal", label: "Calories" },
    { icon: barbellIcon, value: "133lbs", label: "Weight" }
  ];

  // Al abrir un modal, si es emergency, tempData será el arreglo de contactos
  const handleModalOpen = (modalType, currentData) => {
    if (modalType === 'emergency') {
      setTempData(currentData || []); // tempData será un arreglo
    } else {
      setTempData(currentData);
    }
    setActiveModal(modalType);
  };

  const handleModalClose = () => {
    setActiveModal(null);
    setTempData({});
  };

  const handleSaveChanges = () => {
    if (activeModal === 'password') {
      if (passwordData.currentPassword !== "cevicheria") {
        alert("Current password is incorrect");
        return;
      }
      if (passwordData.newPassword !== passwordData.confirmPassword) {
        alert("New password and confirmation do not match");
        return;
      }
      handleModalClose();
      alert("Password successfully updated");
    } else if (activeModal === 'emergency') {
      // Actualizamos el arreglo de emergencyContacts
      setUserData({ ...userData, emergencyContacts: [...tempData] });
      handleModalClose();
    } else {
      setUserData({ ...userData, ...tempData });
      handleModalClose();
    }
  };

  const handleLogout = () => {
    setShowConfirmLogout(true);
  };

  const confirmLogout = () => {
    setShowConfirmLogout(false);
    setActiveModal(null);
    setTempData({});
    setPasswordData({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
    onNavigate('login');
  };

  const handlePasswordChange = (field, value) => {
    setPasswordData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  // Reordenamos el menú: Name, E-Mail, Phone no., Emergency Contact, Linked Devices, Change Password, Logout
  const menuItems = [
    {
      icon: nameIcon,
      label: "Name",
      value: userData.name,
      onClick: () => handleModalOpen('name', { name: userData.name }),
      bgColor: '#F5F5F5'
    },
    {
      icon: emailIcon,
      label: "E-Mail",
      value: userData.email,
      onClick: () => handleModalOpen('email', { email: userData.email }),
      bgColor: '#F5F5F5'
    },
    {
      icon: phoneIcon,
      label: "Phone no.",
      value: userData.phone,
      onClick: () => handleModalOpen('phone', { phone: userData.phone }),
      bgColor: '#F5F5F5'
    },
    {
      icon: emergencyIcon,
      label: "Emergency Contact",
      value: userData.emergencyContacts.length > 0
        ? `${userData.emergencyContacts[0].name} - ${userData.emergencyContacts[0].phone}`
        : "Not Set",
      onClick: () => handleModalOpen('emergency', userData.emergencyContacts),
      bgColor: '#FFEDED'
    },
    {
      icon: deviceIcon,
      label: "Linked Devices",
      value: "Huawei Watch D2 (1 Total)",
      onClick: () => handleModalOpen('devices'),
      bgColor: '#F5F5F5'
    },
    {
      icon: passwordIcon,
      label: "Change Password",
      className: "action-item",
      onClick: () => handleModalOpen('password'),
      bgColor: '#EBF2FF'
    },
    {
      icon: logoutIcon,
      label: "Logout",
      className: "logout-item",
      onClick: handleLogout,
      bgColor: '#FFF2F2'
    }
  ];

  const getModalTitle = (modalType) => {
    const titles = {
      name: "Edit Name",
      email: "Edit Email",
      phone: "Edit Phone Number",
      password: "Change Password",
      devices: "Linked Devices",
      emergency: "Emergency Contact"
    };
    return titles[modalType] || "Edit Profile";
  };

  // Función para agregar un nuevo contacto vacío en el modal de emergency
  const addNewEmergencyContact = () => {
    setTempData(prev => [, { name: "", relationship: "", phone: "" }]);
  };

  if (showPrevious) {
    if (previousScreen === "health") {
      return <FitnessDashboard onBack={() => setPrevious(false)} />;
    } else if (previousScreen === "activities") {
      return <ActivitiesScreen onBack={() => setPrevious(false)} />;
    }
  }

  return (
    <div className="condition-screen">
      <div className="profile-screen-wrapper">
        <div className="profile-screen-container">
          <div className="profile-screen-spiral">
            <img src={espiralIcon} alt="" aria-hidden="true" />
          </div>

          <div className="profile-screen-header">
            <button className="profile-screen-back-button" onClick={() => setPrevious(true)}>
              <img src={backIcon} alt="Back" />
            </button>
            <button className="profile-screen-more-button">
              <img src={moreIcon} alt="More" />
            </button>
          </div>

          <div className="profile-screen-info">
            <div className="profile-screen-photo">
              <img src={avatarFransua} alt="Profile" />
              <button className="profile-screen-camera-button">
                <img src={cameraIcon} alt="Change photo" />
              </button>
            </div>
            <h2 className="profile-screen-username">{userData.username}</h2>
          </div>

          <div className="profile-screen-stats">
            {userStats.map((stat, index) => (
              <React.Fragment key={index}>
                <div className="profile-screen-stat-box">
                  <img src={stat.icon} alt={stat.label} className="stat-icon" />
                  <span className="profile-screen-stat-value">{stat.value}</span>
                  <span className="profile-screen-stat-label">{stat.label}</span>
                </div>
                {index < userStats.length - 1 && <div className="stat-divider"></div>}
              </React.Fragment>
            ))}
          </div>

          <div className="profile-screen-menu">
            <div className="menu-items-container">
              {menuItems.map((item, index) => (
                <button
                  key={index}
                  className={`profile-screen-menu-item ${item.className || ''}`}
                  onClick={item.onClick}
                >
                  <div className="profile-screen-menu-item-left">
                    <div className="menu-item-icon" style={{ background: item.bgColor }}>
                      <img src={item.icon} alt="" />
                    </div>
                    <div className="menu-item-text">
                      <span className="profile-screen-menu-item-label">{item.label}</span>
                      {item.value && (
                        <span className="profile-screen-menu-item-value">{item.value}</span>
                      )}
                    </div>
                  </div>
                  <div className="profile-screen-menu-item-right">
                    <img src={arrowIcon} alt="" className="menu-item-arrow" />
                  </div>
                </button>
              ))}
            </div>
          </div>
        </div>
        
        {activeModal && (
          <div className="modal-overlay" onClick={handleModalClose}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
              <h3>{getModalTitle(activeModal)}</h3>
              
              {activeModal === 'password' ? (
                <>
                  <div className="password-field">
                    <input
                      type="password"
                      placeholder="Current Password"
                      className="modal-input"
                      value={passwordData.currentPassword}
                      onChange={e => handlePasswordChange('currentPassword', e.target.value)}
                    />
                  </div>
                  <div className="password-field">
                    <input
                      type="password"
                      placeholder="New Password"
                      className="modal-input"
                      value={passwordData.newPassword}
                      onChange={e => handlePasswordChange('newPassword', e.target.value)}
                    />
                  </div>
                  <div className="password-field">
                    <input
                      type="password"
                      placeholder="Confirm New Password"
                      className="modal-input"
                      value={passwordData.confirmPassword}
                      onChange={e => handlePasswordChange('confirmPassword', e.target.value)}
                    />
                  </div>
                </>
              ) : activeModal === 'devices' ? (
                <div className="devices-list">
                  <div className="device-item">
                    <span>Huawei Watch D2</span>
                    <button className="disconnect-btn" style={{ backgroundColor: 'white', color: '#0957DE', border: '2px dashed #0957DE', marginLeft: '20px' }}>
                      Disconnect
                    </button>
                  </div>
                  <button className="add-device-btn" style={{ backgroundColor: 'white', color: '#0957DE', border: '2px dashed #0957DE' }}>
                    + Add New Device
                  </button>
                </div>
              ) : activeModal === 'emergency' ? (
                <>
                  {tempData && Array.isArray(tempData) && tempData.map((contact, idx) => (
                    <div className="modal-input-group" key={idx} style={{ marginBottom: '1rem' }}>
                      <label>Contact {idx + 1} Name</label>
                      <input
                        type="text"
                        placeholder="Enter emergency contact name"
                        value={contact.name || ""}
                        onChange={e => {
                          const updated = [...tempData];
                          updated[idx].name = e.target.value;
                          setTempData(updated);
                        }}
                        className="modal-input"
                      />
                      <label>Relationship</label>
                      <input
                        type="text"
                        placeholder="Enter relationship"
                        value={contact.relationship || ""}
                        onChange={e => {
                          const updated = [...tempData];
                          updated[idx].relationship = e.target.value;
                          setTempData(updated);
                        }}
                        className="modal-input"
                      />
                      <label>Phone Number</label>
                      <input
                        type="text"
                        placeholder="Enter phone number"
                        value={contact.phone || ""}
                        onChange={e => {
                          const updated = [...tempData];
                          updated[idx].phone = e.target.value;
                          setTempData(updated);
                        }}
                        className="modal-input"
                      />
                    </div>
                  ))}
                  <button 
                    className="add-emergency-contact-btn" 
                    onClick={addNewEmergencyContact}
                    style={{
                      backgroundColor: "#FEC635",
                      color: "#fff",
                      border: "none",
                      borderRadius: "19px",
                      padding: "6px 12px",
                      fontSize: "14px",
                      cursor: "pointer",
                      marginBottom: "1rem"
                    }}
                  >
                    + Add Emergency Contact
                  </button>
                </>
              ) : (
                <input
                  type="text"
                  value={tempData[activeModal] || ""}
                  onChange={e => setTempData({ ...tempData, [activeModal]: e.target.value })}
                  className="modal-input"
                  placeholder={`Enter new ${activeModal}`}
                />
              )}
              
              <div className="popup-buttons">
                <button className="modal-cancel" onClick={handleModalClose}>
                  Cancel
                </button>
                <button className="modal-save" onClick={handleSaveChanges} >
                  Save Changes
                </button>
              </div>
            </div>
          </div>
        )}

        {showConfirmLogout && (
          <div className="modal-overlay" onClick={() => setShowConfirmLogout(false)}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
              <h3>Confirm Logout</h3>
              <p>Are you sure you want to log out?</p>
              <div className="modal-actions">
                <button className="modal-cancel" onClick={() => setShowConfirmLogout(false)}>
                  Cancel
                </button>
                <button className="modal-logout" onClick={() => navigate('/login')} style={{ backgroundColor: 'rgba(255, 58, 48, 0.9)', color: '#FFFFFF' }}>
                  Logout
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProfileScreen;
