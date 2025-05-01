import React from "react";
import "../styles/HomeScreen.css";
import logo from "../assets/logos/logo_blue.png"; // Ahora usamos logo_blue.png
import background from "../assets/background/background.svg"; // Fondo degradado superior
import { useNavigate } from 'react-router-dom';

const HomeScreen = ({ onNavigate }) => {
    const navigate = useNavigate();

    const handleNavigate = () => {
        navigate('/login');
    };

    return (
        <div className="home-screen">
            <img src={background} alt="Background" className="background" />

            <div className="home-content">
                <img src={logo} alt="HealthIA Logo" className="home-logo" />
                <h1 className="home-title">HealthIA</h1>
                <p className="home-subtitle">Your personal health assistant</p>
            </div>

            {/* Botón de acción */}
            <button className="home-button" onClick={handleNavigate}>Let’s do it</button>
        </div>
    );
};

export default HomeScreen;
