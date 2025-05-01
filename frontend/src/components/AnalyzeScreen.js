import React, { useState, useEffect } from 'react';
import "../styles/AnalyzeScreen.css";
import "../styles/SharedContainer.css";
import plateImage from "../assets/images/plate.png"; // Asegúrate de tener esta imagen
import CameraScreen from './CameraScreen';
import logoDashboard from "../assets/logos/logo_sign.png";
// URL de la API desde variables de entorno
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8000';

// Función para enviar la imagen al backend para análisis
const analizarImagen = async (imageFile, nextId) => {
    // Validar que nextId sea un valor válido
    if (!nextId || isNaN(parseInt(nextId))) {
        console.error('Error: ID inválido para el análisis', nextId);
        throw new Error('ID inválido para el análisis de imagen');
    }
    
    const formData = new FormData();
    
    // Convertir base64 a Blob si es necesario
    let fileToSend = imageFile;
    if (typeof imageFile === 'string' && imageFile.startsWith('data:image')) {
        const response = await fetch(imageFile);
        const blob = await response.blob();
        const timestamp = new Date().getTime();
        const fileName = `food_photo_${timestamp}.jpg`;
        fileToSend = new File([blob], fileName, { type: 'image/jpeg' });
    }
    
    // Campos requeridos según la API
    const idToSend = parseInt(nextId).toString(); // Asegurar que sea un string numérico
    formData.append('id', idToSend);
    formData.append('media_file', fileToSend);

    try {
        console.log('🍽️ Enviando imagen para análisis:', {
            id: idToSend,
            tamaño: fileToSend instanceof File ? `${(fileToSend.size / 1024).toFixed(2)}KB` : 'N/A',
            timestamp: new Date().toISOString()
        });

        const response = await fetch(`${API_URL}/analyze-image`, {
            method: 'PUT',
            body: formData,
            headers: {
                'Accept': 'application/json',
                'ngrok-skip-browser-warning': '69420'
            }
        });

        if (!response.ok) {
            throw new Error(`Error HTTP: ${response.status}`);
        }

        const data = await response.json();
        console.log('✅ Respuesta del análisis:', data);
        return data;
    } catch (error) {
        console.error('❌ Error al analizar la imagen:', error.message);
        throw error;
    }
};

// Función para obtener la lista de análisis del usuario
const obtenerAnalisis = async () => {
    try {
        console.log("Solicitando lista de análisis a:", `${API_URL}/list-analyses`);
        
        const response = await fetch(`${API_URL}/list-analyses`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'ngrok-skip-browser-warning': '69420'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error(`Error HTTP: ${response.status}`, errorText);
            throw new Error(`Error HTTP: ${response.status}`);
        }

        const data = await response.json();
        
        // Imprimir el JSON completo para verificar su estructura y contenido
        console.log("JSON COMPLETO DE ANÁLISIS:", JSON.stringify(data, null, 2));
        console.log("Cantidad de análisis recibidos:", data.analyses ? data.analyses.length : 0);
        console.log("IDs de análisis recibidos:", data.analyses ? data.analyses.map(a => a.id).join(', ') : 'ninguno');
        console.log("Next ID según el servidor:", data.next_id);
        
        // Verificar que la respuesta contenga el campo next_id
        if (data && typeof data.next_id !== 'number') {
            console.warn("La respuesta del servidor no contiene un next_id válido:", data);
            // Si no hay next_id, intentamos calcularlo a partir del historial
            if (data.analyses && Array.isArray(data.analyses) && data.analyses.length > 0) {
                const maxId = Math.max(...data.analyses.map(item => parseInt(item.id) || 0));
                data.next_id = maxId + 1;
                console.log("next_id calculado a partir del historial:", data.next_id);
            } else {
                // Si no hay historial, comenzamos desde 1
                data.next_id = 1;
                console.log("No hay historial, usando next_id = 1");
            }
        }
        
        return data;
    } catch (error) {
        console.error('Error al obtener análisis:', error.message);
        // Devolver un objeto vacío con la estructura esperada para evitar errores
        return { analyses: [], next_id: 1 };
    }
};

// Función para eliminar un análisis del historial
const eliminarAnalisis = async (id) => {
    try {
        console.log(`Eliminando análisis con ID: ${id}`);
        
        const response = await fetch(`${API_URL}/delete-analysis`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'ngrok-skip-browser-warning': '69420'
            },
            body: JSON.stringify({ id })
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error(`Error al eliminar análisis: ${response.status}`, errorText);
            throw new Error(`Error HTTP: ${response.status}`);
        }

        console.log(`Análisis con ID ${id} eliminado correctamente`);
        return true;
    } catch (error) {
        console.error('Error al eliminar análisis:', error.message);
        throw error;
    }
};

// Ideal de la Harvard Plate (porcentajes)
const HARVARD_PLATE = {
    "Verduras/vegetales": 50,
    "Carbohidratos": 25,
    "Proteínas": 25
};

const AnalyzeScreen = ({ onBack }) => {
    const [showCamera, setShowCamera] = useState(false);
    const [capturedPhoto, setCapturedPhoto] = useState(null);
    const [isAnalyzing, setIsAnalyzing] = useState(false);
    const [analysisResult, setAnalysisResult] = useState(null);
    const [analysisError, setAnalysisError] = useState(null);
    const [currentTab, setCurrentTab] = useState('results'); // 'results' o 'original'
    const [analysisHistory, setAnalysisHistory] = useState([]); // Historial de análisis
    const [showHistory, setShowHistory] = useState(false); // Controlar visibilidad del historial
    const [isLoadingHistory, setIsLoadingHistory] = useState(false); // Estado de carga del historial
    const [historyError, setHistoryError] = useState(null); // Error específico del historial
    const [isDeletingAnalysis, setIsDeletingAnalysis] = useState(false); // Estado para la eliminación
    const [deleteError, setDeleteError] = useState(null); // Error al eliminar

    // Adaptamos nuestros estados para el nuevo formato
    const [foodCategories, setFoodCategories] = useState({});
    const [recommendations, setRecommendations] = useState([]);
    const [processedImageUrl, setProcessedImageUrl] = useState('');
    
    // Cargar historial de análisis cuando se monte el componente
    useEffect(() => {
        loadAnalysisHistory();
    }, []);

    // Función para cargar el historial de análisis
    const loadAnalysisHistory = async () => {
        try {
            setIsLoadingHistory(true);
            setHistoryError(null); // Limpiar errores previos
            const response = await obtenerAnalisis();
            
            console.log("Respuesta completa para el historial:", response);
            
            // Verificar si la respuesta tiene la estructura esperada con "analyses"
            if (response && response.analyses && Array.isArray(response.analyses)) {
                console.log("Cantidad de análisis a procesar:", response.analyses.length);
                
                // Procesar cada análisis para adaptarlo al formato que espera nuestra UI
                const processedHistory = response.analyses.map(item => {
                    console.log("Procesando análisis con ID:", item.id);
                    
                    // Extraer datos del objeto anidado "analisis" y combinarlos con los datos del nivel superior
                    return {
                        id: item.id,
                        fecha: item.fecha,
                        evaluacion_general: item.analisis?.evaluacion_general || "Análisis realizado",
                        porcentaje_verduras: item.analisis?.porcentaje_verduras || 0,
                        porcentaje_proteinas: item.analisis?.porcentaje_proteinas || 0,
                        porcentaje_carbohidratos: item.analisis?.porcentaje_carbohidratos || 0,
                        detalle_alimentos: item.analisis?.detalle_alimentos || [],
                        recomendaciones: item.analisis?.recomendaciones || [],
                        imagen_original_url: item.imagen_original_url || item.analisis?.imagen_original_url,
                        imagen_procesada_url: item.imagen_procesada_url || item.analisis?.imagen_procesada_url
                    };
                });
                
                console.log("Historial procesado completo:", processedHistory);
                console.log("IDs en el historial procesado:", processedHistory.map(item => item.id).join(', '));
                
                setAnalysisHistory(processedHistory);
                console.log("Historial cargado con éxito. Total de elementos:", processedHistory.length);
            } else {
                console.error("Formato de respuesta inesperado:", response);
                setAnalysisHistory([]);
                setHistoryError("No se pudo cargar el historial. Formato de respuesta inesperado.");
            }
        } catch (error) {
            console.error('Error al cargar el historial:', error);
            setAnalysisHistory([]);
            setHistoryError("No se pudo cargar el historial. Por favor, intente nuevamente.");
        } finally {
            setIsLoadingHistory(false);
        }
    };

    // Actualizar datos del análisis cuando se reciba el resultado
    useEffect(() => {
        if (analysisResult) {
            // Ahora usamos directamente los porcentajes proporcionados por la API
            const categoriesData = {
                "Verduras/vegetales": analysisResult.porcentaje_verduras || 0,
                "Carbohidratos": analysisResult.porcentaje_carbohidratos || 0,
                "Proteínas": analysisResult.porcentaje_proteinas || 0
            };
            
            setFoodCategories(categoriesData);
            
            // Usamos directamente las recomendaciones proporcionadas por la API
            if (analysisResult.recomendaciones && analysisResult.recomendaciones.length > 0) {
                setRecommendations(analysisResult.recomendaciones);
            }
            
            // Guardar URL de la imagen procesada
            if (analysisResult.imagen_procesada_url) {
                setProcessedImageUrl(analysisResult.imagen_procesada_url);
            }
        }
    }, [analysisResult]);

    // Función para manejar la foto tomada
    const handlePhotoTaken = (photoData) => {
        setCapturedPhoto(photoData);
        setShowCamera(false);
        setAnalysisResult(null);
        setAnalysisError(null);
        console.log("Photo captured, ready for analysis");
    };

    // Función para analizar la foto
    const handleAnalyzePhoto = async () => {
        if (!capturedPhoto) return;
        
        setIsAnalyzing(true);
        setAnalysisError(null);
        
        try {
            // Primero obtenemos el next_id del historial
            const historialData = await obtenerAnalisis();
            
            // Verificar que tengamos un next_id válido del servidor
            if (!historialData || typeof historialData.next_id !== 'number') {
                console.error("Error: No se pudo obtener un ID válido del servidor", historialData);
                throw new Error("No se pudo obtener un ID válido para el análisis");
            }
            
            const nextId = historialData.next_id;
            console.log("Usando next_id del servidor:", nextId);
            
            // Enviar la foto para análisis con el next_id
            const resultado = await analizarImagen(capturedPhoto, nextId);
            console.log("Resultado del análisis:", resultado);
            setAnalysisResult(resultado);
            
            // Actualizar el historial después de un análisis exitoso
            console.log("Actualizando historial después del análisis exitoso...");
            await loadAnalysisHistory();
            console.log("Historial actualizado correctamente");
        } catch (error) {
            console.error("Error al analizar la foto:", error);
            setAnalysisError("No se pudo analizar la imagen. Por favor, intente nuevamente.");
        } finally {
            setIsAnalyzing(false);
        }
    };

    // Función para manejar la eliminación de un análisis
    const handleDeleteAnalysis = async (id, event) => {
        // Detener la propagación para evitar que se seleccione el análisis
        event.stopPropagation();
        
        if (!id) {
            console.error("Invalid analysis ID");
            return;
        }
        
        // Confirmar antes de eliminar
        if (!window.confirm("Are you sure you want to delete this analysis? This action cannot be undone.")) {
            return;
        }
        
        try {
            setIsDeletingAnalysis(true);
            setDeleteError(null);
            
            await eliminarAnalisis(id);
            
            // Actualizar el historial después de eliminar
            loadAnalysisHistory();
            
            // Si el análisis actual es el que se eliminó, limpiar el resultado
            if (analysisResult && analysisResult.id === id) {
                setAnalysisResult(null);
                setFoodCategories({});
                setRecommendations([]);
                setProcessedImageUrl('');
            }
        } catch (error) {
            console.error("Error deleting analysis:", error);
            setDeleteError("The analysis could not be deleted. Please try again.");
        } finally {
            setIsDeletingAnalysis(false);
        }
    };

    // Si estamos mostrando la cámara, renderiza el componente CameraScreen
    if (showCamera) {
        return <CameraScreen 
            onBack={() => setShowCamera(false)} 
            onPhotoTaken={handlePhotoTaken} 
        />;
    }

    // Renderizado
    return (
        <div className="screen-container">
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
                            <p>Analyze your plate</p>
                        </div>
                    </div>
                    
                </div>
            </div>

            <div className="ap-analyze-content">
                {!analysisResult ? (
                    // Pantalla inicial con instrucciones
                    <div className="ap-analyze-introduction">
                        {!capturedPhoto ? (
                            <>
                                <div className="ap-plate-container">
                                    <img src={plateImage} alt="Plato saludable" className="ap-plate-image" />
                                </div>
                            <p className="ap-analyze-description">
                                Take a photo of your plate of food to receive a nutritional
                                <p>analysis and personalized recommendations.</p>
                            </p>
                            
                                
                                {/* Botón para mostrar/ocultar historial */}
                                <button 
                                    className={`ap-history-toggle-button ${showHistory ? 'active' : ''}`}
                                    onClick={() => setShowHistory(!showHistory)}
                                >
                                    <div className="ap-history-toggle-icon">
                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M13 3C8.03 3 4 7.03 4 12H1L4.89 15.89L4.96 16.03L9 12H6C6 8.13 9.13 5 13 5C16.87 5 20 8.13 20 12C20 15.87 16.87 19 13 19C11.07 19 9.32 18.21 8.06 16.94L6.64 18.36C8.27 19.99 10.51 21 13 21C17.97 21 22 16.97 22 12C22 7.03 17.97 3 13 3ZM12 8V13L16.28 15.54L17 14.33L13.5 12.25V8H12Z" fill="currentColor"/>
                                        </svg>
                                    </div>
                                    <span className="ap-history-toggle-text">
                                        {showHistory ? 'Hide history' : 'View analysis history'}
                                    </span>
                                    <div className="ap-history-toggle-arrow">
                                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d={showHistory ? "M7.41 15.41L12 10.83L16.59 15.41L18 14L12 8L6 14L7.41 15.41Z" : "M7.41 8.59L12 13.17L16.59 8.59L18 10L12 16L6 10L7.41 8.59Z"} fill="currentColor"/>
                                        </svg>
                                    </div>
                                </button>
                                
                                {/* Historial de análisis */}
                                {showHistory && (
                                    <div className="ap-analysis-history-container">
                                        <div className="ap-history-header">
                                            <h3 className="ap-history-title">Analysis history</h3>
                                            <button 
                                                className="ap-history-refresh-button" 
                                                onClick={loadAnalysisHistory}
                                                disabled={isLoadingHistory}
                                            >
                                                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M13.65 2.35C12.2 0.9 10.21 0 8 0C3.58 0 0 3.58 0 8C0 12.42 3.58 16 8 16C11.73 16 14.84 13.45 15.73 10H13.65C12.83 12.33 10.61 14 8 14C4.69 14 2 11.31 2 8C2 4.69 4.69 2 8 2C9.66 2 11.14 2.69 12.22 3.78L9 7H16V0L13.65 2.35Z" fill="currentColor"/>
                                                </svg>
                                            </button>
                                        </div>
                                        
                                        {isLoadingHistory ? (
                                            <div className="ap-history-loading">
                                                <div className="ap-loading-spinner"></div>
                                                <p>Loading history...</p>
                                            </div>
                                        ) : historyError ? (
                                            <div className="ap-history-error">
                                                <p>{historyError}</p>
                                                <button 
                                                    className="ap-history-retry-button"
                                                    onClick={loadAnalysisHistory}
                                                >
                                                    Retry
                                                </button>
                                            </div>
                                        ) : analysisHistory.length === 0 ? (
                                            <p className="ap-history-empty-message">
                                                No prior analysis. Analyze your first dish!
                                            </p>
                                        ) : (
                                            <ul className="ap-history-list">
                                                {console.log("Renderizando historial. Total de elementos:", analysisHistory.length, "IDs:", analysisHistory.map(a => a.id).join(', '))}
                                                {analysisHistory.map((analysis, index) => (
                                                    <li 
                                                        key={analysis.id || index} 
                                                        className="ap-history-item"
                                                        onClick={() => {
                                                            // Cargar análisis previo
                                                            setAnalysisResult(analysis);
                                                            
                                                            // Configurar datos para visualización
                                                            const categoriesData = {
                                                                "Verduras/vegetales": analysis.porcentaje_verduras || 0,
                                                                "Carbohidratos": analysis.porcentaje_carbohidratos || 0,
                                                                "Proteínas": analysis.porcentaje_proteinas || 0
                                                            };
                                                            setFoodCategories(categoriesData);
                                                            
                                                            if (analysis.recomendaciones && analysis.recomendaciones.length > 0) {
                                                                setRecommendations(analysis.recomendaciones);
                                                            }
                                                            
                                                            if (analysis.imagen_procesada_url) {
                                                                setProcessedImageUrl(analysis.imagen_procesada_url);
                                                            } else if (analysis.imagen_original_url) {
                                                                // Si no hay imagen procesada, usamos la original
                                                                setProcessedImageUrl(analysis.imagen_original_url);
                                                            }
                                                            
                                                            // Ocultar el historial después de seleccionar
                                                            setShowHistory(false);
                                                        }}
                                                    >
                                                        <div className="ap-history-item-image">
                                                            {analysis.imagen_procesada_url ? (
                                                                <img src={analysis.imagen_procesada_url} alt="Análisis previo" />
                                                            ) : analysis.imagen_original_url ? (
                                                                <img src={analysis.imagen_original_url} alt="Análisis previo" />
                                                            ) : (
                                                                <div className="ap-history-no-image">
                                                                    <span>Sin imagen</span>
                                                                </div>
                                                            )}
                                                        </div>
                                                        <div className="ap-history-item-info">
                                                            <span className="ap-history-item-date">
                                                                {new Date(analysis.fecha || Date.now()).toLocaleDateString()}
                                                            </span>
                                                            <span className="ap-history-item-evaluation">
                                                                {analysis.evaluacion_general || "Análisis realizado"}
                                                            </span>
                                                        </div>
                                                        <div className="ap-history-item-actions">
                                                            <button 
                                                                className="ap-history-delete-button"
                                                                onClick={(e) => handleDeleteAnalysis(analysis.id, e)}
                                                                disabled={isDeletingAnalysis}
                                                                title="Eliminar análisis"
                                                            >
                                                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z" fill="currentColor"/>
                                                                </svg>
                                                            </button>
                                                            <div className="ap-history-item-arrow">
                                                                <svg width="8" height="12" viewBox="0 0 8 12" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                    <path d="M1.5 1L6.5 6L1.5 11" stroke="#0957DE" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                </svg>
                                                            </div>
                                                        </div>
                                                    </li>
                                                ))}
                                            </ul>
                                        )}
                                    </div>
                                )}
                            
                            </>
                        ) : (
                            <div className="ap-captured-image-container">
                                <img src={capturedPhoto} alt="Tu plato de comida" className="ap-captured-food-image" />
                                <button className="ap-retake-photo-button" onClick={() => setShowCamera(true)}>
                                Take another photo
                                </button>
                            </div>
                        )}
                    </div>
                ) : (
                    // Resultados del análisis
                    <div className="ap-analysis-results-container">
                        <h2 className="ap-analysis-main-title">Resultado del análisis</h2>
                        <p className="ap-analysis-subtitle">Basado en el Plato de Harvard</p>
                        
                        {/* Pestañas para alternar entre vista */}
                        <div className="ap-analysis-tabs">
                            <button 
                                className={`ap-tab-button ${currentTab === 'results' ? 'ap-active' : ''}`}
                                onClick={() => setCurrentTab('results')}
                            >
                                <span className="ap-tab-circle ap-blue"></span>
                                Mi plato
                            </button>
                            <button 
                                className={`ap-tab-button ${currentTab === 'original' ? 'ap-active' : ''}`}
                                onClick={() => setCurrentTab('original')}
                            >
                                <span className="ap-tab-circle ap-orange"></span>
                                El Plato de Harvard
                            </button>
                        </div>

                        {currentTab === 'results' ? (
                            <div>
                                {/* Evaluación general */}
                                {analysisResult && analysisResult.evaluacion_general && (
                                    <div className="ap-general-evaluation">
                                        <h3 className="ap-evaluation-title">Evaluación general:</h3>
                                        <p className="ap-evaluation-text">{analysisResult.evaluacion_general}</p>
                                    </div>
                                )}
                                
                                {/* Gráfico comparativo */}
                                <div className="ap-analysis-chart-container">
                                    <div className="ap-chart-area">
                                        <div className="ap-chart-y-axis">
                                            <span>100%</span>
                                            <span>75%</span>
                                            <span>50%</span>
                                            <span>25%</span>
                                            <span>0%</span>
                                        </div>
                                        <div className="ap-chart-bars">
                                            {/* Barra para Vegetales */}
                                            <div className="ap-chart-category">
                                                <div className="ap-chart-bars-container">
                                                    <div className="ap-bar-wrapper">
                                                        <div className="ap-chart-bar ap-blue" style={{ height: `${foodCategories["Verduras/vegetales"] || 0}%` }}>
                                                            {(foodCategories["Verduras/vegetales"] || 0).toFixed(1)}%
                                                        </div>
                                                        <div className="ap-chart-bar ap-orange" style={{ height: `${HARVARD_PLATE["Verduras/vegetales"]}%` }}>
                                                            {HARVARD_PLATE["Verduras/vegetales"]}%
                                                        </div>
                                                    </div>
                                                </div>
                                                <div className="ap-chart-label">Vegetales</div>
                                            </div>
                                            
                                            {/* Barra para Carbohidratos */}
                                            <div className="ap-chart-category">
                                                <div className="ap-chart-bars-container">
                                                    <div className="ap-bar-wrapper">
                                                        <div className="ap-chart-bar ap-blue" style={{ height: `${foodCategories["Carbohidratos"] || 0}%` }}>
                                                            {(foodCategories["Carbohidratos"] || 0).toFixed(1)}%
                                                        </div>
                                                        <div className="ap-chart-bar ap-orange" style={{ height: `${HARVARD_PLATE["Carbohidratos"]}%` }}>
                                                            {HARVARD_PLATE["Carbohidratos"]}%
                                                        </div>
                                                    </div>
                                                </div>
                                                <div className="ap-chart-label">Carbohidratos</div>
                                            </div>
                                            
                                            {/* Barra para Proteínas */}
                                            <div className="ap-chart-category">
                                                <div className="ap-chart-bars-container">
                                                    <div className="ap-bar-wrapper">
                                                        <div className="ap-chart-bar ap-blue" style={{ height: `${foodCategories["Proteínas"] || 0}%` }}>
                                                            {(foodCategories["Proteínas"] || 0).toFixed(1)}%
                                                        </div>
                                                        <div className="ap-chart-bar ap-orange" style={{ height: `${HARVARD_PLATE["Proteínas"]}%` }}>
                                                            {HARVARD_PLATE["Proteínas"]}%
                                                        </div>
                                                    </div>
                                                </div>
                                                <div className="ap-chart-label">Proteínas</div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="ap-chart-legend">
                                        <div className="ap-legend-item">
                                            <div className="ap-legend-color ap-blue"></div>
                                            <span>Tu plato</span>
                                        </div>
                                        <div className="ap-legend-item">
                                            <div className="ap-legend-color ap-orange"></div>
                                            <span>Plato de Harvard (ideal)</span>
                                        </div>
                                    </div>
                                </div>
                                
                                {/* Recomendaciones */}
                                <div className="ap-recommendations-section">
                                    <h3 className="ap-recommendations-title">Recomendaciones para un plato más saludable</h3>
                                    <ul className="ap-recommendations-list">
                                        {recommendations.map((recommendation, index) => (
                                            <li key={index} className="ap-recommendation-item">
                                                {recommendation}
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                                
                                <button className="ap-adjust-button">
                                    ¡Haz pequeños ajustes para un balance más saludable!
                                </button>
                                
                                {/* Imagen procesada (ahora al final) */}
                                <div className="ap-processed-image-section">
                                    {processedImageUrl ? (
                                        <div className="ap-processed-image-wrapper">
                                            <h3 className="ap-processed-image-title">Imagen de tu plato analizado</h3>
                                            <img 
                                                src={processedImageUrl} 
                                                alt="Plato analizado con etiquetas" 
                                                className="ap-processed-image" 
                                            />
                                            <p className="ap-processed-image-caption">
                                                Plato analizado con IA
                                            </p>
                                        </div>
                                    ) : (
                                        <div className="ap-processed-image-wrapper">
                                            <h3 className="ap-processed-image-title">Tu plato</h3>
                                            <img 
                                                src={capturedPhoto} 
                                                alt="Tu plato de comida" 
                                                className="ap-captured-food-image" 
                                            />
                                            <p className="ap-processed-image-caption">
                                                Imagen original
                                            </p>
                                        </div>
                                    )}
                                </div>
                            </div>
                        ) : (
                            // Pestaña de detalles del plato
                            <div className="ap-food-details-container">
                                <h3 className="ap-food-details-title">Alimentos identificados:</h3>
                                <ul className="ap-food-details-list">
                                    {analysisResult && analysisResult.detalle_alimentos && 
                                        analysisResult.detalle_alimentos.map((food, index) => (
                                            <li key={index} className="ap-food-item">
                                                <span className="ap-food-name">{food.nombre} ({food.categoria})</span>
                                                <span className="ap-food-percentage">{food.porcentaje_area.toFixed(1)}%</span>
                                            </li>
                                        ))
                                    }
                                </ul>
                            </div>
                        )}
                    </div>
                )}
                
                {/* Mensaje de error */}
                {analysisError && (
                    <div className="ap-analysis-error">
                        <p>{analysisError}</p>
                </div>
                )}
            </div>

            {/* Botón de análisis o tomar foto (solo visible si no hay resultados) */}
            {!analysisResult && (
                capturedPhoto ? (
                    <button 
                        className={`ap-nutrition-button ${isAnalyzing ? 'loading' : ''}`}
                        onClick={handleAnalyzePhoto}
                        disabled={isAnalyzing}
                    >
                        {isAnalyzing ? (
                            <div className="ap-loading-spinner"></div>
                        ) : (
                            "Analyze your plate"
                        )}
                    </button>
                ) : (
                    <button 
                        className="ap-nutrition-button" 
                        onClick={() => setShowCamera(true)}
                    >
                        Take a photo of your dish
                    </button>
                )
            )}
            
            {/* Botón para tomar otra foto (visible cuando hay resultados) */}
            {analysisResult && (
                <button 
                    className="ap-take-another-photo-button" 
                    onClick={() => {
                        setShowCamera(true);
                        setAnalysisResult(null);
                    }}
                >
                    Analizar otro plato
                </button>
            )}

            {/* Mensaje de error al eliminar */}
            {deleteError && (
                <div className="ap-history-delete-error">
                    <p>{deleteError}</p>
                    <button 
                        className="ap-history-error-close"
                        onClick={() => setDeleteError(null)}
                    >
                        Cerrar
            </button>
                </div>
            )}
        </div>
    );
};

export default AnalyzeScreen; 