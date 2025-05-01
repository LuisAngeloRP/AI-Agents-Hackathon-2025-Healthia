import React, { useState } from 'react';
import '../../styles/Nutrition/NutritionChooseScreen.css';
import '../../styles/Nutrition/LocationModal.css';
import logoDashboard from "../../assets/logos/logo_sign.png";
import { useNavigate } from 'react-router-dom';
import mealsInfo from './mealsInfo';
import ModalScreen from '../Nutrition/Modal/ModalScreen';

const NutritionChooseScreen = ({ onBack }) => {
  const [isModalOpen, setModalOpen] = useState(false);
  const [location, setLocation] = useState('Lima, Peru');
  const [customLocation, setCustomLocation] = useState('');
  const [locationSuggestions, setLocationSuggestions] = useState([]);
  const [showInput, setShowInput] = useState(false);
  const [showSecondModal, setShowSecondModal] = useState(false);
  const [showErrorModal, setShowErrorModal] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [isFilterModalOpen, setIsFilterModalOpen] = useState(false);
  const [numberOfServings, setNumberOfServings] = useState(1);
  const [priceMin, setPriceMin] = useState(0);
  const [priceMax, setPriceMax] = useState(100);
  const [selectedCategory, setSelectedCategory] = useState('Lunch');
  const [showRestaurantModal, setShowRestaurantModal] = useState(false);
  // Estados para filtros adicionales
  const [selectedMeats, setSelectedMeats] = useState([]);
  const [selectedDietary, setSelectedDietary] = useState([]);
  const navigate = useNavigate();

  // Funciones para manejar checkboxes de "Type of Meat"
  const handleMeatCheckbox = (meat) => {
    setSelectedMeats(prev =>
      prev.includes(meat) ? prev.filter(m => m !== meat) : [...prev, meat]
    );
  };

  // Funciones para manejar checkboxes de "Dietary Preferences"
  const handleDietaryCheckbox = (pref) => {
    setSelectedDietary(prev =>
      prev.includes(pref) ? prev.filter(p => p !== pref) : [...prev, pref]
    );
  };

  // Filtrado combinado: categoría, búsqueda, filtros adicionales y precio
  const displayedMeals = mealsInfo.filter(meal => {
    if (meal.mealCategory !== selectedCategory) return false;
    if (searchText && !meal.name.toLowerCase().includes(searchText.toLowerCase()))
      return false;
    if (selectedMeats.length > 0) {
      const mealLower = meal.name.toLowerCase();
      const meatMatch = selectedMeats.some(meat => mealLower.includes(meat.toLowerCase()));
      if (!meatMatch) return false;
    }
    if (selectedDietary.includes("Vegetarian")) {
      const mealLower = meal.name.toLowerCase();
      if (
        mealLower.includes("chicken") ||
        mealLower.includes("beef") ||
        mealLower.includes("fish") ||
        mealLower.includes("shrimp")
      ) {
        return false;
      }
    }
    if (selectedDietary.includes("Vegan")) {
      const mealLower = meal.name.toLowerCase();
      if (
        mealLower.includes("chicken") ||
        mealLower.includes("beef") ||
        mealLower.includes("fish") ||
        mealLower.includes("shrimp") ||
        mealLower.includes("milk") ||
        mealLower.includes("cheese")
      ) {
        return false;
      }
    }
    const costPerPlate = meal.ingredients.reduce((acc, ing) => acc + ing.price, 0);
    if (costPerPlate < priceMin || costPerPlate > priceMax) return false;
    return true;
  });

  const handleLocationClick = () => {
    setModalOpen(true);
  };

  const handleCloseModal = () => {
    setModalOpen(false);
  };

  const handleLocationChange = (newLocation) => {
    setLocation(newLocation);
    setModalOpen(false);
  };

  const handleGetCurrentLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition((position) => {
        const { latitude, longitude } = position.coords;
        fetch(`https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${latitude}&longitude=${longitude}&localityLanguage=en`)
          .then(response => response.json())
          .then(data => {
            const city = data.city || '';
            const country = data.countryName || '';
            setCustomLocation(`${city}, ${country}`);
            setLocation(`${city}, ${country}`);
            setModalOpen(false);
          })
          .catch(error => {
            console.error('Error fetching location:', error);
            setShowErrorModal(true);
          });
      }, () => {
        setShowErrorModal(true);
      });
    } else {
      alert('Geolocation is not supported by this browser.');
    }
  };

  const handleCustomLocationChange = (e) => {
    setCustomLocation(e.target.value);
  };

  const handleNoClick = () => {
    setShowInput(false);
    setShowSecondModal(true);
  };

  const handleSearchChange = (e) => {
    setSearchText(e.target.value);
  };

  const handleCloseFilterModal = () => {
    setIsFilterModalOpen(false);
  };

  const handleFilterClick = () => {
    setIsFilterModalOpen(true);
  };

  const handleSaveFilters = () => {
    setIsFilterModalOpen(false);
  };

  const handleMealClick = (mealName) => {
    navigate(`/nutrition/${mealName}`);
  };

  // shop-button: recomienda el platillo del día en la categoría seleccionada
  const handleShopButtonClick = () => {
    const days = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
    const today = days[new Date().getDay()];
    const recommendedMeal = displayedMeals.find(meal => meal.day === today) || displayedMeals[0];
    if (recommendedMeal) {
      navigate(`/nutrition/${recommendedMeal.name}`);
    }
  };

  // Función para limpiar todos los filtros
  const handleClearFilters = () => {
    setSelectedMeats([]);
    setSelectedDietary([]);
    setNumberOfServings(1);
    setPriceMin(0);
    setPriceMax(100);
    setSearchText('');
  };

  return (
    <div className="plate-screen">
      <div className="plate-header">
        <div className="back-wrapper">
          <button className="conditions-back-button" onClick={() => navigate('/nutrition', { state: { id: 2 } })}>
            <svg xmlns="http://www.w3.org/2000/svg" width="17" height="15" viewBox="0 0 17 15" fill="none">
              <path d="M7.45408 13.6896L1.0805 7.47707L7.29305 1.10349M15.4646 7.29304L1.0805 7.47707L15.4646 7.29304Z" stroke="black" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </button>
          <div className="header-content">
            <img src={logoDashboard} alt="HealthIA" className="header-logo" />
            <div className="header-text-plate">
              <h1>HealthIA</h1>
              <p>Your personal health assistant</p>
            </div>
          </div>
        </div>
      </div>
      <div className="choose-plate-container">
        <div className="header-plate">
          <div className="location-container" onClick={handleLocationClick}>
            <div data-svg-wrapper>
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path fillRule="evenodd" clipRule="evenodd" d="M3.5 10.3178C3.5 5.71789 7.34388 2 11.9934 2C16.6561 2 20.5 5.71789 20.5 10.3178C20.5 12.6357 19.657 14.7876 18.2695 16.6116C16.7388 18.6235 14.8522 20.3765 12.7285 21.7524C12.2425 22.0704 11.8039 22.0944 11.2704 21.7524C9.13474 20.3765 7.24809 18.6235 5.7305 16.6116C4.34198 14.7876 3.5 12.6357 3.5 10.3178ZM9.19423 10.5768C9.19423 12.1177 10.4517 13.3297 11.9934 13.3297C13.5362 13.3297 14.8058 12.1177 14.8058 10.5768C14.8058 9.0478 13.5362 7.77683 11.9934 7.77683C10.4517 7.77683 9.19423 9.0478 9.19423 10.5768Z" fill="#0957DE"/>
              </svg>
            </div>
            <div className="location-text">{location}</div>
            <div data-svg-wrapper>
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12.6667 5.66663L8.00004 10.3333L3.33337 5.66663" stroke="#0957DE" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </div>
          </div>

          <div className="search-container">
            <div className="search-input">
              <div className="search-icon">
                <div style={{ width: 18.76, height: 19.22, position: 'relative' }}>
                  <div style={{ width: 17.98, height: 17.98, left: 0, top: 0, position: 'absolute', borderRadius: 9999, border: '1.50px #0D0D0D solid' }} />
                  <div data-svg-wrapper style={{ left: 14, top: 7.9, position: 'absolute' }}>
                    <svg width="6" height="6" viewBox="0 0 6 6" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M1.01831 1.48511L4.54234 4.99998" stroke="#0D0D0D" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                  </div>
                </div>
              </div>
              <input
                type="text"
                value={searchText}
                onChange={handleSearchChange}
                placeholder="Search"
                style={{ border: "none", outline: "none" }}
              />
            </div>
            <div data-svg-wrapper onClick={handleFilterClick}>
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="0.5" y="0.5" width="47" height="47" rx="9.5" fill="white" />
                <rect x="0.5" y="0.5" width="47" height="47" rx="9.5" stroke="#0957DE" />
                <path d="M22.3301 28.5928H16.0294" stroke="#0957DE" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                <path d="M25.1405 18.9004H31.4412" stroke="#0957DE" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                <path fillRule="evenodd" clipRule="evenodd" d="M20.7263 18.8463C20.7263 17.5506 19.6681 16.5 18.3631 16.5C17.0582 16.5 16 17.5506 16 18.8463C16 20.1419 17.0582 21.1925 18.3631 21.1925C19.6681 21.1925 20.7263 20.1419 20.7263 18.8463Z" stroke="#0957DE" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                <path fillRule="evenodd" clipRule="evenodd" d="M32 28.5538C32 27.2581 30.9426 26.2075 29.6376 26.2075C28.3318 26.2075 27.2737 27.2581 27.2737 28.5538C27.2737 29.8494 28.3318 30.9 29.6376 30.9C30.9426 30.9 32 29.8494 32 28.5538Z" stroke="#0957DE" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </div>
          </div>

          <div className="categories-title">Categories</div>
          <div className="categories-container">
            <div className="categories">
              {["Breakfast", "Lunch", "Snack", "Dinner"].map((cat) => (
                <div
                  key={cat}
                  className={`category-button ${selectedCategory === cat ? "active" : ""}`}
                  onClick={() => setSelectedCategory(cat)}
                >
                  <img className="category-image" src={require(`../../assets/images/plates/${cat.toLowerCase()}.png`)} alt={cat} />
                  <div className="category-text">{cat}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
        <div className="choose-plate-content">
          <div className="meals-carousel">
            {displayedMeals.map((meal, index) => {
              const cost = meal.ingredients.reduce((acc, ing) => acc + ing.price, 0);
              const intCost = Math.floor(cost);
              const budgetMin = intCost - 2;
              const budgetMax = intCost + 2;
              return (
                <div className="meal-card" key={index} onClick={() => handleMealClick(meal.name)}>
                  <img src={meal.image} alt={meal.name} className="meal-image" />
                  <h2 className="meal-name">{meal.name}</h2>
                  <p className="meal-info">{meal.day}</p>
                  <p className="highlight-kcal">{meal.energy} kcal</p>
                  <p className="meal-price">Budget: S/. {budgetMin} - S/. {budgetMax}</p>
                </div>
              );
            })}
          </div>
        </div>
      </div>
      <div className="plate-input-container">
        <button className="shop-button" onClick={handleShopButtonClick}>
          View <br />Recommendation
        </button>
      </div>
      {isModalOpen && (
        <ModalScreen onClose={handleCloseModal}>
          <h3>Share Your Location</h3>
          <p>This location will help us provide recommendations based on your country.</p>
          <button onClick={handleGetCurrentLocation}>Yes</button>
          <button onClick={handleNoClick}>No, I prefer to write it</button>
          {showInput && (
            <input
              type="text"
              value={customLocation}
              onChange={handleCustomLocationChange}
              placeholder="Enter your city, country"
              style={{ border: "none", outline: "none" }}
            />
          )}
          <div className="suggestions">
            {locationSuggestions.map((suggestion, index) => (
              <div key={index} className="suggestion-item" onClick={() => handleLocationChange(suggestion)}>
                {suggestion}
              </div>
            ))}
          </div>
        </ModalScreen>
      )}
      {showSecondModal && (
        <ModalScreen onClose={() => setShowSecondModal(false)}>
          <h3>Share Your Location</h3>
          <input
            type="text"
            value={customLocation}
            className="location-input"
            onChange={handleCustomLocationChange}
            placeholder="Enter your city, country"
            style={{
              border: "1px solid rgb(168, 168, 168)",
              padding: "10px 10px",
              fontFamily: "Inter",
              fontSize: 16,
              borderRadius: 10,
              outline: "none",
              width: "100%"
            }}
          />
          <button onClick={() => { handleLocationChange(customLocation); setShowSecondModal(false); }}>OK</button>
        </ModalScreen>
      )}
      {showErrorModal && (
        <ModalScreen onClose={() => setShowErrorModal(false)}>
          <h3>Permission Denied</h3>
          <p>HealthIA does not have the necessary permissions to access your current location.</p>
          <button onClick={() => setShowErrorModal(false)}>OK</button>
        </ModalScreen>
      )}
      {isFilterModalOpen && (
        <ModalScreen onClose={handleCloseFilterModal}>
          <div className="choose-filter-modal-header">
            <h3>Filter Options</h3>
          </div>
          <div className="filter-options" style={{ textAlign: 'left', marginLeft: '20px' }}>
            <h4 style={{ marginTop: '20px', marginBottom: '10px' }}>Type of Meat</h4>
            <label style={{ display: 'block' }}>
              <input 
                type="checkbox" 
                checked={selectedMeats.includes("Chicken")}
                onChange={() => handleMeatCheckbox("Chicken")} 
              /> Chicken
            </label>
            <label style={{ display: 'block' }}>
              <input 
                type="checkbox" 
                checked={selectedMeats.includes("Beef")}
                onChange={() => handleMeatCheckbox("Beef")} 
              /> Beef
            </label>
            <label style={{ display: 'block' }}>
              <input 
                type="checkbox" 
                checked={selectedMeats.includes("Fish")}
                onChange={() => handleMeatCheckbox("Fish")} 
              /> Fish
            </label>
            <h4 style={{ marginTop: '10px' }}>Number of Servings</h4>
            <input 
              type="range" 
              min="1" 
              max="10" 
              value={numberOfServings} 
              onChange={(e) => setNumberOfServings(Number(e.target.value))} 
            />
            <span>{numberOfServings} servings</span>
            <h4 style={{ marginTop: '10px' }}>Price Range (Min)</h4>
            <input 
              type="range" 
              min="0" 
              max="100" 
              value={priceMin} 
              onChange={(e) => setPriceMin(Number(e.target.value))} 
            />
            <span>S/. {priceMin}</span>
            <h4 style={{ marginTop: '10px' }}>Price Range (Max)</h4>
            <input 
              type="range" 
              min="0" 
              max="100" 
              value={priceMax} 
              onChange={(e) => setPriceMax(Number(e.target.value))} 
            />
            <span>S/. {priceMax}</span>
            <h4 style={{ marginTop: '10px', marginBottom: '10px' }}>Dietary Preferences</h4>
            <label style={{ display: 'block' }}>
              <input 
                type="checkbox" 
                checked={selectedDietary.includes("Vegetarian")}
                onChange={() => handleDietaryCheckbox("Vegetarian")} 
              /> Vegetarian
            </label>
            <label style={{ display: 'block' }}>
              <input 
                type="checkbox" 
                checked={selectedDietary.includes("Vegan")}
                onChange={() => handleDietaryCheckbox("Vegan")} 
              /> Vegan
            </label>
          </div>
          <div className="choose-filter-modal-footer" style={{ marginTop: '20px', textAlign: 'center' }}>
            <button style={{ backgroundColor: '#007BFF', color: 'white', marginRight: '10px' }} onClick={handleSaveFilters}>Save</button>
            <button style={{ backgroundColor: '#FF5733', color: 'white' }} onClick={handleCloseFilterModal}>Close</button>
          </div>
        </ModalScreen>
      )}
    </div>
  );
};

export default NutritionChooseScreen;
