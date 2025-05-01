import React, { useState, useRef } from 'react';
import "../styles/HealthConditionsScreen.css";
import "../styles/SharedContainer.css";
import logoDashboard from "../assets/logos/logo_sign.png";
import galleryIcon from '../assets/icons/gallery.svg';
import configIcon from '../assets/icons/config.svg';
import medicalIcon from '../assets/images/medical.png';
import ModalScreen from '../components/Nutrition/Modal/ModalScreen';

const HealthConditionsScreen = ({ onBack }) => {
    // Estados para las condiciones de salud
    const [conditions, setConditions] = useState([
        {
            id: 1,
            type: 'Allergy',
            description: 'Mani, blueberries',
            temporary: 'No',
            reminders: 'No'
        },
        {
            id: 2,
            type: 'Medical',
            description: 'Hypertension',
            temporary: 'No',
            reminders: 'Yes'
        },
        {
            id: 3,
            type: 'Condition',
            description: 'Neck pain',
            temporary: 'Yes',
            reminders: 'Yes'
        }
    ]);

    // Estados para las notificaciones
    const [notifications, setNotifications] = useState([
        'Medication Reminder: Take blood pressure medicine at 8:00 AM.',
        'Hydration Reminder: Drink a glass of water every 2 hours.'
    ]);

    // Estados para los documentos
    const [documents, setDocuments] = useState([
        { 
            id: 1,
            name: 'Scan 01:11:2020 03:57:06', 
            date: 'Today', 
            pages: '1 page' 
        },
        { 
            id: 2,
            name: 'Scan 20:02:2021 01:36:43', 
            date: 'Today', 
            pages: '1 page' 
        },
        { 
            id: 3,
            name: 'Prescription', 
            date: 'Yesterday', 
            pages: '1 page' 
        }
    ]);

    // Estados para los pop-ups
    const [showAddCondition, setShowAddCondition] = useState(false);
    const [showAddNotification, setShowAddNotification] = useState(false);
    const [newCondition, setNewCondition] = useState({
        type: '',
        description: '',
        temporary: 'No',
        reminders: 'No'
    });
    const [newNotification, setNewNotification] = useState('');

    // Estados para la cámara
    const [showCamera, setShowCamera] = useState(false);
    const [showSettings, setShowSettings] = useState(false);
    const videoRef = useRef(null);
    const streamRef = useRef(null);

    // Estado para las configuraciones (sin darkMode)
    const [settings, setSettings] = useState({
        notifications: true,
        autoScan: false
    });

    // Manejadores para condiciones
    const handleAddCondition = (newCondition) => {
        setConditions(prev => [...prev, {
            id: Date.now(),
            ...newCondition,
            lastUpdated: new Date().toISOString().split('T')[0]
        }]);
    };

    const handleUpdateCondition = (id, updates) => {
        setConditions(prev => prev.map(condition => 
            condition.id === id ? { ...condition, ...updates } : condition
        ));
    };

    const handleDeleteCondition = (id) => {
        setConditions(prev => prev.filter(condition => condition.id !== id));
    };

    // Manejador para notificaciones
    const handleAddNotification = () => {
        if (newNotification.trim()) {
            setNotifications(prev => [...prev, newNotification]);
            setNewNotification('');
            setShowAddNotification(false);
        }
    };

    // Manejador para documentos
    const handleAddDocument = () => {
        const now = new Date();
        const formattedDate = now.toLocaleString('en-US', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        }).replace(/[/,]/g, ':');

        const newDocument = {
            id: Date.now(),
            name: `Scan ${formattedDate}`,
            date: 'Just now',
            pages: '1 page'
        };

        setDocuments(prev => [newDocument, ...prev]); // Agregar al inicio de la lista
    };

    // Función para registrar nueva condición
    const handleRegisterCondition = () => {
        if (newCondition.type && newCondition.description) {
            const condition = {
                id: Date.now(),
                ...newCondition,
                lastUpdated: new Date().toLocaleDateString()
            };
            setConditions(prev => [...prev, condition]);
            setNewCondition({ type: '', description: '', temporary: 'No', reminders: 'No' });
            setShowAddCondition(false);
        }
    };

    // Función para abrir la cámara
    const openCamera = async () => {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ 
                video: { facingMode: 'environment' } 
            });
            videoRef.current.srcObject = stream;
            streamRef.current = stream;
            setShowCamera(true);
        } catch (error) {
            console.error('Error accessing camera:', error);
            alert('Could not access camera. Please check permissions.');
        }
    };

    // Función para capturar imagen y crear documento
    const captureImage = () => {
        const now = new Date();
        const formattedDate = now.toLocaleString('en-US', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        }).replace(/[/,]/g, ':');

        const newDocument = {
            id: Date.now(),
            name: `Scan ${formattedDate}`,
            date: 'Just now',
            pages: '1 page'
        };

        setDocuments(prev => [newDocument, ...prev]);
        closeCamera();
    };

    // Función para cerrar la cámara
    const closeCamera = () => {
        if (streamRef.current) {
            streamRef.current.getTracks().forEach(track => track.stop());
        }
        setShowCamera(false);
    };

    // Función para abrir galería
    const handleGalleryClick = () => {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = '.pdf,image/*';
        input.multiple = false;
        
        input.onchange = (e) => {
            const file = e.target.files[0];
            if (file) {
                const now = new Date();
                const formattedDate = now.toLocaleString('en-US', {
                    day: '2-digit',
                    month: '2-digit',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                    second: '2-digit',
                    hour12: false
                }).replace(/[/,]/g, ':');

                const newDocument = {
                    id: Date.now(),
                    name: file.name,
                    date: 'Just now',
                    pages: '1 page',
                    type: file.type
                };

                setDocuments(prev => [newDocument, ...prev]);
            }
        };

        input.click();
    };

    // Función para manejar cambios en configuraciones
    const handleSettingChange = (setting) => {
        setSettings(prev => {
            const newSettings = { ...prev, [setting]: !prev[setting] };
            
            // Efectos específicos para cada configuración
            switch(setting) {
                case 'notifications':
                    if (newSettings.notifications) {
                        Notification.requestPermission();
                    }
                    break;
                case 'autoScan':
                    // Lógica para auto-scan
                    break;
                default:
                    break;
            }
            
            return newSettings;
        });
    };

    // Función para eliminar documento
    const handleDeleteDocument = (id) => {
        setDocuments(prev => prev.filter(doc => doc.id !== id));
    };

    // Función para eliminar notificación
    const handleDeleteNotification = (index) => {
        setNotifications(prev => prev.filter((_, i) => i !== index));
    };

    return (
        <div className="condition-screen">
            <div className="conditions-header">
              <div className="back-wrapper">
                <button className="conditions-back-button" onClick={onBack}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="17" height="15" viewBox="0 0 17 15" fill="none">
                        <path d="M7.45408 13.6896L1.0805 7.47707L7.29305 1.10349M15.4646 7.29304L1.0805 7.47707L15.4646 7.29304Z" 
                        stroke="black" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                </button>

                    <div className="header-content">
                        <img src={logoDashboard} alt="HealthIA" className="header-logo" />
                        <div className="header-text">
                            <h1>HealthIA</h1>
                            <p>Your personal health assistant</p>
                        </div>
                    </div>
                    
                </div>
            </div>

            <div className="health-content-scrollable">
                <div >
                    <div className="section-container">
                        <h2 className="section-title-health">Register Health Conditions</h2>
                        <div className="register-condition" onClick={() => setShowAddCondition(true)}>
                            <div className="condition-input">
                                <span className="input-icon">+</span>
                                <span>Register new condition</span>
                            </div>
                        </div>
                    </div>

                    <div className="section-container">
                        <h3 className="section-subtitle">Registered Conditions:</h3>
                        <div className="conditions-list">
                            {conditions.map(condition => (
                                <div key={condition.id} className="condition-card">
                                    <div className="condition-header">
                                        <div className="condition-type">{condition.type}</div>
                                        <button className="delete-button" onClick={() => handleDeleteCondition(condition.id)}>×</button>
                                    </div>
                                    <div className="condition-row">
                                        <span className="condition-label">Description:</span>
                                        <span className="condition-value">{condition.description}</span>
                                    </div>
                                    <div className="condition-row">
                                        <span className="condition-label">Temporary?:</span>
                                        <span className="condition-value">{condition.temporary}</span>
                                    </div>
                                    <div className="condition-row">
                                        <span className="condition-label">Reminders?:</span>
                                        <span className="condition-value">{condition.reminders}</span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="section-container">
                        <div className="section-header">
                            <h3 className="section-subtitle"><span className="notification-icon">🔔</span> Active Notifications</h3>
                            <button className="add-button" onClick={() => setShowAddNotification(true)}>+</button>
                        </div>
                        <div className="notifications-list">
                            {notifications.map((notification, index) => (
                                <div key={index} className="notification-item">
                                    <span className="check-icon">✓</span>
                                    {notification}
                                    <button className="delete-button" onClick={() => handleDeleteNotification(index)}>×</button>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="section-container">
                        <h3 className="section-subtitle">Upload medical document with OCR</h3>
                        <div className="documents-list">
                            {documents.map((doc) => (
                                <div key={doc.id} className="document-item">
                                    <div className="document-preview"><img src={medicalIcon} alt="Medical document" /></div>
                                    <div className="document-info">
                                        <h4>{doc.name}</h4>
                                        <div className="document-meta"><span>{doc.date}</span><span>{doc.pages}</span></div>
                                    </div>
                                    <button className="delete-button" onClick={() => handleDeleteDocument(doc.id)}>×</button>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>

            <div className="health-chat-input-container">
                <div className="nav-icon-container" onClick={handleGalleryClick}><img src={galleryIcon} alt="Gallery" /></div>
                <button className="scan-button" onClick={openCamera}>SCAN</button>
                <div className="nav-icon-container" onClick={() => setShowSettings(true)}><img src={configIcon} alt="Settings" /></div>
            </div>

            {/* Modal de cámara */}
            {showCamera && (
                <ModalScreen onClose={() => setShowCamera(false)}>
                        <video 
                            ref={videoRef} 
                            autoPlay 
                            playsInline
                        />
                        <div className="camera-controls">
                            <button onClick={closeCamera} className="camera-button cancel">
                                Cancel
                            </button>
                            <button onClick={captureImage} className="camera-button capture">
                                Capture
                            </button>
                        </div>
                </ModalScreen>
            )}

            {/* Modal de configuración actualizado */}
            {showSettings && (
                <ModalScreen onClose={() => setShowSettings(false)}>
                        <h3>Settings</h3>
                        <div className="settings-list">
                            <div className="settings-item">
                                <span>Notifications</span>
                                <label className="switch">
                                    <input 
                                        type="checkbox" 
                                        checked={settings.notifications}
                                        onChange={() => handleSettingChange('notifications')}
                                    />
                                    <span className="slider"></span>
                                </label>
                            </div>
                            <div className="settings-item">
                                <span>Auto Scan</span>
                                <label className="switch">
                                    <input 
                                        type="checkbox"
                                        checked={settings.autoScan}
                                        onChange={() => handleSettingChange('autoScan')}
                                    />
                                    <span className="slider"></span>
                                </label>
                            </div>
                        </div>
                        <div className="popup-buttons">
                            <button onClick={() => setShowSettings(false)}>Close</button>
                        </div>
                    </ModalScreen>
            )}

            {/* Pop-up para agregar condición */}
            {showAddCondition && (
                <ModalScreen onClose={() => setShowAddCondition(false)}>
                        <h3>Register New Condition</h3>
                        <div className="condition-form">
                            <input
                                type="text"
                                placeholder="Type of condition"
                                value={newCondition.type}
                                onChange={e => setNewCondition({...newCondition, type: e.target.value})}
                                className="modal-input"
                            />
                            <input
                                type="text"
                                placeholder="Description"
                                value={newCondition.description}
                                onChange={e => setNewCondition({...newCondition, description: e.target.value})}
                                className="modal-input"
                            />
                            <div className="form-row">
                                <label>Temporary?</label>
                                <select 
                                    value={newCondition.temporary}
                                    onChange={e => setNewCondition({...newCondition, temporary: e.target.value})}
                                >
                                    <option value="Yes">Yes</option>
                                    <option value="No">No</option>
                                </select>
                            </div>
                            <div className="form-row">
                                <label>Reminders?</label>
                                <select 
                                    value={newCondition.reminders}
                                    onChange={e => setNewCondition({...newCondition, reminders: e.target.value})}
                                >
                                    <option value="Yes">Yes</option>
                                    <option value="No">No</option>
                                </select>
                            </div>
                        </div>
                        <div className="popup-buttons">
                            <button onClick={() => setShowAddCondition(false)}>Cancel</button>
                            <button onClick={handleRegisterCondition}>Save</button>
                        </div>
                    </ModalScreen>
            )}

            {/* Pop-up para agregar notificación */}
            {showAddNotification && (
                <ModalScreen onClose={() => setShowAddNotification(false)}>
                        <h3>Add New Notification</h3>
                        <input
                            type="text"
                            placeholder="Notification text"
                            value={newNotification}
                            onChange={(e) => setNewNotification(e.target.value)}
                            className="modal-input"
                        />
                        <div className="popup-buttons">
                            <button onClick={handleAddNotification}>Add</button>
                            <button onClick={() => setShowAddNotification(false)}>Cancel</button>
                        </div>
                </ModalScreen>
            )}
        </div>
    );
};

export default HealthConditionsScreen; 