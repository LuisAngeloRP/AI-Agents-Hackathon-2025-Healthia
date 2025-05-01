import React, { useState } from 'react';
import '../styles/ExerciseScreen.css';
import avatarFransua from '../assets/images/avatar_fransua.jpeg';
import logoDashboard from "../assets/logos/logo_sign.png";
import ExercisesCharts from './ExercisesCharts';
import runIcon from '../assets/icons/run.svg';
import PrincipalExercise from '../assets/images/principal_exercise.png';
import FirstExercise from '../assets/images/first_exercise.png';
import SecondExercise from '../assets/images/second_exercise.png';
import ThirdExercise from '../assets/images/third_exercise.png';

const clockIcon = (
  <div data-svg-wrapper style={{ position: 'relative' }}>
    <svg width="9" height="9" viewBox="0 0 9 9" fill="none">
      <path d="M4.5 9C5.69336 8.99962 6.83774 8.52533 7.68157 7.68136C8.5254 6.8374 8.99962 5.69285 9 4.4993C8.99962 3.30589 8.52534 2.16146 7.68146 1.31773C6.83759 0.473991 5.69323 0 4.5 0C3.30677 0 2.16237 0.473991 1.3185 1.31773C0.474627 2.16146 0.000379284 3.30589 0 4.4993C0.000379145 5.69285 0.474598 6.8374 1.31843 7.68136C2.16226 8.52533 3.30664 8.99962 4.5 9ZM2.55036 5.65378L4.21392 4.36623V2.55652C4.21392 2.48064 4.24405 2.40785 4.2977 2.35419C4.35135 2.30053 4.42413 2.2704 4.5 2.2704C4.57587 2.2704 4.64862 2.30053 4.70227 2.35419C4.75592 2.40785 4.78608 2.48064 4.78608 2.55652V4.50643C4.78608 4.50643 4.78608 4.51789 4.78608 4.52361C4.78608 4.52933 4.78608 4.54363 4.78608 4.55365C4.78436 4.5624 4.78196 4.571 4.77892 4.57939C4.77974 4.58843 4.77974 4.59756 4.77892 4.6066L4.76746 4.63234L4.75461 4.65665L4.73886 4.67956C4.73343 4.68691 4.72721 4.69361 4.72028 4.69957C4.71387 4.70743 4.70666 4.7146 4.69881 4.72102L4.68735 4.73247L2.91225 6.11304C2.85244 6.15934 2.77673 6.18005 2.70168 6.17067C2.62662 6.16128 2.55832 6.12256 2.51174 6.06295C2.48684 6.03381 2.46805 5.99996 2.45649 5.96341C2.44493 5.92686 2.44084 5.88834 2.44444 5.85017C2.44805 5.81201 2.45928 5.77498 2.47748 5.74124C2.49568 5.7075 2.52045 5.67775 2.55036 5.65378Z" fill="#232323"/>
    </svg>
  </div>
);

const fireIcon = (
  <div data-svg-wrapper style={{ position: 'relative' }}>
    <svg width="7" height="9" viewBox="0 0 7 9" fill="none">
      <path d="M1.56995 2.2901C1.96297 1.73694 2.3339 1.23356 2.4581 0.614014C2.4581 0.614014 2.4581 0.614014 2.4581 0.601106C2.3306 0.456209 2.18447 0.332004 2.02424 0.232329C2.0087 0.220701 1.9906 0.213657 1.97178 0.21189C1.95296 0.210122 1.93403 0.213692 1.91687 0.222248C1.89971 0.230804 1.88493 0.244053 1.87392 0.260691C1.86291 0.27733 1.85608 0.296786 1.85411 0.317149C1.83572 0.434091 1.81127 0.549822 1.78092 0.663798C1.72769 0.85291 1.64483 1.03068 1.53593 1.18931C1.33177 1.48617 1.07997 1.75353 0.855392 2.03565C0.321166 2.70867 -0.0888501 3.54026 0.0166341 4.45483C0.0477602 4.72803 0.125671 4.99255 0.246334 5.23479C0.376535 4.38087 0.690697 3.57226 1.16335 2.87462C1.29265 2.67548 1.43385 2.48002 1.56995 2.2901Z" fill="#232323"/>
      <path d="M6.32867 2.622C6.32425 2.62045 6.31947 2.62045 6.31505 2.622C6.31608 2.74643 6.30177 2.87046 6.27251 2.99078C6.23648 3.10528 6.1785 3.21022 6.10238 3.29871C6.03881 3.37874 5.96064 3.44365 5.8728 3.48934C5.78496 3.53504 5.68935 3.56053 5.59197 3.56423C5.43946 3.56252 5.29243 3.50243 5.17685 3.39459C4.82467 3.07744 4.7549 2.53534 4.65282 2.08175C4.54876 1.62495 4.34856 1.20047 4.06785 0.84155C3.78714 0.482627 3.43356 0.199008 3.03482 0.0129073C3.01833 0.00445929 3.00031 5.25368e-05 2.98207 1.3476e-07C2.94893 -4.96527e-05 2.91704 0.0136978 2.89297 0.0384036C2.86891 0.0631094 2.85451 0.0968869 2.85277 0.13276C2.83972 0.322153 2.81645 0.510553 2.78303 0.696988C2.59588 1.63737 1.93236 2.33251 1.43556 3.09404C0.867311 3.96988 0.46918 5.03934 0.571261 6.12723C0.697162 7.47142 1.57506 8.45421 2.64861 8.89305C2.44901 8.73619 2.28229 8.53532 2.15943 8.30365C2.03657 8.07198 1.96034 7.81474 1.93575 7.54886C1.9035 6.90866 2.07927 6.27644 2.43254 5.76214C2.71496 5.31407 3.09778 4.90473 3.20666 4.35157C3.22615 4.24194 3.23922 4.13108 3.24579 4.01967C3.24611 4.00631 3.24949 3.99324 3.25559 3.98163C3.26169 3.97002 3.27033 3.96022 3.28076 3.9531C3.2912 3.94599 3.30311 3.94178 3.31541 3.94084C3.32771 3.93991 3.34004 3.94229 3.35129 3.94776C3.58115 4.06063 3.78397 4.22923 3.94415 4.44056C4.10434 4.65189 4.21758 4.90028 4.27512 5.16656C4.33297 5.43208 4.3738 5.75292 4.57626 5.93915C4.85529 6.19545 5.22445 5.92071 5.22615 5.55009V5.51137L5.24148 5.4874C5.27926 5.55239 5.31282 5.62016 5.34187 5.69023V5.69945C5.51072 6.088 5.60597 6.50928 5.62204 6.93841C5.63811 7.36754 5.57469 7.79581 5.43546 8.19791C5.37015 8.3715 5.28063 8.53318 5.17004 8.67732C5.07477 8.80004 4.9649 8.90854 4.84335 9C5.37289 8.83038 5.84568 8.49796 6.20443 8.04302C6.39672 7.79863 6.55339 7.52378 6.66894 7.22803C6.77115 6.96452 6.84976 6.69099 6.90371 6.41119C6.95785 6.12826 6.98917 5.84075 6.99729 5.55194C7.00605 5.26122 6.99354 4.9702 6.95987 4.68162C6.92691 4.39423 6.87341 4.11003 6.79991 3.83159C6.72815 3.55721 6.63599 3.28958 6.52431 3.03135C6.49369 2.96312 6.46135 2.89121 6.42902 2.82299L6.37627 2.7142C6.36681 2.68006 6.35059 2.64858 6.32867 2.622Z" fill="#232323"/>
    </svg>
  </div>
);

const runIconSvg = (
  <div data-svg-wrapper style={{ position: 'relative' }}>
    <svg width="7" height="8" viewBox="0 0 7 8" fill="none">
      <path d="M7 4.42046C7 4.57115 6.94083 4.71567 6.83552 4.82223C6.73022 4.92878 6.5874 4.98864 6.43847 4.98864C6.40883 4.98791 6.37929 4.98507 6.35005 4.98012H6.32194C5.85611 4.89825 5.42312 4.68336 5.07396 4.36079L5.60039 3.24147C5.70227 3.4057 5.83763 3.54605 5.99741 3.65309C6.15719 3.76014 6.33773 3.83143 6.52693 3.86221C6.65933 3.8835 6.77982 3.95204 6.86664 4.05541C6.95219 4.15741 6.99941 4.28668 7 4.42046Z" fill="#232323"/>
      <path d="M3.01176 5.83097C2.86332 5.94792 2.70166 6.04662 2.53022 6.125C2.16441 6.28719 1.76865 6.36856 1.36929 6.36365C1.03667 6.36461 0.705665 6.31676 0.386613 6.2216C0.246237 6.17504 0.129648 6.07437 0.0621154 5.94143C-0.0054171 5.80848 -0.0184619 5.65397 0.0258203 5.51137C0.055698 5.41884 0.108568 5.33563 0.179364 5.26971C0.25016 5.2038 0.336491 5.15739 0.430105 5.13495C0.671563 5.07671 0.904606 5.20171 1.14045 5.2287C1.39297 5.25706 1.64856 5.23289 1.89151 5.15768C2.04406 5.11244 2.18689 5.03873 2.31266 4.94035C2.34687 5.053 2.39398 5.16122 2.45304 5.26279C2.592 5.49443 2.78353 5.68921 3.01176 5.83097Z" fill="#232323"/>
      <path d="M6.79784 7.78693C6.79784 7.84344 6.77564 7.89764 6.73615 7.93759C6.69666 7.97755 6.64312 8 6.58727 8H2.09503C2.03918 8 1.9856 7.97755 1.94611 7.93759C1.90662 7.89764 1.88446 7.84344 1.88446 7.78693C1.88446 7.73042 1.90662 7.67624 1.94611 7.63629C1.9856 7.59633 2.03918 7.57386 2.09503 7.57386H4.90268C4.93497 7.57386 4.9462 7.56535 4.9434 7.52842C4.93352 7.31559 4.91102 7.10357 4.87601 6.89348C4.82962 6.6461 4.74025 6.409 4.61208 6.19319C4.5411 6.07665 4.45518 5.97014 4.35657 5.87643C4.32428 5.8466 4.28919 5.81677 4.25269 5.78694C4.11788 5.68029 3.96526 5.59899 3.80207 5.54689C3.45252 5.42757 3.13807 5.31393 2.93873 4.97871C2.86548 4.85448 2.81877 4.71609 2.8016 4.57248C2.78444 4.42886 2.7972 4.28322 2.83906 4.1449C2.88679 4.0199 2.95839 3.89773 3.01595 3.77699L3.48621 2.77131C3.49884 2.74433 3.57326 2.52557 3.60414 2.51847C3.48916 2.5409 3.37606 2.57224 3.26583 2.61222C2.87951 2.75922 2.52555 2.98172 2.22417 3.26705C2.16942 3.31705 2.10547 3.35565 2.03598 3.38064C1.96648 3.40563 1.89282 3.41651 1.81917 3.41269C1.74551 3.40886 1.67331 3.3904 1.60671 3.35834C1.54011 3.32629 1.48043 3.28126 1.43102 3.22585C1.38161 3.17045 1.34344 3.10576 1.31874 3.03545C1.29405 2.96513 1.2833 2.89058 1.28708 2.81605C1.29086 2.74152 1.3091 2.66848 1.34078 2.60109C1.37246 2.5337 1.41698 2.4733 1.47174 2.42331C1.88155 2.03909 2.3618 1.73988 2.88537 1.54262C3.25704 1.40568 3.64938 1.33504 4.04493 1.33381C4.24335 1.33345 4.44131 1.35296 4.63593 1.39206C4.68784 1.39592 4.73892 1.4074 4.78756 1.42615C4.85363 1.43598 4.91826 1.45411 4.9799 1.48013C5.18596 1.56735 5.34935 1.73383 5.43412 1.94293C5.51889 2.15204 5.5181 2.38666 5.43192 2.59518L5.3898 2.69745L4.48715 4.61648C4.5405 4.64205 4.59523 4.66903 4.64717 4.69744C4.69911 4.72585 4.78755 4.7841 4.85213 4.83097C5.05619 4.97741 5.23866 5.15246 5.39402 5.35087C5.70811 5.76968 5.915 6.26056 5.99626 6.77983C6.05181 7.04352 6.06414 7.31466 6.03276 7.58239C6.21667 7.58239 6.40056 7.58239 6.58446 7.58239C6.63936 7.58161 6.69238 7.60255 6.73224 7.64076C6.7721 7.67897 6.79565 7.73142 6.79784 7.78693Z" fill="#232323"/>
      <path d="M1.07306 7.57386H0.56626C0.510412 7.57386 0.456868 7.59632 0.417377 7.63628C0.377887 7.67624 0.355686 7.73042 0.355686 7.78693C0.355686 7.84344 0.377887 7.89763 0.417377 7.93759C0.456868 7.97755 0.510412 7.99999 0.56626 7.99999H1.07306C1.1289 7.99999 1.18245 7.97755 1.22194 7.93759C1.26143 7.89763 1.28363 7.84344 1.28363 7.78693C1.28363 7.73042 1.26143 7.67624 1.22194 7.63628C1.18245 7.59632 1.1289 7.57386 1.07306 7.57386Z" fill="#232323"/>
      <path d="M5.49089 1.35511C5.86072 1.35511 6.16052 1.05176 6.16052 0.677557C6.16052 0.303353 5.86072 0 5.49089 0C5.12107 0 4.82127 0.303353 4.82127 0.677557C4.82127 1.05176 5.12107 1.35511 5.49089 1.35511Z" fill="#232323"/>
    </svg>
  </div>
);
// Dropdown icon for month/year selection
const DropdownIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="10" height="7" viewBox="0 0 10 7" fill="none">
    <path
      d="M9.23529 1.00115L5.11765 5.70703L1 1.00115"
      stroke="#B3A0FF"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
);

// Duration Icon (example)
const DurationIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="none">
    <path
      d="M8 16C10.1215 15.9993 12.156 15.1561 13.6561 13.6558C15.1563 12.1554 15.9993 10.1206 16 7.99876C15.9993 5.87714 15.1562 3.8426 13.6559 2.34263C12.1557 0.842651 10.1213 0 8 0C5.87871 0 3.84422 0.842651 2.344 2.34263C0.843781 3.8426 0 5.87714 0 7.99876C0 10.1206 0.84373 12.1554 2.34388 13.6558C3.84402 15.1561 5.87847 15.9993 8 16ZM4.53397 10.0512L7.49142 7.76219V4.54492C7.49142 4.41002 7.54498 4.28061 7.64035 4.18522C7.73573 4.08983 7.86512 4.03626 8 4.03626C8.13488 4.03626 8.26421 4.08983 8.35958 4.18522C8.45496 4.28061 8.50858 4.41002 8.50858 4.54492V8.01143C8.50858 8.01143 8.50858 8.0318 8.50858 8.04198C8.50858 8.05215 8.50858 8.07757 8.50858 8.09537C8.50553 8.11094 8.50127 8.12623 8.49586 8.14114C8.49732 8.15721 8.49732 8.17343 8.49586 8.18951L8.47549 8.23527L8.45265 8.27849L8.42465 8.31922C8.41498 8.33228 8.40393 8.34419 8.39162 8.3548C8.38021 8.36876 8.36739 8.38151 8.35344 8.39292L8.33307 8.41329L5.17734 10.8676C5.071 10.9499 4.93641 10.9868 4.80298 10.9701C4.66955 10.9534 4.54812 10.8845 4.46531 10.7786C4.42105 10.7268 4.38765 10.6666 4.3671 10.6016C4.34654 10.5366 4.33927 10.4682 4.34568 10.4003C4.35208 10.3325 4.37206 10.2666 4.40441 10.2066C4.43676 10.1467 4.4808 10.0938 4.53397 10.0512Z"
      fill="#55B0FF"
    />
  </svg>
);

// Componente para cada tarjeta de recomendación
function RecommendationCard({ title, image, duration, calories, exercises }) {
  return (
    <div className="recommended-card" style={{
      display: 'flex',
      alignItems: 'center',
      background: '#F5F7FA',
      borderRadius: '12px',
      padding: '16px',
      marginBottom: '10px',
      justifyContent: 'space-between',
      position: 'relative',
      width: '100%'
    }}>
      {/* Contenedor izquierdo: texto */}
      <div className="recommended-card-text" style={{ flex: '1', paddingRight: '10px' }}>
        <h3 style={{ margin: '0 0 8px 0', fontSize: '18px' }}>{title}</h3>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
          {/* Reloj + Duración */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            {clockIcon}
            <span>{duration}</span>
          </div>
          <span>|</span>
          {/* Fuego + Kcal */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            {fireIcon}
            <span>{calories}</span>
          </div>
          <span>|</span>
          {/* Correr + Ejercicios */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            {runIconSvg}
            <span>{exercises} Exercises</span>
          </div>
        </div>
      </div>

      {/* Contenedor derecho: imagen */}
      <div className="recommended-card-image-wrapper" style={{
        position: 'relative',
        width: '180px',
        height: '100px',
        borderRadius: '8px',
        overflow: 'hidden',
        flexShrink: 0
      }}>
        <img
          src={image}
          alt={title}
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover'
          }}
        />
        {/* Estrella en la esquina superior derecha (fija) */}
        <div style={{
          position: 'absolute',
          top: '8px',
          right: '8px',
          width: '20px',
          height: '20px'
        }}>
          <svg
            viewBox="0 0 24 24"
            fill="rgb(206, 186, 3)"
            xmlns="http://www.w3.org/2000/svg"
            style={{ width: '100%', height: '100%' }}
          >
            <path d="M12 17.75L7.192 20.768L8.5 15.344L4.5 11.768L9.9 11.24L12 6.25L14.1 11.24L19.5 11.768L15.5 15.344L16.808 20.768L12 17.75Z" />
          </svg>
        </div>
      </div>
    </div>
  );
}


const ExerciseScreen = ({ onBack }) => {
  // Tab control
  const [selectedTab, setSelectedTab] = useState('Workout Log');
  const today = new Date();

  // Days of week in English
  const daysOfWeek = [
    "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
  ];
  const dayName = daysOfWeek[today.getDay()];

  // Calendar states (for Workout Log tab)
  const [selectedDay, setSelectedDay] = useState(today.getDate());
  const [currentMonth, setCurrentMonth] = useState(
    today.toLocaleString('en-US', { month: 'long' })
  );
  const [currentYear, setCurrentYear] = useState(today.getFullYear());
  const [showMonths, setShowMonths] = useState(false);
  const [showYears, setShowYears] = useState(false);

  // Month and Year arrays
  const months = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];
  const thisYear = today.getFullYear();
  const years = [thisYear, thisYear - 1, thisYear - 2, thisYear - 3];

  // Function to get days in month (Monday start)
  const getDaysInMonth = (month, year) => {
    const monthIndex = months.indexOf(month);
    const daysInMonth = new Date(year, monthIndex + 1, 0).getDate();
    const firstDay = new Date(year, monthIndex, 1).getDay(); // 0 = Sunday
    const startDay = firstDay === 0 ? 6 : firstDay - 1; // Adjust for Monday
    const calendarDays = [];
    for (let i = 0; i < startDay; i++) {
      calendarDays.push(null);
    }
    for (let i = 1; i <= daysInMonth; i++) {
      calendarDays.push(i);
    }
    return calendarDays;
  };

  const calendarDays = getDaysInMonth(currentMonth, currentYear);

  // Example activities
  const activities = [
    {
      name: 'Upper Body Workout',
      kcal: 120,
      date: 'January 5',
      duration: '25 Mins',
      day: 5,
      month: 'January'
    },
    {
      name: 'Leg Day',
      kcal: 150,
      date: 'January 12',
      duration: '30 Mins',
      day: 12,
      month: 'January'
    },
    {
      name: 'Cardio Blast',
      kcal: 200,
      date: 'March ' + today.getDate(),
      duration: '20 Mins',
      day: today.getDate(),
      month: 'March'
    },
    {
      name: 'Cardio Blast',
      kcal: 200,
      date: 'March ' + today.getDate(),
      duration: '25 Mins',
      day: today.getDate(),
      month: 'March'
    },
    {
      name: 'Core Workout',
      kcal: 110,
      date: 'January 23',
      duration: '15 Mins',
      day: 23,
      month: 'January'
    },
    {
      name: 'HIIT Session',
      kcal: 180,
      date: 'January 30',
      duration: '20 Mins',
      day: 30,
      month: 'January'
    },
    {
      name: 'Upper Body Workout',
      kcal: 130,
      date: 'February 3',
      duration: '25 Mins',
      day: 3,
      month: 'February'
    },
    {
      name: 'Strength Training',
      kcal: 160,
      date: 'February 8',
      duration: '30 Mins',
      day: 8,
      month: 'February'
    },
    {
      name: 'Cardio Blast',
      kcal: 210,
      date: 'February 10',
      duration: '20 Mins',
      day: 10,
      month: 'February'
    },
    {
      name: 'Leg Day',
      kcal: 155,
      date: 'February 14',
      duration: '30 Mins',
      day: 14,
      month: 'February'
    },
    {
      name: 'HIIT Session',
      kcal: 175,
      date: 'February 18',
      duration: '20 Mins',
      day: 18,
      month: 'February'
    },
    {
      name: 'Upper Body Workout',
      kcal: 125,
      date: 'February 21',
      duration: '25 Mins',
      day: 21,
      month: 'February'
    },
    {
      name: 'Core Workout',
      kcal: 115,
      date: 'February 24',
      duration: '15 Mins',
      day: 24,
      month: 'February'
    },
    {
      name: 'Run',
      kcal: 130,
      date: 'February 27',
      duration: '30 Mins',
      day: 27,
      month: 'February'
    },
    {
      name: 'Strength Training',
      kcal: 140,
      date: 'March 1',
      duration: '25 Mins',
      day: 1,
      month: 'March'
    },
    {
      name: 'Cardio Blast',
      kcal: 190,
      date: 'January 2',
      duration: '20 Mins',
      day: 2,
      month: 'January'
    },
    {
      name: 'HIIT Session',
      kcal: 180,
      date: 'March 3',
      duration: '20 Mins',
      day: 3,
      month: 'March'
    },
    {
      name: 'Upper Body Workout',
      kcal: 130,
      date: 'January 4',
      duration: '25 Mins',
      day: 4,
      month: 'January'
    },
    {
      name: 'Leg Day',
      kcal: 150,
      date: 'January 5',
      duration: '30 Mins',
      day: 5,
      month: 'January'
    },
    {
      name: 'Core Workout',
      kcal: 115,
      date: 'January 28',
      duration: '15 Mins',
      day: 28,
      month: 'January'
    },
    {
      name: 'Run',
      kcal: 135,
      date: 'February 16',
      duration: '30 Mins',
      day: 16,
      month: 'February'
    },
  ];

  // Filter activities for the selected day
  const filteredActivities = (selectedDay !== null && selectedDay !== undefined)
  ? activities.filter(act => act.day === selectedDay && act.month === currentMonth)
  : activities.slice(-5);

  const hasActivity = (day) => {
    return activities.some(
      (act) => act.day === day && act.month === currentMonth
    );
  };

  // Handlers for changing month and year
  const handleMonthChange = (month) => {
    setCurrentMonth(month);
    setShowMonths(false);
    setSelectedDay(null);
  };

  const handleYearChange = (year) => {
    setCurrentYear(year);
    setShowYears(false);
    setSelectedDay(null);
  };

  const maxYear = today.getFullYear();
  const minYear = maxYear - 3;

  const handlePrevMonth = () => {
    const currentIndex = months.indexOf(currentMonth);
    if (currentIndex === 0) {
      if (currentYear > minYear) {
        setCurrentMonth(months[11]);
        setCurrentYear(currentYear - 1);
      }
    } else {
      setCurrentMonth(months[currentIndex - 1]);
    }
    setSelectedDay(null);
  };

  const handleNextMonth = () => {
    const currentIndex = months.indexOf(currentMonth);
    if (currentIndex === 11) {
      if (currentYear < maxYear) {
        setCurrentMonth(months[0]);
        setCurrentYear(currentYear + 1);
      }
    } else {
      setCurrentMonth(months[currentIndex + 1]);
    }
    setSelectedDay(null);
  };

  // Fade-out animation when switching to Charts or Routines
  const [isFadingOut, setIsFadingOut] = useState(false);

  const handleTabChange = (tab) => {
    if (tab === 'Charts' || tab === 'Routines') {
      setIsFadingOut(true);
      setTimeout(() => {
        setSelectedTab(tab);
        setIsFadingOut(false);
      }, 0);
    } else {
      setSelectedTab(tab);
    }
  };

  // State for favorite (star) toggle
  const [isStarred, setIsStarred] = useState(false);
  const handleStarClick = () => {
    setIsStarred(!isStarred);
  };

  // Array of recommendations for the Routines tab
  const recommendations = [
    {
      title: "Upper Body",
      image: FirstExercise,
      duration: "60 Minutes",
      calories: "1320 Kcal",
      exercises: 5
    },
    {
      title: "Full Body Stretching",
      image: SecondExercise,
      duration: "45 Minutes",
      calories: "1150 Kcal",
      exercises: 5
    },
    {
      title: "Glutes & Abs",
      image: ThirdExercise,
      duration: "40 Minutes",
      calories: "900 Kcal",
      exercises: 5
    },
  ];

  return (
    <div className="exercise-screen">
      {/* Header with Back button */}
      <div className="conditions-header">
        <div className="back-wrapper">
          <button className="conditions-back-button" onClick={onBack}>
            <svg xmlns="http://www.w3.org/2000/svg" width="17" height="15" viewBox="0 0 17 15" fill="none">
              <path
                d="M7.45408 13.6896L1.0805 7.47707L7.29305 1.10349M15.4646 7.29304L1.0805 7.47707L15.4646 7.29304Z"
                stroke="black" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
              />
            </svg>
          </button>
          <div className="header-content">
            <img src={logoDashboard} alt="HealthIA" className="header-logo" />
            <div className="header-text">
              <h1>HealthIA</h1>
              <p>Your Workout</p>
            </div>
          </div>
        </div>
      </div>

      {/* Profile Banner (hidden in Charts or Routines) */}
      {selectedTab !== 'Charts' && selectedTab !== 'Routines' && (
        <div className={`profile-banner ${isFadingOut ? 'fade-out' : ''}`}>
          <div className="profile-content">
            <div className="profile-info">
              <div>
                <div className="profile-name">FRANSUA</div>
                <div className="profile-age">Age: 21</div>
              </div>
              <div className="profile-stats">
                <div className="stat-box">
                  <div className="stat-value">72 Kg</div>
                  <div className="stat-label">Weight</div>
                </div>
                <div className="stat-box">
                  <div className="stat-value">1.69 CM</div>
                  <div className="stat-label">Height</div>
                </div>
              </div>
            </div>
            <img src={avatarFransua} alt="Profile" className="profile-avatar" />
          </div>
        </div>
      )}

      {/* Tabs Navigation */}
      <div className="workout-nav" >
        <button
          className={`nav-button ${selectedTab === 'Workout Log' ? 'active' : ''}`}
          onClick={() => setSelectedTab('Workout Log')}
        >
          Workout Log
        </button>
        <button
          className={`nav-button ${selectedTab === 'Charts' ? 'active' : ''}`}
          onClick={() => handleTabChange('Charts')}
        >
          Charts
        </button>
        <button
          className={`nav-button ${selectedTab === 'Routines' ? 'active' : ''}`}
          onClick={() => handleTabChange('Routines')}
        >
          Routines
        </button>
      </div>

      {/* Content by Tab */}
      {selectedTab === 'Workout Log' && (
        <>
          {/* Calendar */}
          <div className="calendar-container">
            <div className="calendar-header">
              <div className="month-selector">
                <button className="month-arrow" onClick={handlePrevMonth}>{'<'}</button>
                <span className="month-selector-text" onClick={() => { setShowMonths(!showMonths); setShowYears(false); }}>{currentMonth}</span>
                <button className="month-arrow" onClick={handleNextMonth}>{'>'}</button>
                {showMonths && (
                  <div className="month-dropdown">
                    {months.map((m) => (
                      <div
                        key={m}
                        className="month-option"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleMonthChange(m);
                        }}
                      >
                        {m}
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div
                className="year-selector"
                onClick={() => {
                  setShowYears(!showYears);
                  setShowMonths(false);
                }}
              >
                <span>{currentYear}</span>
                <DropdownIcon />
                {showYears && (
                  <div className="year-dropdown">
                    {years.map((year) => (
                      <div
                        key={year}
                        className="year-option"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleYearChange(year);
                        }}
                      >
                        {year}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            {/* Weekday Headers */}
            <div className="weekdays-grid">
              {['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'].map((day) => (
                <div key={day} className="weekday">
                  {day}
                </div>
              ))}
            </div>

            {/* Calendar Days */}
            <div className="calendar-grid">
              {calendarDays.map((day, index) => {
                if (!day) {
                  return <div key={index} className="calendar-day empty"></div>;
                }
                const isToday =
                  day === today.getDate() &&
                  currentMonth === months[today.getMonth()] &&
                  currentYear === today.getFullYear();

                const todayClass = isToday ? 'today' : '';
                const activityClass = hasActivity(day) ? 'has-activity' : '';
                const selectedClass = day === selectedDay ? 'active' : '';

                return (
                  <div
                    key={index}
                    className={`calendar-day ${selectedClass} ${todayClass} ${activityClass}`}
                    onClick={() => setSelectedDay(day)}
                  >
                    {day}
                    {hasActivity(day) && <div className="exercise-dot"></div>}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Activities Section */}
          <div className="activities-section">
            <h2 className="activities-title">Activities</h2>
            {filteredActivities.map((activity, idx) => (
              <div key={idx} className="activity-card">
                <div className="activity-icon-exercise">
                  <img src={runIcon} alt="Running" />
                </div>
                <div className="activity-details">
                  <div className="activity-header">
                    <div className="activity-name">{activity.name}</div>
                    <div className="activity-kcal">{activity.kcal} kcal</div>
                  </div>
                  <div className="activity-date">{activity.date}</div>
                  <div className="activity-duration">
                    <DurationIcon />
                    <span>Duration {activity.duration}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </>
      )}

      {selectedTab === 'Charts' && (
        <div style={{ padding: '20px' }}>
          <ExercisesCharts />
        </div>
      )}

      {selectedTab === 'Routines' && (
        <div className="routines-container">
          {/* Routines Background */}
          <div className="routines-background">
            {/* White box with today's day */}
            <div className="routines-workout">
              <div className="routines-workout-title">
                Workout by Healthia - {dayName}
              </div>
            </div>

            {/* Main Image */}
            <div className="routines-image">
              <img
                className="routines-images-container"
                src={PrincipalExercise}
                alt="Functional Training"
              />
              {/* Overlay with routine info */}
              <div className="routines-overlay">
                <div className="routines-title">Functional Training</div>
                <div className="routines-duration">45 Minutes</div>
                <div className="routines-calories">1450 Kcal</div>
                <div className="routines-exercises">5 Exercises</div>

                {/* Star icon (favorite) */}
                <div
                  style={{
                    position: 'absolute',
                    right: 14,
                    top: 6,
                    cursor: 'pointer'
                  }}
                  onClick={handleStarClick}
                >
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <path
                      d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.965a1 1 0 00.95.69h4.162c.969 0 1.371 1.24.588 1.81l-3.37 2.449a1 1 0 00-.364 1.118l1.286 3.965c.3.921-.755 1.688-1.54 1.118l-3.37-2.449a1 1 0 00-1.176 0l-3.37 2.449c-.784.57-1.84-.197-1.54-1.118l1.286-3.965a1 1 0 00-.364-1.118l-3.37-2.449c-.783-.57-.38-1.81.588-1.81h4.162a1 1 0 00.95-.69l1.286-3.965z"
                      fill={isStarred ? "#F9D65B" : "none"}
                      stroke="#F9D65B"
                      strokeWidth="1.2"
                    />
                  </svg>
                </div>
              </div>
            </div>
          </div>

          {/* Recommended Workouts Section */}
          <div className="recommended-workouts-section">
            <h2 className="recommended-title">Let's Go, Fransua</h2>
            <p className="recommended-subtitle">
              Explore Other Recommended Workouts
            </p>
            <div className="recommended-cards-container">
            {recommendations.map((item, idx) => (
                <RecommendationCard
                  key={idx}
                  title={item.title}
                  image={item.image}
                  duration={item.duration}
                  calories={item.calories}
                  exercises={item.exercises}
                />
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ExerciseScreen;
