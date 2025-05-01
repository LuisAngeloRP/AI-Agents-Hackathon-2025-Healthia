import React, { useState } from "react";
import "../styles/ActivitiesScreen.css";
import "../styles/SharedContainer.css";
import logoDashboard from "../assets/logos/logo_dashboard.png";
import micIcon from "../assets/icons/mic.svg";
import cameraIcon from "../assets/icons/camera.svg";
import clipIcon from "../assets/icons/clip.svg";
import foodIcon from "../assets/icons/food.svg";
import exerciseIcon from "../assets/icons/exercise.svg";
import analyzeIcon from "../assets/icons/analyze.svg";
import personIcon from "../assets/icons/person.svg";
import bellIcon from "../assets/icons/bell.svg";
import playIcon from "../assets/icons/play.png";
import ChatScreen from './ChatScreen';
import NutritionPlanScreen from '../components/Nutrition/NutritionPlanScreen';
import AnalyzeScreen from './AnalyzeScreen';
import HealthConditionsScreen from './HealthConditionsScreen';
import ExerciseScreen from './ExerciseScreen';
import FitnessDashboard from './FitnessDashboard';
import ProfileScreen from './ProfileScreen';
import DeviceScreen from './DeviceScreen';
import { useNavigate } from 'react-router-dom';
import mealsInfo from '../components/Nutrition/mealsInfo';

const ActivitiesScreen = ({ onNavigate }) => {
    const navigate = useNavigate();
    const [showDevice, setShowDevice] = useState(false);
    const [showChat, setShowChat] = useState(false);
    const [showNutrition, setShowNutrition] = useState(false);
    const [showAnalyze, setShowAnalyze] = useState(false);
    const [showHealthConditions, setShowHealthConditions] = useState(false);
    const [showExercise, setShowExercise] = useState(false);
    const [showHealth, setShowHealth] = useState(false);
    const [showProfile, setShowProfile] = useState(false);

    const days = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
    const today = days[new Date().getDay()];

    const handleShopButtonClick = () => { 
        const days = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
        const today = days[new Date().getDay()];
        // Filtramos solo los Lunch
        const lunchMeals = mealsInfo.filter(meal => meal.mealCategory === "Lunch");
        // Buscamos el lunch del día actual, o el primero si no existe
        const recommendedMeal = lunchMeals.find(meal => meal.day === today) || lunchMeals[0];
        if (recommendedMeal) {
          navigate(`/nutrition/${recommendedMeal.name}`);
        }
    };

        // Filtrar solo los meals de la categoría Lunch
    const lunchMeals = mealsInfo.filter(meal => meal.mealCategory === "Lunch");

    // Buscar el meal del día actual o usar el primero si no se encuentra
    const recommendedMeal = lunchMeals.find(meal => meal.day === today) || lunchMeals[0];

    // Variable que contiene el nombre del meal del día para Lunch
    const mealNameOfTheDay = recommendedMeal ? recommendedMeal.name : "";


    if (showHealth) {
        return <FitnessDashboard onBack={() => setShowHealth(false)} />;
    }
    if (showDevice) {
        return <DeviceScreen onBack={() => setShowDevice(false)} previousScreen="activities"/>;
    }

    if (showProfile) {
        return <ProfileScreen 
            onNavigate={onNavigate}
            previousScreen="activities"
        />;
    }

    if (showChat) {
        return <ChatScreen onBack={() => setShowChat(false)} />;
    }

    if (showNutrition) {
        return <NutritionPlanScreen onBack={() => setShowNutrition(false)} />;
    }

    if (showAnalyze) {
        return <AnalyzeScreen onBack={() => setShowAnalyze(false)} />;
    }

    if (showHealthConditions) {
        return <HealthConditionsScreen onBack={() => setShowHealthConditions(false)} />;
    }

    if (showExercise) {
        return <ExerciseScreen onBack={() => setShowExercise(false)} />;
    }

    return (
        <>
            <div className="condition-screen" style={{backgroundColor: "rgba(221, 221, 221, 0.03)"}}>
                {/* 🔹 Barra de navegación (Siempre visible) */}
                <div className="activities-navbar" >
                    <span onClick={() => setShowHealth(true)}>My Health</span>
                    <span className="active-tab">Activities</span>
                    <span onClick={() => setShowProfile(true)}>Profile</span>
                </div>

                {/* 🔹 Contenedor con scroll habilitado */}
                <div className="activities-content">
                    {/* 🔹 Tarjeta de Bienvenida */}
                    <div className="activities-welcome">
                        <div className="dashboard-welcome-activities">
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
                        {/* 🔹 Notificaciones */}
                        <div className="notification-container">
                            <div>
                            <button className="notification">
                                <img src={bellIcon} alt="Alert" className="notification-icon green-icon" />
                                <span>Home office today: stand up and stretch hourly.</span>
                            </button>
                            </div>
                            <div>
                            <button className="notification" onClick={handleShopButtonClick}>
                                <img src={bellIcon} alt="Alert" className="notification-icon green-icon" />
                                <span>Today's menu: {mealNameOfTheDay}. See recipe.</span>
                            </button>
                            </div>
                        </div>
                    </div>

                    {/* 🔹 Sección de Ask HealthIA */}
                    <div className="ask-healthia" onClick={() => setShowChat(true)}>
                        <div className="ask-input-container">
                            <span className="ask-placeholder">Ask HealthIA!</span>
                            <div className="input-actions">
                                <img src={clipIcon} alt="Attach" className="action-icon" />
                                <img src={micIcon} alt="Voice" className="action-icon" />
                                <img src={cameraIcon} alt="Camera" className="action-icon" />
                            </div>
                        </div>
                    </div>

                    {/* 🔹 Grid de Actividades */}
                    <div className="activities-grid">
                        <div className="activity-card" onClick={() => navigate('/nutrition', { state: { id: 2 } })}>
                            <div className="activity-content">
                                <div className="activity-header">
                                    <h4>
                                        Eat healthy on a budget
                                        <span className="highlight-text"> HealthIA </span>
                                        will help you!
                                    </h4>
                                    
                                    <img src={foodIcon} alt="Food" className="activity-icon blue-icon" />
                                </div>
                            </div>
                        </div>

                        <div className="activity-card" onClick={() => setShowExercise(true)}>
                            <div className="activity-content">
                                <div className="activity-header">
                                    <h4>Exercise is life! Train with <span className="text-blue">HealthIA</span> Fitness</h4>
                                    <img src={exerciseIcon} alt="Exercise" className="activity-icon blue-icon" />
                                </div>
                            </div>
                        </div>

                        <div className="activity-card" onClick={() => setShowAnalyze(true)}>
                            <div className="activity-content">
                                <div className="activity-header">
                                    <h4>Analyze what's in your food with <span className="text-blue">HealthIA</span></h4>
                                    <img src={analyzeIcon} alt="Analyze" className="activity-icon blue-icon" />
                                </div>
                            </div>
                        </div>

                        <div className="activity-card" onClick={() => setShowHealthConditions(true)}>
                            <div className="activity-content">
                                <div className="activity-header">
                                    <h4>Personalize <span className="text-blue">HealthIA</span> for your needs</h4>
                                    <img src={personIcon} alt="Personalize" className="activity-icon blue-icon" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default ActivitiesScreen;
