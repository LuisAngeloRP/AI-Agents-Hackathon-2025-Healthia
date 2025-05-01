// src/components/ExercisesChart.jsx
import React, { useState, useEffect } from 'react';
import '../styles/ExerciseScreen.css'; // Ajusta según tu estructura de estilos
import '../styles/ExercisesCharts.css';

const ExercisesChart = () => {
  const [showNumber, setShowNumber] = useState(false);
  const [currentSteps, setCurrentSteps] = useState(null);

  const handleBarClick = (steps) => {
    setCurrentSteps(steps);
    setShowNumber(true);
  };

  const handleClickOutside = (event) => {
    if (showNumber) {
      setShowNumber(false);
      setCurrentSteps(null);
    }
  };

  useEffect(() => {
    document.addEventListener('click', handleClickOutside);
    return () => {
      document.removeEventListener('click', handleClickOutside);
    };
  }, [showNumber]);

  console.log("Componente ExercisesChart renderizado");

  // Datos de ejemplo para las barras de Steps por mes
  const stepsByMonth = [
    { month: 'Jan', steps: 140 },
    { month: 'Feb', steps: 165 },
    { month: 'Mar', steps: 170 },
    { month: 'Apr', steps: 155 },
  ];

  console.log("Datos de pasos por mes:", stepsByMonth);

  // Datos de ejemplo para la lista de días
  const dailyProgress = [
    { day: 'Thu', number: 14, steps: 3679, duration: '1hr40m' },
    { day: 'Wed', number: 20, steps: 5789, duration: '1hr20m' },
    { day: 'Sat', number: 22, steps: 1859, duration: '1hr10m' },
  ];

  console.log("Progreso diario:", dailyProgress);

  // Para la fecha actual (ejemplo: "January 12th")
  const today = new Date();
  const monthName = today.toLocaleString('en-US', { month: 'long' });
  const dayNumber = today.getDate();
  const suffix = 
    dayNumber === 1 ? 'st' : 
    dayNumber === 2 ? 'nd' : 
    dayNumber === 3 ? 'rd' : 'th';

  console.log(`Fecha actual: ${monthName} ${dayNumber}${suffix}`);

  return (
    <div className="charts-container">
      <h2 className="charts-title">My Progress</h2>
      <p className="charts-date">{`${monthName} ${dayNumber}${suffix}`}</p>

      <div className="charts-graph-box-container-new">
          <div className='charts-graph-box-container-up'>
              <div className='charts-title-container'>Steps</div>
              <div className='charts-graph-box-container-down'>
                  <div className='charts-number-container'>
                      <div className="charts-number">170</div>
                      <div className="charts-number">165</div>
                      <div className="charts-number">155</div>
                      <div className="charts-number">150</div>
                  </div>
                  <div className="charts-graph-box-container-graph">
                      <div style={{justifyContent: 'center', alignItems: 'flex-end', gap: 44, display: 'flex'}}>
                        {[50, 75, 30, 90].map((steps, index) => (
                            <div className='charts-blue' key={index} onClick={() => handleBarClick(steps)}>
                              <div style={{width: 16, height: 147, position: 'relative'}}>
                                  <div style={{width: 16, height: 147, left: 0, top: 0, position: 'absolute', background: '#D9D9D9', borderRadius: 100}} />
                                  <div style={{width: 16, height: 147 - steps, left: 0, top: steps, position: 'absolute', background: '#55B0FF'}} />
                                  {showNumber && (
                                    <div style={{
                                      position: 'absolute',
                                      top: -20,
                                      left: -10,
                                      background: '#55B0FF',
                                      color: 'white',
                                      padding: '5px',
                                      borderRadius: '5px',
                                      fontSize: '12px',
                                    }}>{currentSteps} * 3.3</div>
                                  )}
                              </div>
                            </div>
                          ))}
                      </div>
                      <div dat-svg-wrapper>
                        <svg width="220" height="2" viewBox="0 0 282 2" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <path d="M0 1H282" stroke="#30323B"/>
                        </svg>
                      </div>
                      <div className="chart-month-container">
                          <div className="charts-number">Jan</div>
                          <div className="charts-number">Feb</div>
                          <div className="charts-number">Mar</div>
                          <div className="charts-number">Apr</div>
                      </div>
                  </div>
              </div>
          </div>
      </div>

      <div className="charts-daily-list">
        {dailyProgress.map((dp, index) => (
          <div key={index} className="charts-day-row">
            <div className="charts-day-container">
                <div className="charts-day-col">{dp.day}</div>
                <div className="charts-day">{dp.number}</div>
            </div>
            <div className="stat-divider-day"></div>
            <div className="charts-day-container">
                <div className="charts-day-col">Steps</div>
                <div className="charts-day">{dp.steps.toLocaleString()}</div>
            </div>
            <div className="charts-day-container">
                <div className="charts-day-col">Duration</div>
                <div className="charts-day-time">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="21" viewBox="0 0 20 21" fill="none">
                    <path d="M10 20.5C12.6519 20.4992 15.195 19.4452 17.0702 17.5697C18.9453 15.6942 19.9992 13.1508 20 10.4984C19.9992 7.84642 18.9452 5.30325 17.0699 3.42828C15.1946 1.55331 12.6516 0.5 10 0.5C7.34838 0.5 4.80528 1.55331 2.93 3.42828C1.05473 5.30325 0.000842854 7.84642 0 10.4984C0.000842545 13.1508 1.05466 15.6942 2.92985 17.5697C4.80503 19.4452 7.34809 20.4992 10 20.5ZM5.66747 13.064L9.36427 10.2027V6.18116C9.36427 6.01252 9.43122 5.85077 9.55044 5.73153C9.66966 5.61229 9.83139 5.54533 10 5.54533C10.1686 5.54533 10.3303 5.61229 10.4495 5.73153C10.5687 5.85077 10.6357 6.01252 10.6357 6.18116V10.5143C10.6357 10.5143 10.6357 10.5398 10.6357 10.5525C10.6357 10.5652 10.6357 10.597 10.6357 10.6192C10.6319 10.6387 10.6266 10.6578 10.6198 10.6764C10.6216 10.6965 10.6216 10.7168 10.6198 10.7369L10.5944 10.7941L10.5658 10.8481L10.5308 10.899C10.5187 10.9153 10.5049 10.9302 10.4895 10.9435C10.4753 10.9609 10.4592 10.9769 10.4418 10.9912L10.4163 11.0166L6.47167 14.0845C6.33875 14.1874 6.17052 14.2334 6.00372 14.2126C5.83693 14.1917 5.68515 14.1057 5.58164 13.9732C5.52631 13.9085 5.48457 13.8332 5.45887 13.752C5.43317 13.6708 5.42409 13.5852 5.4321 13.5004C5.4401 13.4156 5.46507 13.3333 5.50551 13.2583C5.54595 13.1833 5.601 13.1172 5.66747 13.064Z" fill="#55B0FF"/>
                  </svg>
                  <div className="charts-day-timer">{dp.duration}</div>
                </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ExercisesChart;
