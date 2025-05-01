import React, { useState, useEffect } from 'react';
import '../styles/DevicePopup.css';
import smartwatch from '../assets/images/smartwatch.png';
import checkIcon from '../assets/icons/check.svg';

const DevicePopup = ({ isOpen, onClose }) => {
    const [deviceFound, setDeviceFound] = useState(false);
    const [showDeviceDetails, setShowDeviceDetails] = useState(false);
    const [showNameInput, setShowNameInput] = useState(false);
    const [showSuccess, setShowSuccess] = useState(false);
    const [deviceName, setDeviceName] = useState('');
    const [error, setError] = useState('');
    const [suggestions, setSuggestions] = useState([]);
    const [showSuggestion, setShowSuggestion] = useState(false);
    const suggestionName = "Fransua's Smartwatch";

    useEffect(() => {
        if (!isOpen) {
            setDeviceFound(false);
            setShowDeviceDetails(false);
            setShowNameInput(false);
            setShowSuccess(false);
            setDeviceName('');
            setError('');
        }
    }, [isOpen]);

    useEffect(() => {
        if (isOpen && !deviceFound) {
            const timer = setTimeout(() => {
                setDeviceFound(true);
            }, 2000);
            return () => clearTimeout(timer);
        }
    }, [isOpen, deviceFound]);

    const handleAddDevice = () => {
        setShowDeviceDetails(true);
    };

    const handleAddSmartwatch = () => {
        setShowNameInput(true);
    };

    const handleNameChange = (e) => {
        const value = e.target.value;
        setDeviceName(value);
        setError('');
    };

    const handleInputFocus = () => {
        if (!deviceName) {
            setShowSuggestion(true);
        }
    };

    const handleInputBlur = (e) => {
        if (!e.relatedTarget?.classList.contains('name-suggestion')) {
            setShowSuggestion(false);
        }
    };

    const handleSuggestionClick = () => {
        setDeviceName(suggestionName);
        setShowSuggestion(false);
    };

    const handleContinue = () => {
        if (!deviceName.trim()) {
            setError('Please enter a name for your device');
            return;
        }
        setShowSuccess(true);
    };

    const clearInput = () => {
        setDeviceName('');
        setError('');
    };

    if (showSuccess) {
        return (
            <>
                <div className={`device-popup-overlay ${isOpen ? 'show' : ''}`} />
                    <div className={`device-popup ${isOpen ? 'show' : ''}`}>
                        <button className="close-button" onClick={onClose}>
                            <svg viewBox="0 0 24 24" fill="none" stroke="#666666">
                                <path d="M18 6L6 18M6 6l12 12" strokeWidth="1.5" strokeLinecap="round"/>
                            </svg>
                        </button>
                        
                        <div className="success-message">
                            <img src={checkIcon} alt="Success" className="success-icon" />
                            <h3 className="success-title">Device added successfully!</h3>
                            <p className="success-text">
                                <span className="device-success-name">"{deviceName || "Your smartwatch"}"</span>
                                has been connected and is ready to use.
                            </p>
                            <button className="continue-btn" onClick={onClose}>
                                Continue
                            </button>
                        </div>
                    </div>
            </>
        );
    }

    if (showNameInput) {
        return (
            <>
                <div className={`device-popup-overlay ${isOpen ? 'show' : ''}`} />
                <div className={`device-popup ${isOpen ? 'show' : ''}`}>
                    <button className="close-button" onClick={onClose}>
                        <svg viewBox="0 0 24 24" fill="none" stroke="#666666">
                            <path d="M18 6L6 18M6 6l12 12" strokeWidth="1.5" strokeLinecap="round"/>
                        </svg>
                    </button>
                    
                    <div className="device-popup-header">
                        <h3>SmartWatch Name</h3>
                    </div>

                    <div className="name-input-container">
                        <input
                            type="text"
                            className={`name-input ${error ? 'error' : ''}`}
                            placeholder="Write a name"
                            value={deviceName}
                            onChange={handleNameChange}
                            onFocus={handleInputFocus}
                            onBlur={handleInputBlur}
                        />
                        {deviceName && (
                            <button className="clear-input" onClick={clearInput}>
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#666666">
                                    <path d="M18 6L6 18M6 6l12 12" strokeWidth="1.5"/>
                                </svg>
                            </button>
                        )}
                        {showSuggestion && !deviceName && (
                            <div 
                                className="name-suggestion" 
                                onClick={handleSuggestionClick}
                                tabIndex="0"
                            >
                                {suggestionName}
                            </div>
                        )}
                        <div className="name-input-help">
                            Name your smartwatch!
                        </div>
                        <div className={`name-error ${error ? 'show' : ''}`}>
                            {error}
                        </div>
                    </div>

                    <div className="action-buttons">
                        <button 
                            className="continue-btn"
                            onClick={handleContinue}
                            disabled={!deviceName.trim()}
                        >
                            Continue
                        </button>
                    </div>
                </div>
            </>
        );
    }

    if (showDeviceDetails) {
        return (
            <>
                <div className={`device-popup-overlay ${isOpen ? 'show' : ''}`} />
                <div className={`device-popup ${isOpen ? 'show' : ''}`}>
                    <button className="close-button" onClick={onClose}>
                        <svg viewBox="0 0 24 24" fill="none" stroke="#666666">
                            <path d="M18 6L6 18M6 6l12 12" strokeWidth="1.5" strokeLinecap="round"/>
                        </svg>
                    </button>
                    
                    <div className="device-found-content">
                        <div className="device-image-container">
                            <img src={smartwatch} alt="Huawei Watch D2" className="device-image" />
                        </div>
                        <h3 className="device-name">Huawei Watch D2</h3>
                        <button 
                            className="add-smartwatch-btn"
                            onClick={handleAddSmartwatch}
                        >
                            Add smartwatch
                        </button>
                    </div>
                </div>
            </>
        );
    }

    return (
        <>
            <div 
                className={`device-popup-overlay ${isOpen ? 'show' : ''}`}
                onClick={onClose}
            />
            <div className={`device-popup ${isOpen ? 'show' : ''}`}>
                <div className="device-popup-header">
                    <h3>{deviceFound ? 'A device found' : 'Searching for devices...'}</h3>
                    <button className="close-button" onClick={onClose}>
                        <svg viewBox="0 0 24 24" fill="none" stroke="#666666">
                            <path d="M18 6L6 18M6 6l12 12" strokeWidth="1.5" strokeLinecap="round"/>
                        </svg>
                    </button>
                </div>

                <div className="radar-container">
                    <div className="radar-circle"></div>
                    <div className="radar-scan"></div>
                    <div className="radar-center"></div>
                    {deviceFound && <div className="device-dot"></div>}
                </div>

                <div className="action-buttons">
                    {deviceFound ? (
                        <button 
                            className="add-device-btn"
                            onClick={handleAddDevice}
                        >
                            Add device
                        </button>
                    ) : null}
                    <button 
                        className="cancel-btn"
                        onClick={onClose}
                    >
                        I don't have a device
                    </button>
                </div>
            </div>
        </>
    );
};

export default DevicePopup; 