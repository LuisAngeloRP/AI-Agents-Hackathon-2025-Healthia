import React from 'react';
import DevicePopup from './DevicePopup';
import FitnessDashboard from './FitnessDashboard';
import ActivitiesScreen from './ActivitiesScreen';

const DeviceScreen = ({ onBack, previousScreen = 'activities' }) => {
    return (
        <div className="device-screen">
            {/* Pantalla de fondo */}
            <div className="background-screen">
                {previousScreen === 'activities' ? (
                    <ActivitiesScreen onNavigate={() => {}} />
                ) : (
                    <FitnessDashboard onNavigate={() => {}} />
                )}
            </div>

            {/* Popup superpuesto */}
            <DevicePopup 
                isOpen={true} 
                onClose={onBack}
            />
        </div>
    );
};

export default DeviceScreen; 