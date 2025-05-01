import React, { useState } from "react";
import "../../styles/Nutrition/NutritionPlanScreen.css";
import "../../styles/AnalyzeStyle.css";
import mascotImage from "../../assets/logos/mascot.png";
import ActivitiesScreen from '../../components/ActivitiesScreen';
import { useNavigate } from "react-router-dom";

const NutritionPlanScreen = ({ onBack }) => {
    const navigate = useNavigate();
    const [showPreviousActivity, setPreviousActivity] = useState(false);

    if (showPreviousActivity) {
        return <ActivitiesScreen onBack={() => { setPreviousActivity(false); }} />;
    }

    return (

        <div className="home-screen">
            {/* Header con botón de retroceso */}
            <div className="analyze-header">
                <button className="conditions-back-button" onClick={() => { navigate('/', { state: { id: 2 } }) }}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="17" height="15" viewBox="0 0 17 15" fill="none">
                        <path d="M7.45408 13.6896L1.0805 7.47707L7.29305 1.10349M15.4646 7.29304L1.0805 7.47707L15.4646 7.29304Z" 
                        stroke="black" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                </button>
            </div>

            <div className="analyze-content">
                <div className="analyze-header-content">
                    <img src={mascotImage} alt="HealthIA Mascot" className="nutrition-mascot" />
                        <div className="nutrition-title-container">
                            <h1 className="analyze-title">HealthIA</h1>
                            <p className="analyze-description">
                            Personalized Weekly Nutrition Plan
                            </p>
                        </div>

                    {/* Información nutricional */}
                    <div className="nutrition-info-box">
                        <div className="nutrition-info">
                            <p>
                                <span className="info-label">Recommended daily calories: </span>
                                <span className="info-value">2,000 kcal</span>
                            </p>
                            <p>
                                <span className="info-label">Nutrients to reinforce: </span>
                                <span className="info-value">Fiber, Vitamin C</span>
                            </p>
                            <p>
                                <span className="info-label">Restrictions: </span>
                                <span className="info-value">Gluten-free, low sodium</span>
                            </p>
                        </div>
                    </div>
                </div>
            </div>

                {/* Botón de análisis */}
            <button className="nutrition-button" onClick={() => navigate('/nutrition', { state: { id: 1 } })}>
                View Nutrition<br />Plan
            </button>
        </div>
    );
};

export default NutritionPlanScreen; 