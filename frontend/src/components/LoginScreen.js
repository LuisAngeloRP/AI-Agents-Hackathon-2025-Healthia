import React, { useState } from "react";
import "../styles/LoginScreen.css";
import logoSign from "../assets/logos/logo_sign.png";
import backgroundSign from "../assets/background/background_sign.svg";
import HomeScreen from "./HomeScreen";
import emailIcon from "../assets/icons/email.svg";
// Puedes seguir usando passwordIcon si lo deseas para el label; en este ejemplo se mantiene a la izquierda
import passwordIcon from "../assets/icons/password.svg";
import signInArrow from "../assets/icons/sign-in-arrow.svg";
import instagramIcon from "../assets/icons/instagram.svg";
import facebookIcon from "../assets/icons/facebook.svg";
import linkedinIcon from "../assets/icons/linkedin.svg";
import backArrow from "../assets/icons/back-arrow.svg";
import { useNavigate } from "react-router-dom";

const LoginScreen = ({ onLoginSuccess, onSignUpClick, onForgotPasswordClick, onBack }) => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [inputError, setInputError] = useState({ email: false, password: false });
  const [showSuggestion, setShowSuggestion] = useState(false);
  const [activeModal, setActiveModal] = useState(null);
  const [signUpData, setSignUpData] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: ""
  });
  const [resetEmail, setResetEmail] = useState("");
  const [showPassword, setShowPassword] = useState(false); // Estado para togglear visibilidad
  const navigate = useNavigate();

  console.log('onLoginSuccess:', onLoginSuccess);

  const handleSuggestionClick = () => {
    setEmail("fransua@healthia.com");
    setPassword("cevicheria");
    setShowSuggestion(false);
    setError("");
    setInputError({ email: false, password: false });
  };

  const handleLogin = () => {
    setError("");
    setInputError({ email: false, password: false });
    setLoading(true);

    setTimeout(() => {
      setLoading(false);

      if (email !== "fransua@healthia.com" && password !== "cevicheria") {
        setError("❌ Incorrect email or password");
        setInputError({ email: true, password: true });
      } else if (email !== "fransua@healthia.com") {
        setError("❌ Incorrect email");
        setInputError({ email: true, password: false });
      } else if (password !== "cevicheria") {
        setError("❌ Incorrect password");
        setInputError({ email: false, password: true });
      } else {
        onLoginSuccess();
        navigate('/');
      }
    }, 1500);
  };

  const handleSignUp = () => {
    if (!signUpData.name || !signUpData.email || !signUpData.password || !signUpData.confirmPassword) {
      alert("Please fill in all fields");
      return;
    }
    if (signUpData.password !== signUpData.confirmPassword) {
      alert("Passwords do not match");
      return;
    }
    alert("Sign up successful! Please log in.");
    setActiveModal(null);
    setSignUpData({ name: "", email: "", password: "", confirmPassword: "" });
  };

  const handleForgotPassword = () => {
    if (!resetEmail) {
      alert("Please enter your email");
      return;
    }
    alert("Password reset instructions have been sent to your email");
    setActiveModal(null);
    setResetEmail("");
  };

  // Toggle para mostrar/ocultar contraseña
  const toggleShowPassword = () => {
    setShowPassword(!showPassword);
  };

  return (
    <div className="login-screen">
      <img src={backgroundSign} alt="Background" className="login-background" />
      
      <div className="analyze-header">
        <button className="conditions-back-button" onClick={() => navigate('/home')}>
          <svg xmlns="http://www.w3.org/2000/svg" width="17" height="15" viewBox="0 0 17 15" fill="none">
            <path d="M7.45408 13.6896L1.0805 7.47707L7.29305 1.10349M15.4646 7.29304L1.0805 7.47707L15.4646 7.29304Z" 
            stroke="black" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </button>
      </div>

      {/* Main Content */}
      <div className="login-content">
        <div className="login-header">
          <img src={logoSign} alt="HealthIA Logo" className="login-logo" />
          <h1 className="login-title">HealthIA</h1>
        </div>

        {/* Input field for Email */}
        <div className="login-input-group">
          <label>Email Address</label>
          <div className={`login-input-field ${inputError.email ? "error" : ""}`}>
            <img src={emailIcon} alt="Email Icon" className="login-input-icon" />
            <div className="email-input-container">
              <input 
                type="email" 
                placeholder="Enter your email..." 
                value={email} 
                onChange={(e) => setEmail(e.target.value)}
                onFocus={() => setShowSuggestion(true)}
                onBlur={() => setTimeout(() => setShowSuggestion(false), 200)}
              />
              {showSuggestion && !email && (
                <div className="email-suggestion" onClick={handleSuggestionClick}>
                  fransua@healthia.com
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Input field for Password with toggle eye icon */}
        <div className="login-input-group">
          <label>Password</label>
          <div className={`login-input-field ${inputError.password ? "error" : ""}`} style={{ position: 'relative' }}>
            <img src={passwordIcon} alt="Password Icon" className="login-input-icon" />
            <div className="email-input-container">
            <input 
              type={showPassword ? "text" : "password"}
              placeholder="Enter your password..." 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
            />
            {/* Icono de toggle de visibilidad, posicionado a la derecha */}
            <div 
              onClick={toggleShowPassword}
              style={{
                position: 'absolute',
                right: '10px',
                top: '50%',
                transform: 'translateY(-50%)',
                cursor: 'pointer'
              }}
            >
              {showPassword ? (
                // Ojo abierto: contraseña visible
                <div data-svg-wrapper style={{ position: 'relative' }}>
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M10.6019 9.52011C11.5094 9.21761 12.4906 9.21761 13.3981 9.52011C13.9088 9.69037 14.3096 10.0912 14.4799 10.6019C14.7824 11.5094 14.7824 12.4906 14.4799 13.3981C14.3096 13.9088 13.9088 14.3096 13.3981 14.4799C12.4906 14.7824 11.5094 14.7824 10.6019 14.4799C10.0912 14.3096 9.69037 13.9088 9.52011 13.3981C9.21761 12.4906 9.21761 11.5094 9.52011 10.6019C9.69037 10.0912 10.0912 9.69037 10.6019 9.52011Z" fill="black"/>
                    <path fillRule="evenodd" clipRule="evenodd" d="M4.49033 6.91074C8.71776 2.96107 15.2822 2.96107 19.5096 6.91074C19.8579 7.23614 20.2365 7.65686 20.8988 8.39272L21.6194 9.19344C21.7617 9.35155 21.8821 9.52813 21.9772 9.71839C22.6954 11.1547 22.6954 12.8453 21.9772 14.2816C21.8821 14.4719 21.7617 14.6485 21.6194 14.8066L20.8988 15.6073C20.2366 16.343 19.8579 16.7639 19.5096 17.0893C15.2822 21.039 8.71776 21.039 4.49033 17.0893C4.14205 16.7639 3.76341 16.3432 3.10115 15.6073L2.38053 14.8066C2.23822 14.6485 2.11788 14.4719 2.02275 14.2816C1.30459 12.8453 1.30459 11.1547 2.02275 9.71839C2.11788 9.52813 2.23822 9.35155 2.38053 9.19344L3.10118 8.39272C3.76342 7.65686 4.14205 7.23614 4.49033 6.91074ZM14.0305 7.62275C12.7125 7.1834 11.2875 7.1834 9.96949 7.62275C8.86151 7.99207 7.99207 8.86151 7.62275 9.96949C7.1834 11.2875 7.1834 12.7125 7.62275 14.0305C7.99207 15.1385 8.86151 16.0079 9.96949 16.3773C11.2875 16.8166 12.7125 16.8166 14.0305 16.3773C15.1385 16.0079 16.0079 15.1385 16.3773 14.0305C16.8166 12.7125 16.8166 11.2875 16.3773 9.96949C16.0079 8.86151 15.1385 7.99207 14.0305 7.62275Z" fill="black"/>
                  </svg>
                </div>
              ) : (
                // Ojo cerrado: contraseña oculta
                <div data-svg-wrapper style={{ position: 'relative' }}>
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M18 9L16.2857 12.4286C16.1003 12.7995 15.7995 13.1003 15.4286 13.2857V13.2857C13.2702 14.3649 10.7298 14.3649 8.57143 13.2857V13.2857C8.20051 13.1003 7.89975 12.7995 7.71429 12.4286L6 9" stroke="#393C43" strokeWidth="2" strokeLinejoin="round"/>
                  </svg>
                </div>
              )}
            </div>
            </div>
          </div>
        </div>

        <button 
          className={`login-sign-in-button ${loading ? "loading" : ""}`} 
          onClick={handleLogin} 
          disabled={loading}
        >
          {loading ? "Loading..." : "Sign In"} 
          {!loading && <img src={signInArrow} alt="Arrow Icon" className="login-arrow-icon" />}
        </button>

        {error && <p className="login-error-text">{error}</p>}

        <div className="login-social-icons">
          <a href="https://consumer.huawei.com/pe/offer/" target="_blank" rel="noopener noreferrer" className="login-social-btn">
            <img src={instagramIcon} alt="Instagram" />
          </a>
          <a href="https://consumer.huawei.com/pe/offer/" target="_blank" rel="noopener noreferrer" className="login-social-btn">
            <img src={facebookIcon} alt="Facebook" />
          </a>
          <a href="https://consumer.huawei.com/pe/offer/" target="_blank" rel="noopener noreferrer" className="login-social-btn">
            <img src={linkedinIcon} alt="LinkedIn" />
          </a>
        </div>

        <p className="login-footer-text">
          Don't have an account?{" "}
          <button className="login-sign-up-link" onClick={() => setActiveModal('signup')}>
            Sign Up
          </button>{" "}
          or{" "}
          <button className="login-forgot-link" onClick={() => setActiveModal('forgot')}>
            Forgot Password?
          </button>
        </p>
      </div>

      {/* Sign Up Modal */}
      {activeModal === 'signup' && (
        <div className="modal-overlay" onClick={() => setActiveModal(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h3>Create Account</h3>
            <div className="modal-input-group">
              <div className="modal-input-field">
                <img src={emailIcon} alt="User Icon" className="modal-input-icon" />
                <input
                  type="text"
                  placeholder="Full Name"
                  value={signUpData.name}
                  onChange={e => setSignUpData({ ...signUpData, name: e.target.value })}
                />
              </div>
              <div className="modal-input-field">
                <img src={emailIcon} alt="Email Icon" className="modal-input-icon" />
                <input
                  type="email"
                  placeholder="Email Address"
                  value={signUpData.email}
                  onChange={e => setSignUpData({ ...signUpData, email: e.target.value })}
                />
              </div>
              <div className="modal-input-field">
                <img src={passwordIcon} alt="Password Icon" className="modal-input-icon" />
                <input
                  type="password"
                  placeholder="Password"
                  value={signUpData.password}
                  onChange={e => setSignUpData({ ...signUpData, password: e.target.value })}
                />
              </div>
              <div className="modal-input-field">
                <img src={passwordIcon} alt="Confirm Password Icon" className="modal-input-icon" />
                <input
                  type="password"
                  placeholder="Confirm Password"
                  value={signUpData.confirmPassword}
                  onChange={e => setSignUpData({ ...signUpData, confirmPassword: e.target.value })}
                />
              </div>
            </div>
            <div className="modal-actions">
              <button className="modal-cancel" onClick={() => setActiveModal(null)} style={{ backgroundColor: 'white', color: '#0957DE', border: '2px dashed #0957DE' }}>
                Cancel
              </button>
              <button className="modal-submit" onClick={handleSignUp}>
                Sign Up
                <img src={signInArrow} alt="Arrow Icon" className="modal-submit-arrow" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Forgot Password Modal */}
      {activeModal === 'forgot' && (
        <div className="modal-overlay" onClick={() => setActiveModal(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h3>Reset Password</h3>
            <p className="modal-description">
              Enter your email address and we'll send you instructions to reset your password.
            </p>
            <div className="modal-input-group">
              <input
                type="email"
                placeholder="Email Address"
                value={resetEmail}
                onChange={e => setResetEmail(e.target.value)}
                className="modal-input"
              />
            </div>
            <div className="popup-buttons">
              <button className="modal-cancel" onClick={() => setActiveModal(null)} style={{ backgroundColor: 'white', color: '#0957DE', border: '2px dashed #0957DE' }}>
                Cancel
              </button>
              <button className="modal-submit" onClick={handleForgotPassword}>
                Send Instructions
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default LoginScreen;
