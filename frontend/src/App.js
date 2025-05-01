// App.js
import React, { useState, useEffect, Component } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation, useNavigate } from 'react-router-dom';

import FitnessDashboard from './components/FitnessDashboard';
import ActivitiesScreen from './components/ActivitiesScreen';
import LoginScreen from './components/LoginScreen';
import HomeScreen from './components/HomeScreen';
import SplashScreen from './components/SplashScreen';
import ProfileScreen from './components/ProfileScreen';
import DeviceScreen from './components/DeviceScreen';
import MealDetail from './components/Nutrition/MealDetail';
import NutritionChooseScreen from './components/Nutrition/NutritionChooseScreen';
import NutritionPlanScreen from './components/Nutrition/NutritionPlanScreen';
import MealInstructions from './components/Nutrition/MealInstructions';
//---------------------------------
// 1) Un ErrorBoundary para capturar errores en la UI
//---------------------------------
class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return <h1>Something went wrong.</h1>;
    }
    return this.props.children;  
  }
}

//---------------------------------
// 2) Componente hijo (AppContent) 
//    Aquí va la lógica que usa useNavigate y los estados de la app
//---------------------------------
const AppContent = () => {
  // useNavigate se puede usar aquí porque es un descendiente directo de <Router>
  const navigate = useNavigate();

  const [isLoading, setIsLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [currentScreen, setCurrentScreen] = useState('home');
  const [previousScreen, setPreviousScreen] = useState('activities');

  // Transición de pantallas manual
  const handleScreenChange = (screen) => {
    document.body.classList.add('screen-transition');

    setTimeout(() => {
      if (screen === 'device') {
        setPreviousScreen(currentScreen);
      }
      setCurrentScreen(screen);

      setTimeout(() => {
        document.body.classList.remove('screen-transition');
      }, 200);
    }, 200);
  };

  // Simulación del SplashScreen de 2 seg
  useEffect(() => {
    const timer = setTimeout(() => {
      setIsLoading(false);
      setCurrentScreen('home');
    }, 2000);

    return () => clearTimeout(timer);
  }, []);

  if (isLoading) {
    return <SplashScreen />;
  }

  const handleLoginSuccess = () => {
    console.log('Login successful');
    setIsAuthenticated(true);
  };

  // Ejemplo de función que navega programáticamente
  // usando navigate('/') con un 'state'
  const handleNavigate = (id) => {
    navigate('/', { state: { id } });
  };

  // Si currentScreen es 'device', devolvemos esa pantalla
  // antes de renderizar las rutas (manejo de vistas manual)
  if (currentScreen === 'device') {
    return (
      <DeviceScreen
        onBack={() => setCurrentScreen(previousScreen)}
        previousScreen={previousScreen}
      />
    );
  }

  // Si en la ruta principal se pasa location.state con id=2, mostramos ActivitiesScreen;
  // si no, FitnessDashboard
  const Fitness = () => {
    const location = useLocation();
    const id = location.state?.id;
    if (id === 2) {
      return <ActivitiesScreen />;
    }
    return <FitnessDashboard />;
  };

  const Nutrition = () => {
    const location = useLocation();
    const id = location.state?.id;
    if (id === 2) {
      return <NutritionPlanScreen />;
    }else if (id === 3) {
      return <DeviceScreen />;
    }
    return <NutritionChooseScreen />;
  };

  return (
    <div className="app">
      {currentScreen === 'login' && (
        <LoginScreen
          onLoginSuccess={() => handleScreenChange('health')}
          onBack={() => handleScreenChange('home')}
        />
      )}

      {currentScreen === 'health' && (
        <FitnessDashboard onNavigate={handleScreenChange} />
      )}

      {currentScreen === 'profile' && (
        <ProfileScreen onNavigate={setCurrentScreen} />
      )}

      {currentScreen === 'activities' && (
        <ActivitiesScreen onNavigate={handleScreenChange} />
      )}

      <Routes>
        <Route path="/home" element={<HomeScreen />} />
        <Route path="/login" element={<LoginScreen onLoginSuccess={handleLoginSuccess} />} />
        <Route path="/nutrition/:name/instructions" element={isAuthenticated ? <MealInstructions /> : <Navigate to="/home" />}/>
        <Route path="/nutrition/:name" element={isAuthenticated ? <MealDetail /> : <Navigate to="/home" />}/>
        <Route path="/nutrition" element={isAuthenticated ? <Nutrition /> : <Navigate to="/home" />}/>
        <Route path="/" element={isAuthenticated ? <Fitness /> : <Navigate to="/home" />}/>
      </Routes>
    </div>
  );
};

//---------------------------------
// 3) Componente principal: 
//    aquí se define <Router> + <ErrorBoundary> + AppContent
//---------------------------------
const App = () => {
  return (
    <Router>
      <ErrorBoundary>
        <AppContent />
      </ErrorBoundary>
    </Router>
  );
};

export default App;
