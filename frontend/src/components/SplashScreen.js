import React from "react";
import "../styles/SplashScreen.css";
import logo from "../assets/logos/logo.png";

const SplashScreen = () => {
    return (
        <div className="splash-screen">
            <div className="splash-content">
                <img src={logo} alt="HealthIA Logo" className="splash-logo" />
                <h1 className="splash-title">HealthIA</h1>
            </div>
        </div>
    );
};

export default SplashScreen;
