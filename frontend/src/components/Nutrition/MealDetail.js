import React, { useState } from 'react';
import '../../styles/Nutrition/MealDetail.css';
import { useParams, useNavigate } from 'react-router-dom';
import mealsInfo from './mealsInfo';
import ModalRestaurant from './Modal/ModalScreen';

const MealDetail = () => {
  const navigate = useNavigate();
  const { name } = useParams();
  const meal = mealsInfo.find(m => m.name === name);

  // Estado para el número de porciones (serves)
  const [serves, setServes] = useState(1);
  // Estado para marcar/desmarcar favorito (inicializado desde meal.favorite)
  const [isFavorite, setIsFavorite] = useState(meal ? meal.favorite : false);
  // Estado para mostrar el modal de restaurantes aliados
  const [showRestaurantModal, setShowRestaurantModal] = useState(false);

  if (!meal) {
    return <div>Meal not found</div>;
  }

  // Costo por plato (suma de precios de ingredientes para 1 serve)
  const costPerPlate = meal.ingredients.reduce((acc, ing) => acc + ing.price, 0);
  const totalCost = costPerPlate * serves;

  // Incrementar/decrementar porciones
  const handleIncrease = () => setServes(prev => prev + 1);
  const handleDecrease = () => {
    if (serves > 1) setServes(prev => prev - 1);
  };

  // Alternar el estado de favorito
  const toggleFavorite = () => {
    setIsFavorite(prev => !prev);
  };

  // Compartir la receta en WhatsApp (se detiene la propagación para no navegar)
  const shareWhatsApp = (e) => {
    e.stopPropagation();
    const text = encodeURIComponent(
      `Te comparto esta receta: ${meal.name}.\n` +
      `¡Anímate a prepararla!\n\n` +
      `Ingredientes para ${serves} porciones:\n` +
      meal.ingredients.map(ing => {
        const total = ing.baseAmount * serves;
        const totalPrice = ing.price * serves;
        return `- ${total} ${ing.unit} de ${ing.name} (S/ ${totalPrice.toFixed(2)})`;
      }).join('\n')
    );
    window.open(`https://wa.me/?text=${text}`, '_blank');
  };

  return (
    <div className="meal-container">
      {/* Encabezado con botón de volver y botón "more" */}
      <div className="meal-screen-header">
        <button
          className="meal-back-button"
          onClick={() => navigate('/nutrition', { state: { id: 1 } })}
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="17" height="15" viewBox="0 0 17 15" fill="none">
            <path
              d="M7.45408 13.6896L1.0805 7.47707L7.29305 1.10349M15.4646 7.29304L1.0805 7.47707L15.4646 7.29304Z"
              stroke="black"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </button>
        <button className="profile-screen-more-button">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" strokeWidth="2" stroke="black">
            <circle cx="5" cy="12" r="1" />
            <circle cx="12" cy="12" r="1" />
            <circle cx="19" cy="12" r="1" />
          </svg>
        </button>
      </div>

      {/* Imagen principal */}
      <img src={meal.image} alt={meal.name} className="meal-screen-container" />

      {/* Contenedor blanco con la información */}
      <div className="meal-screen-menu">
        <div className="meal-items-container">
          {/* Cabecera central: share-button a la izquierda, texto centrado y favorite-button a la derecha */}
          <div className="detail-header">
            <button className="share-button" onClick={shareWhatsApp}>
              <svg xmlns="http://www.w3.org/2000/svg" width="25" height="25" viewBox="0 0 25 25" fill="none">
                <path d="M14.9902 1.69923C14.4287 1.94825 14.0625 2.50977 14.0625 3.12501V6.25001H8.59375C3.84766 6.25001 0 10.0977 0 14.8438C0 20.376 3.97949 22.8467 4.89258 23.3447C5.01465 23.4131 5.15137 23.4375 5.28809 23.4375C5.82031 23.4375 6.25 23.0029 6.25 22.4756C6.25 22.1094 6.04004 21.7725 5.77148 21.5234C5.3125 21.0889 4.6875 20.2344 4.6875 18.75C4.6875 16.1621 6.78711 14.0625 9.375 14.0625H14.0625V17.1875C14.0625 17.8027 14.4238 18.3643 14.9902 18.6133C15.5566 18.8623 16.2109 18.7598 16.6699 18.3496L24.4824 11.3184C24.8096 11.0205 25 10.6006 25 10.1563C25 9.71192 24.8145 9.292 24.4824 8.99415L16.6699 1.9629C16.2109 1.54786 15.5518 1.44532 14.9902 1.69923Z" fill="black"/>
              </svg>
            </button>
            <div className="detail-header-text">
              <h1 className="detail-meal-name">{meal.name}</h1>
              <p className="detail-meal-subtitle">{meal.mealType} / {meal.time}</p>
              <p className="detail-kcal">{meal.energy} kcal</p>
            </div>
            <button className="favorite-button" onClick={toggleFavorite}>
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill={isFavorite ? "#FFD700" : "none"} stroke="rgb(211, 181, 6)" strokeWidth="2">
                <path d="M12 .587l3.668 7.431 8.214 1.193-5.932 5.785 1.401 8.167L12 18.896l-7.351 3.867 1.401-8.167-5.932-5.785 8.214-1.193z" />
              </svg>
            </button>
          </div>

          {/* Estadísticas nutricionales (valores por plato) */}
          <div className="profile-screen-stats">
            <div className="detail-nutrition-item">
              <span>Energy</span>
              <span>{meal.energy} kcal</span>
            </div>
            <div className="detail-nutrition-item">
              <span>Protein</span>
              <span>{meal.protein} g</span>
            </div>
            <div className="detail-nutrition-item">
              <span>Carbs</span>
              <span>{meal.carbs} g</span>
            </div>
            <div className="detail-nutrition-item">
              <span>Fat</span>
              <span>{meal.fat} g</span>
            </div>
          </div>

          <br />

          {/* Sección de ingredientes con botones de incremento */}
          <div className="ingredients-header">
            <div className="ingredients-title">
              <strong>Ingredients</strong>
              <span className="serves-info">{serves} serves</span>
            </div>
            <div className="serves-buttons">
              <button className="minus-btn" onClick={handleDecrease}>−</button>
              <button className="plus-btn" onClick={handleIncrease}>+</button>
            </div>
          </div>

          {/* Lista de ingredientes */}
          <ul className="detail-ingredients-list">
            {meal.ingredients.map((ingredient, index) => {
              const totalAmount = ingredient.baseAmount * serves;
              const totalPrice = ingredient.price * serves;
              return (
                <li key={index}>
                  <span className="ingredient-name">{ingredient.name}</span>
                  <span className="ingredient-amount">
                    {totalAmount} {ingredient.unit} (S/ {totalPrice.toFixed(2)})
                  </span>
                </li>
              );
            })}
          </ul>

          {/* Resumen de costo */}
          <div className="cost-summary">
            <p>Cost per plate: S/ {costPerPlate.toFixed(2)}</p>
            {serves > 1 && (
              <p>Total cost ({serves} serve{serves > 1 ? 's' : ''}): S/ {totalCost.toFixed(2)}</p>
            )}
          </div>
        </div>
      </div>

      {/* Contenedor sticky para botones de acción */}
      <div className="sticky-detail-buttons">
        <div className="detail-buttons">
          <button 
            className="detail-start-cooking" 
            onClick={() => navigate(`/nutrition/${meal.name}/instructions`)}
          >
            Start cooking
          </button>
          <button className="detail-search" onClick={() => setShowRestaurantModal(true)}>
            Search in
          </button>
        </div>
      </div>

      {/* Modal para restaurantes aliados, renderizado vía React Portal */}
      {showRestaurantModal && (
        <ModalRestaurant onClose={() => setShowRestaurantModal(false)}>
          <h3>If you don't have time, check out our partner restaurants:</h3>
          <div className="restaurant-links">
            <a href="https://www.google.com/maps" target="_blank" rel="noopener noreferrer">
              <img src="https://seeklogo.com/images/N/new-google-maps-icon-logo-263A01C734-seeklogo.com.png" alt="Google Maps" className="restaurant-icon" />
            </a>
            <a href="https://www.metromaps.com" target="_blank" rel="noopener noreferrer">
              <img src="https://consumer.huawei.com/content/dam/huawei-cbg-site/common/mkt/mobileservices/petalmaps/img/img-0113/petalmapsicon.svg" alt="Metro Maps" className="restaurant-icon" />
            </a>
            <a href="https://www.rappi.com" target="_blank" rel="noopener noreferrer" style={{ borderRadius: '20%' }}>
              <img src="https://images.rappi.com/web/fav-icons_android-icon-192x192.png" alt="Rappi" className="restaurant-icon" />
            </a>
          </div>
          <button onClick={() => setShowRestaurantModal(false)}>Close</button>
        </ModalRestaurant>
      )}
    </div>
  );
};

export default MealDetail;
