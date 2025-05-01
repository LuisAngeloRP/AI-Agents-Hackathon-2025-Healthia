import React, { useEffect, useState } from "react";
import "../styles/FitnessDashboard.css";
import "../styles/SharedContainer.css";
import logoDashboard from "../assets/logos/logo_dashboard.png";
import playIcon from "../assets/icons/play.png";
import heartIcon from "../assets/icons/heart.svg";
import sleepIcon from "../assets/icons/sleep.svg";
import ActivitiesScreen from "./ActivitiesScreen";
import ProfileScreen from "./ProfileScreen";
import DeviceScreen from "./DeviceScreen";

const FitnessDashboard = ({ onNavigate }) => {
    const [isLoaded, setIsLoaded] = useState(false);
    const [heartRate, setHeartRate] = useState(85);
    const [heartBeat, setHeartBeat] = useState(false);
    const [heartPath, setHeartPath] = useState("");
    const [currentX, setCurrentX] = useState(0);
    const [lastBeatTime, setLastBeatTime] = useState(Date.now());
    const [selectedDay, setSelectedDay] = useState(null);
    const [selectedWorkoutDay, setSelectedWorkoutDay] = useState(null);
    const [isHeartIconBeating, setIsHeartIconBeating] = useState(false);
    
    const [showActivities, setShowActivities] = useState(false);
    const [showProfile, setShowProfile] = useState(false);
    const [showDevice, setShowDevice] = useState(false);

    // Obtener el día actual y su posición en el gráfico
    const getCurrentDayPosition = () => {
        const today = new Date().getDay();
        // Ajustamos el cálculo para alinear con los días (dividimos el ancho en 7 secciones)
        const sectionWidth = 287 / 7;
        return (sectionWidth * today) + (sectionWidth / 2); // Centramos en cada sección
    };

    const todayX = getCurrentDayPosition();
    const todayName = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'][new Date().getDay()];

    const getCurrentDayLetter = () => {
        const days = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];
        return days[new Date().getDay()];
    };

    const sleepData = [
        { day: 'Mo', hours: 7.5, height: '75%' },
        { day: 'Tu', hours: 8.5, height: '85%' },
        { day: 'We', hours: 6.5, height: '65%' },
        { day: 'Th', hours: 8.0, height: '80%' },
        { day: 'Fr', hours: 7.0, height: '70%' },
        { day: 'Sa', hours: 9.0, height: '90%' },
        { day: 'Su', hours: 7.5, height: '75%' }
    ];

    // Datos de workout por día
    const workoutData = [
        { day: 'Sun', hours: 2.5 },
        { day: 'Mon', hours: 4.0 },
        { day: 'Tue', hours: 3.5 },
        { day: 'Wed', hours: 5.0 },
        { day: 'Thu', hours: 3.0 },
        { day: 'Fri', hours: 4.5 },
        { day: 'Sat', hours: 2.0 }
    ];

    useEffect(() => {
        setIsLoaded(true);

        // Función para generar un latido cardíaco realista
        const generateHeartbeat = (x) => {
            const segment = x % 100;
            
            if (segment < 35) { // Línea base
                return 50;
            } else if (segment < 40) { // Onda P
                return 50 - Math.sin((segment - 35) * Math.PI / 5) * 5;
            } else if (segment < 45) { // Segmento PR
                return 50;
            } else if (segment < 47) { // Onda Q
                return 50 + (segment - 45) * 10;
            } else if (segment < 50) { // Onda R (pico alto)
                return 15;
            } else if (segment < 52) { // Onda S (pico bajo)
                return 85;
            } else if (segment < 55) { // Segmento ST
                return 50;
            } else if (segment < 65) { // Onda T
                return 50 - Math.sin((segment - 55) * Math.PI / 10) * 8;
            } else {
                return 50; // Línea base
            }
        };

        // Función para actualizar el path del corazón
        const updateHeartPath = () => {
            const now = Date.now();
            setCurrentX(prev => {
                const newX = (prev + 1) % 200;
                
                // Detectar cuando ocurre un latido (al inicio del ciclo)
                if (newX === 47) { // Punto R del complejo QRS
                    const timeSinceLastBeat = now - lastBeatTime;
                    setLastBeatTime(now);
                    
                    // Calcular BPM basado en el tiempo entre latidos
                    const newBPM = Math.round(60000 / timeSinceLastBeat);
                    setHeartRate(prev => {
                        // Mantener BPM en un rango realista para una exposición (80-120)
                        const targetBPM = Math.min(Math.max(newBPM, 80), 120);
                        // Suavizar cambios y añadir variabilidad
                        const variation = Math.random() * 4 - 2; // Variación de ±2 BPM
                        const newHeartRate = Math.round((prev + targetBPM) / 2 + variation);
                        
                        // Activar la animación del ícono
                        setIsHeartIconBeating(true);
                        setTimeout(() => setIsHeartIconBeating(false), 500);
                        
                        return newHeartRate;
                    });
                    setHeartBeat(true);
                    setTimeout(() => setHeartBeat(false), 150);
                }
                
                return newX;
            });
            
            let path = "M0,50 ";
            for (let i = 0; i < 100; i++) {
                const x = (currentX + i) % 100;
                const y = generateHeartbeat(x);
                path += `L${i},${y} `;
            }
            setHeartPath(path);
        };

        // Actualizar el gráfico cada 25ms para una animación más fluida
        const heartPathInterval = setInterval(updateHeartPath, 25);

        return () => {
            clearInterval(heartPathInterval);
        };
    }, [currentX, lastBeatTime]);

    if (showActivities) {
        return <ActivitiesScreen onBack={() => setShowActivities(false)} />;
    }

    if (showDevice) {
        return <DeviceScreen onBack={() => setShowDevice(false)} previousScreen="health" />;
    }

    if (showProfile) {
        return <ProfileScreen 
            onNavigate={onNavigate}
            previousScreen="health"
        />;
    }


    return (
        <>
            <div className="condition-screen" style={{backgroundColor: "rgba(221, 221, 221, 0.03)"}}>
                {/* 🔹 Barra de navegación */}
                <div className="activities-navbar">
                    <span className="active-tab">My Health</span>
                    <span onClick={() => setShowActivities(true)}>Activities</span>
                    <span onClick={() => setShowProfile(true)}>Profile</span>
                </div>
                {/* 🔹 Rectángulo Azul (Bienvenida) */}
                <div className="dashboard-welcome">
                    <div className="welcome-stats">
                        <div className="stat-item">
                            <h3>1350 kcal</h3>
                            <p>Eaten</p>
                        </div>
                        <div className="stat-item">
                            <h3>680 kcal</h3>
                            <p>Burned</p>
                        </div>
                    </div>
                    <div className="welcome-user">
                        <img src={logoDashboard} alt="Logo" className="dashboard-logo" />
                        <div className="user-greeting-container">
                            <div className="user-greeting">
                                <span>HI</span>
                                <span>FRANSUA</span>
                            </div>
                            <img 
                                src={playIcon} 
                                alt="Play" 
                                className="play-icon" 
                                onClick={() => setShowDevice(true)}
                            />
                        </div>
                    </div>
                </div>

                {/* 🔹 Sección de Actividad */}
                <div className="dashboard-activity">
                    <div className="activity-ring">
                        <svg viewBox="-5 -5 110 110" className={`ring ${isLoaded ? 'animate' : ''}`}>
                            {/* Fondo de los anillos */}
                            <circle className="ring-bg" cx="50" cy="50" r="48"/>
                            <circle className="ring-bg" cx="50" cy="50" r="38"/>
                            <circle className="ring-bg" cx="50" cy="50" r="28"/>
                            
                            {/* Anillos de progreso */}
                            <circle 
                                className="ring-progress standing" 
                                cx="50" 
                                cy="50" 
                                r="28"
                                strokeDasharray="176"
                                strokeDashoffset="176"
                            />
                            <circle 
                                className="ring-progress exercise" 
                                cx="50" 
                                cy="50" 
                                r="38"
                                strokeDasharray="239"
                                strokeDashoffset="239"
                            />
                            <circle 
                                className="ring-progress movement" 
                                cx="50" 
                                cy="50" 
                                r="48"
                                strokeDasharray="302"
                                strokeDashoffset="302"
                            />

                            {/* Indicadores de inicio */}
                            <circle className="ring-start movement" cx="98" cy="50" r="5"/>
                            <circle className="ring-start exercise" cx="88" cy="50" r="5"/>
                            <circle className="ring-start standing" cx="78" cy="50" r="5"/>
                        </svg>
                    </div>
                    <div className="activity-stats">
                        <div className="activity-item">
                            <div className="activity-info">
                                <span className="dot movement"></span>
                                <span className="label">Movement</span>
                            </div>
                            <span className="value"><span className="completed">240</span>/270 <p > kcal</p></span>
                        </div>
                        <div className="activity-item">
                            <div className="activity-info">
                                <span className="dot exercise"></span>
                                <span className="label">Exercise</span>
                            </div>
                            <span className="value"><span className="completed">20</span>/25 <p>min</p></span>
                        </div>
                        <div className="activity-item">
                            <div className="activity-info">
                                <span className="dot standing"></span>
                                <span className="label">Standing</span>
                            </div>
                            <span className="value"><span className="completed">8</span>/12 <p>h</p></span>
                        </div>
                    </div>
                </div>

                {/* 🔹 Sección de métricas */}
                <div className="dashboard-metrics">
                    <div className="metric-box">
                        <div className="metric-header">
                            <h4>Sleep</h4>
                            <img src={sleepIcon} alt="Sleep" className="metric-icon" />
                        </div>
                        <div className="sleep-graph">
                            {sleepData.map((data, index) => (
                                <div 
                                    key={index} 
                                    className={`sleep-bar ${selectedDay === index ? 'active' : ''} ${data.day === getCurrentDayLetter() ? 'today' : ''}`}
                                    style={{ height: data.height }}
                                    onClick={() => setSelectedDay(selectedDay === index ? null : index)}
                                >
                                    <span className="sleep-day">{data.day}</span>
                                    <span className="sleep-hours">{data.hours}h</span>
                                </div>
                            ))}
                        </div>
                        <p>{selectedDay !== null ? `${sleepData[selectedDay].day}: ${sleepData[selectedDay].hours} hours` : 'Average: 7.5 hours'}</p>
                    </div>

                    <div className="metric-box">
                        <div className="metric-header">
                            <h4>Heart</h4>
                            <img 
                                src={heartIcon} 
                                alt="Heart" 
                                className={`metric-icon ${isHeartIconBeating ? 'beating' : ''}`} 
                            />
                        </div>
                        <div className={`heart-graph ${heartBeat ? 'beat' : ''}`}>
                            <svg viewBox="0 0 100 100" className="heart-line" preserveAspectRatio="none">
                                <path
                                    className="heart-path"
                                    d={heartPath}
                                    fill="none"
                                    stroke="#FF0000"
                                    strokeWidth="1.5"
                                />
                            </svg>
                        </div>
                        <p>{heartRate} bpm</p>
                    </div>
                </div>

                {/* 🔹 Sección de Progreso */}
                <div className="dashboard-progress">
                    <div className="metric-header1">
                        <h4>Workout Progress</h4>
                    </div>
                    <div className="progress-graph">
                        <div className="time-markers">
                            <span>10h</span>
                            <span>8h</span>
                            <span>6h</span>
                            <span>4h</span>
                            <span>2h</span>
                            <span>0h</span>
                        </div>
                        <div className="graph-container">
                            <svg viewBox="0 0 287 97" preserveAspectRatio="none" className={`progress-line ${isLoaded ? 'animate' : ''}`}>
                                <defs>
                                    <linearGradient id="progressGradient" x1="143.5" y1="-44.6083" x2="140.817" y2="118.262" gradientUnits="userSpaceOnUse">
                                        <stop offset="0" stopColor="#04BFDA" stopOpacity="0.27"/>
                                        <stop offset="0.729167" stopColor="white" stopOpacity="0.58"/>
                                    </linearGradient>
                                    <linearGradient id="strokeGradient" x1="286" y1="96.3595" x2="-76.7507" y2="12.5358" gradientUnits="userSpaceOnUse">
                                        <stop offset="0.025" stopColor="#04BFDA" stopOpacity="0.9841"/>
                                        <stop offset="1" stopColor="#04BFDA" stopOpacity="0.26"/>
                                    </linearGradient>
                                </defs>

                                {/* Líneas horizontales de la cuadrícula */}
                                {[...Array(6)].map((_, i) => (
                                    <line
                                        key={i}
                                        x1="0"
                                        y1={i * (97/5)}
                                        x2="287"
                                        y2={i * (97/5)}
                                        stroke="#E8E8E8"
                                        strokeWidth="1"
                                        opacity="1"
                                    />
                                ))}
                                
                                {/* Línea principal con gradiente */}
                                <path
                                    className="progress-line-path"
                                    d="M1 74.0323C11.6667 62.0661 21.7 38.1337 52.5 38.1337C69.5 38.1337 75.9649 56.5208 108 56.5208C139 56.5208 131.481 20.1844 158 20.1844C183.5 20.1844 196.5 41.636 219 41.636C250 41.636 241.5 10.553 284 0.921692"
                                    stroke="url(#strokeGradient)"
                                    strokeWidth="1.8"
                                    strokeLinecap="round"
                                    fill="none"
                                />
                                
                                {/* Área con relleno degradado */}
                                <path
                                    className="progress-path"
                                    d="M52.864 38.1337C21.8463 38.1337 11.742 62.0661 1 74.0323V96.3595H286V0.921692C243.2 10.553 251.76 41.636 220.541 41.636C197.882 41.636 184.79 20.1844 159.11 20.1844C132.403 20.1844 139.975 56.5208 108.756 56.5208C76.4946 56.5208 69.9841 38.1337 52.864 38.1337Z"
                                    fill="url(#progressGradient)"
                                    stroke="url(#strokeGradient)"
                                />

                                {/* Línea vertical del día seleccionado o actual */}
                                <line
                                    x1={selectedWorkoutDay !== null ? (selectedWorkoutDay * (287/7)) + (287/14) : todayX}
                                    y1="0"
                                    x2={selectedWorkoutDay !== null ? (selectedWorkoutDay * (287/7)) + (287/14) : todayX}
                                    y2="97"
                                    stroke="#04BFDA"
                                    strokeWidth="1"
                                    strokeDasharray="2,2"
                                    opacity="0.5"
                                />
                                
                                {/* Punto en la intersección */}
                                <circle
                                    cx={selectedWorkoutDay !== null ? (selectedWorkoutDay * (287/7)) + (287/14) : todayX}
                                    cy="95"
                                    r="2.5"
                                    fill="white"
                                    stroke="#04BFDA"
                                    strokeWidth="1.5"
                                />

                                {/* Horas en el eje X */}
                                {selectedWorkoutDay !== null && (
                                    <g>
                                        <rect
                                            x={selectedWorkoutDay !== null ? (selectedWorkoutDay * (287/7)) + (287/14) - 15 : todayX - 15}
                                            y="45"
                                            width="30"
                                            height="16"
                                            rx="3"
                                            fill="white"
                                            stroke="#04BFDA"
                                            strokeWidth="1"
                                        />
                                        <text
                                            x={selectedWorkoutDay !== null ? (selectedWorkoutDay * (287/7)) + (287/14) : todayX}
                                            y="56"
                                            textAnchor="middle"
                                            fill="#04BFDA"
                                            fontSize="10"
                                            fontWeight="500"
                                            letterSpacing="0.2"
                                        >
                                            {workoutData[selectedWorkoutDay].hours}h
                                        </text>
                                    </g>
                                )}
                            </svg>
                        </div>
                    </div>
                    <div className="days">
                        {workoutData.map((data, index) => (
                            <div
                                key={index}
                                className={`day-container ${todayName === data.day ? 'active' : ''} ${selectedWorkoutDay === index ? 'selected' : ''}`}
                                onClick={() => setSelectedWorkoutDay(selectedWorkoutDay === index ? null : index)}
                            >
                                <span className="day-name">{data.day}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </>
    );
};

export default FitnessDashboard;
