import React, { useState, useRef, useEffect } from 'react';
import "../styles/CameraScreen.css";
import "../styles/SharedContainer.css";
import mascotImage from "../assets/logos/mascot.png";
import logoDashboard from "../assets/logos/logo.png";

const CameraScreen = ({ onBack, onPhotoTaken }) => {
    const [photoSource, setPhotoSource] = useState(null);
    const [cameraActive, setCameraActive] = useState(false);
    const [showOptions, setShowOptions] = useState(true);
    const videoRef = useRef(null);
    const mediaStreamRef = useRef(null);

    // Activar la cámara cuando el usuario lo solicite
    const startCamera = async () => {
        try {
            setCameraActive(true);
            setShowOptions(false);
            
            const constraints = {
                video: {
                    facingMode: 'environment', // Usar cámara trasera
                    width: { ideal: 1920 },
                    height: { ideal: 1080 }
                }
            };
            
            const stream = await navigator.mediaDevices.getUserMedia(constraints);
            
            if (videoRef.current) {
                videoRef.current.srcObject = stream;
                mediaStreamRef.current = stream;
            }
        } catch (error) {
            console.error("Error al acceder a la cámara:", error);
            setCameraActive(false);
            setShowOptions(true);
            alert("No se pudo acceder a la cámara. Por favor, revisa los permisos.");
        }
    };

    // Tomar foto de la cámara
    const takePhoto = () => {
        if (videoRef.current && mediaStreamRef.current) {
            // Crear un canvas cuadrado para la captura
            const canvas = document.createElement('canvas');
            
            // Obtener las dimensiones del video
            const videoWidth = videoRef.current.videoWidth;
            const videoHeight = videoRef.current.videoHeight;
            
            // Usar el lado más pequeño para hacer un cuadrado
            const size = Math.min(videoWidth, videoHeight);
            canvas.width = size;
            canvas.height = size;
            
            // Calcular posición para centrar la captura
            const offsetX = (videoWidth - size) / 2;
            const offsetY = (videoHeight - size) / 2;
            
            // Dibujar la sección cuadrada del video en el canvas
            const ctx = canvas.getContext('2d');
            ctx.drawImage(
                videoRef.current,
                offsetX, offsetY, size, size,  // Sección a recortar del video
                0, 0, size, size               // Tamaño y posición en el canvas
            );
            
            // Convertir a base64
            const photoData = canvas.toDataURL('image/jpeg', 0.9);
            setPhotoSource(photoData);
            
            // Detener la cámara
            stopCamera();
            
            // Notificar al componente padre
            if (onPhotoTaken) {
                onPhotoTaken(photoData);
            }
        }
    };

    // Subir foto desde el dispositivo
    const uploadPhoto = (event) => {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (e) => {
                // Crear una imagen para obtener dimensiones
                const img = new Image();
                img.onload = () => {
                    // Crear un canvas cuadrado para el procesamiento
                    const canvas = document.createElement('canvas');
                    
                    // Usar el lado más pequeño para hacer un cuadrado
                    const size = Math.min(img.width, img.height);
                    canvas.width = size;
                    canvas.height = size;
                    
                    // Calcular posición para centrar la imagen
                    const offsetX = (img.width - size) / 2;
                    const offsetY = (img.height - size) / 2;
                    
                    // Dibujar la sección cuadrada de la imagen en el canvas
                    const ctx = canvas.getContext('2d');
                    ctx.drawImage(
                        img,
                        offsetX, offsetY, size, size,  // Sección a recortar de la imagen
                        0, 0, size, size               // Tamaño y posición en el canvas
                    );
                    
                    // Convertir a base64 con buena calidad
                    const photoData = canvas.toDataURL('image/jpeg', 0.9);
                    setPhotoSource(photoData);
                    setShowOptions(false);
                    
                    // Notificar al componente padre
                    if (onPhotoTaken) {
                        onPhotoTaken(photoData);
                    }
                };
                img.src = e.target.result;
            };
            reader.readAsDataURL(file);
        }
    };

    // Detener la cámara
    const stopCamera = () => {
        if (mediaStreamRef.current) {
            mediaStreamRef.current.getTracks().forEach(track => track.stop());
            mediaStreamRef.current = null;
        }
        setCameraActive(false);
    };

    // Reiniciar para tomar otra foto
    const resetCamera = () => {
        setPhotoSource(null);
        setShowOptions(true);
        stopCamera();
    };

    // Limpiar al desmontar el componente
    useEffect(() => {
        return () => {
            stopCamera();
        };
    }, []);

    return (
        <div className="cs-camera-screen">
            {/* Header con botón de retroceso */}
            <div className="conditions-header" style={{ backgroundColor: 'transparent' }}>
              <div className="back-wrapper">
                <button className="conditions-back-button" onClick={onBack} >
                    <svg xmlns="http://www.w3.org/2000/svg" width="17" height="15" viewBox="0 0 17 15" fill="none" style={{ color: 'white' }}>
                        <path d="M7.45408 13.6896L1.0805 7.47707L7.29305 1.10349M15.4646 7.29304L1.0805 7.47707L15.4646 7.29304Z" 
                        stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ stroke: 'white' }}/>
                    </svg>
                </button>

                    <div className="header-content">
                        <img src={logoDashboard} alt="HealthIA" className="header-logo" />
                        <div className="header-text">
                            <h1  style={{ color: 'white' }}>HealthIA</h1>
                            <p style={{ color: 'white' }}>Take a photo of your plate</p>
                        </div>
                    </div>
                    
                </div>
            </div>

            <div className="cs-camera-content">
                {/* Área de visualización (cámara o foto tomada) */}
                <div className="cs-camera-viewport">
                    {photoSource ? (
                        <div className="cs-camera-preview-container">
                            <img src={photoSource} alt="Foto capturada" className="cs-captured-photo" />
                        </div>
                    ) : cameraActive ? (
                        <div className="cs-camera-preview-container">
                            <video 
                                ref={videoRef} 
                                autoPlay 
                                playsInline 
                                className="cs-camera-preview" 
                            />
                            <div className="cs-plate-watermark">
                                <div className="cs-plate-section-labels">
                                    <span className="cs-plate-section-label label-vegetables">Vegetales (50%)</span>
                                    <span className="cs-plate-section-label label-carbs">Carbohidratos (25%)</span>
                                    <span className="cs-plate-section-label label-proteins">Proteínas (25%)</span>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="cs-camera-placeholder">
                            <div className="cs-camera-icon">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                                    <circle cx="12" cy="13" r="4"/>
                                </svg>
                            </div>
                            <p>Take or upload a photo of </p>
                            <p>your plate of food</p>
                        </div>
                    )}
                </div>

                {/* Botones de control */}
                <div className="cs-camera-controls">
                    {photoSource ? (
                        <>
                            <button className="cs-control-button confirm-button" onClick={() => onPhotoTaken && onPhotoTaken(photoSource)}>
                                Confirmar
                            </button>
                            <button className="cs-control-button retake-button" onClick={resetCamera}>
                                Volver a tomar
                            </button>
                        </>
                    ) : cameraActive ? (
                        <>
                            <button className="cs-control-button capture-button" onClick={takePhoto}>
                                <div className="cs-capture-icon"></div>
                            </button>
                            <button className="cs-control-button cancel-button" onClick={resetCamera}>
                                Cancelar
                            </button>
                        </>
                    ) : showOptions && (
                        <div className="cs-camera-options">
                            <button className="cs-option-button take-photo-button" onClick={startCamera} style={{ color: 'black', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="cs-upload-photo-icon">
                                    <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                                    <circle cx="12" cy="13" r="4"/>
                                </svg>
                                <p style={{ color: 'black' }}>Take photo</p>
                            </button>
                            <p className="cs-option-separator">o</p>
                            <label className="cs-option-button">
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="cs-upload-photo-icon">
                                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                                    <polyline points="17 8 12 3 7 8"/>
                                    <line x1="12" y1="3" x2="12" y2="15"/>
                                </svg>
                                Upload photo
                                <input 
                                    type="file" 
                                    accept="image/*" 
                                    onChange={uploadPhoto} 
                                    style={{ display: 'none' }} 
                                />
                            </label>
                        </div>
                    )}
                </div>

                {/* Instrucciones */}
                {showOptions && (
                    <div className="cs-camera-instructions">
                        <p>For better analysis:</p>
                        <ul>
                            <li>Make sure the whole plate is visible</li>
                            <li>Take the photo from above</li>
                            <li>Use good lighting</li>
                        </ul>
                    </div>
                )}
            </div>
        </div>
    );
};

export default CameraScreen; 