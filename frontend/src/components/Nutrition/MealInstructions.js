import React, { useState, useRef } from 'react';
import '../../styles/Nutrition/MealInstructions.css';
import { useParams, useNavigate } from 'react-router-dom';
import mealsInfo from './mealsInfo';

const MealInstructions = () => {
  const navigate = useNavigate();
  const { name } = useParams();

  // Hook para controlar el paso actual (Step 1 a 4)
  const [currentStep, setCurrentStep] = useState(1);
  // Ref para el contenedor del video
  const videoContainerRef = useRef(null);

  // Buscamos el meal en función del nombre de la URL
  const meal = mealsInfo.find((m) => m.name === name);
  if (!meal) return <div>Meal not found</div>;

  // Si meal.videoLink está vacío, usamos el enlace embed por defecto
  const finalVideoLink =
    meal.videoLink && meal.videoLink.trim() !== ''
      ? meal.videoLink
      : 'https://www.youtube.com/embed/TetF9jIaFiE?si=pdsKopqAlB3a-cR8';

  // Funciones para obtener ingredientes e instrucciones del paso actual
  const getIngredientsForStep = (step) => meal[`ingredients_step${step}`] || [];
  const getInstructionsForStep = (step) => meal[`instructions_step${step}`] || '';

  // Array de steps para renderizar los botones circulares
  const steps = [1, 2, 3, 4];
  const handleStepClick = (stepNumber) => setCurrentStep(stepNumber);

  // Manejo del botón Next Step / Go Back
  const handleNextStep = () => {
    if (currentStep < 4) {
      setCurrentStep(currentStep + 1);
    } else {
      navigate(`/nutrition/${meal.name}`);
    }
  };

  // Manejo del botón para maximizar el video
  const handleMaximizeVideo = () => {
    if (videoContainerRef.current) {
      if (document.fullscreenElement) {
        document.exitFullscreen().catch((err) =>
          console.error("Error exiting fullscreen:", err)
        );
      } else {
        videoContainerRef.current
          .requestFullscreen()
          .catch((err) => console.error("Error entering fullscreen:", err));
      }
    }
  };

  return (
    <div className="meal-container">
      {/* Header con botón Back y botón para maximizar el video */}
      <div className="meal-screen-header">
        <button
          className="meal-back-button"
          onClick={() => navigate(`/nutrition/${meal.name}`)}
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="17"
            height="15"
            viewBox="0 0 17 15"
            fill="none"
          >
            <path
              d="M7.45408 13.6896L1.0805 7.47707L7.29305 1.10349M15.4646 7.29304L1.0805 7.47707L15.4646 7.29304Z"
              stroke="black"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </button>
        {/* Botón para maximizar el video */}
        <button
          className="maximize-video-button"
          onClick={handleMaximizeVideo}
        >
          <svg xmlns="http://www.w3.org/2000/svg" 
            width="17" 
            height="17"
            viewBox="0 0 512 512">
            <path d="M344 0L488 0c13.3 0 24 10.7 24 24l0 144c0 9.7-5.8 18.5-14.8 22.2s-19.3 1.7-26.2-5.2l-39-39-87 87c-9.4 
            9.4-24.6 9.4-33.9 0l-32-32c-9.4-9.4-9.4-24.6 0-33.9l87-87L327 41c-6.9-6.9-8.9-17.2-5.2-26.2S334.3 0 344 0zM168 
            512L24 512c-13.3 0-24-10.7-24-24L0 344c0-9.7 5.8-18.5 14.8-22.2s19.3-1.7 26.2 5.2l39 39 87-87c9.4-9.4 24.6-9.4 
            33.9 0l32 32c9.4 9.4 9.4 24.6 0 33.9l-87 87 39 39c6.9 6.9 8.9 17.2 5.2 26.2s-12.5 14.8-22.2 14.8z"
            />
          </svg>
        </button>
      </div>

      {/* Contenedor del video con referencia para fullscreen */}
      <img src={meal.image} alt={meal.name} className="meal-screen-container" />

      {/* Contenedor blanco con la información (pasos, ingredientes, instrucciones) */}
      <div className="meal-screen-menu">
        <div className="instructions-items-container">
          {/* Header con share y botón PLAY (abre el video en una nueva pestaña) */}
          <div className="detail-header">
            <button
              className="share-button"
              onClick={(e) => {
                e.stopPropagation();
                // Aquí puedes implementar compartir si lo deseas
              }}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="25"
                height="25"
                viewBox="0 0 25 25"
                fill="none"
              >
                <path
                  d="M14.9902 1.69923C14.4287 1.94825 14.0625 2.50977 14.0625 3.12501V6.25001H8.59375C3.84766 6.25001 0 10.0977 0 14.8438C0 20.376 3.97949 22.8467 4.89258 23.3447C5.01465 23.4131 5.15137 23.4375 5.28809 23.4375C5.82031 23.4375 6.25 23.0029 6.25 22.4756C6.25 22.1094 6.04004 21.7725 5.77148 21.5234C5.3125 21.0889 4.6875 20.2344 4.6875 18.75C4.6875 16.1621 6.78711 14.0625 9.375 14.0625H14.0625V17.1875C14.0625 17.8027 14.4238 18.3643 14.9902 18.6133C15.5566 18.8623 16.2109 18.7598 16.6699 18.3496L24.4824 11.3184C24.8096 11.0205 25 10.6006 25 10.1563C25 9.71192 24.8145 9.292 24.4824 8.99415L16.6699 1.9629C16.2109 1.54786 15.5518 1.44532 14.9902 1.69923Z"
                  fill="black"
                />
              </svg>
            </button>
            <div className="detail-header-text">
              <h1 className="detail-meal-name">{meal.name}</h1>
              <p className="detail-meal-subtitle">
                {meal.mealCategory} / {meal.time}
              </p>
            </div>
            <button
              className="favorite-button"
              onClick={() => window.open(finalVideoLink, '_blank')}
            >
              {/* Ícono de PLAY en amarillo (#FEC635) */}
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="25"
                height="25"
                viewBox="0 0 24 24"
                fill="black"
              >
                <path d="M3 22v-20l18 10-18 10z" />
              </svg>
            </button>
          </div>

          {/* Step indicator: muestra el step actual y botones circulares */}
          <div className="step-indicator">
            <h3>Step {currentStep}</h3>
            <div>
              {steps.map((step) => (
                <div
                  key={step}
                  onClick={() => handleStepClick(step)}
                  style={{
                    width: '24px',
                    height: '24px',
                    borderRadius: '50%',
                    backgroundColor: currentStep === step ? '#FEC635' : '#ccc',
                    cursor: 'pointer',
                    display: 'inline-flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '12px',
                    color: '#000',
                    margin: '0 4px',
                  }}
                >
                  {step}
                </div>
              ))}
            </div>
          </div>

          {/* Ingredientes del step actual */}
          <div className="instructions-screen-stats">
            {getIngredientsForStep(currentStep).map((ing, index) => (
              <div className="instructions-nutrition-item" key={index}>
                <span>{ing.name}</span>
                <span>
                  {ing.baseAmount} {ing.unit}
                </span>
              </div>
            ))}
          </div>

          {/* Instrucciones del step actual */}
          <div className="instructions-section">
            <h2>Instructions</h2>
            <p>{getInstructionsForStep(currentStep)}</p>
          </div>
        </div>
      </div>

      {/* Botón sticky para "Next Step" o "Go Back" */}
      <div className="sticky-detail-buttons">
        <div className="detail-buttons">
          <button className="detail-start-cooking" onClick={handleNextStep}>
            {currentStep < 4 ? 'Next Step' : 'Go Back'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default MealInstructions;
