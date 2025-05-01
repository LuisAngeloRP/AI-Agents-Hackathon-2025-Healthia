# 🤖 HealthIA

> Your intelligent personal assistant for a healthier life — powered by Azure AI and OpenAI.

<img src="https://github.com/user-attachments/assets/6a6dfcd8-726a-41e1-bc6d-92ec5ad27f16" alt="HealthIA Demo"/>

<p align="center">
  <b>Personalized nutrition, workouts, and health guidance — developed with Java, Azure AI, OpenAI, and LangGraph for the Microsoft Hackathon.</b>
</p>

<p align="center">
  <a href="https://sergioyupanqui.com" target="_blank">
    <img src="https://img.shields.io/badge/Live-Demo-blue?style=for-the-badge" alt="Live Demo"/>
  </a>
</p>

<p align="center"><strong>👉 To try it now, visit <a href="https://sergioyupanqui.com" target="_blank">www.sergioyupanqui.com</a> from your PC or mobile and analyze your own plate instantly!</strong></p>

---

## 🚨 The Problem

- 43% of adults globally were overweight in 2022.
- Cardiovascular diseases account for 17.9 million deaths annually (32% of all deaths).
- 80% of users abandon health apps within 30 days due to lack of personalization.

📌 Source: WHO, 2024; Mustafa et al., 2022

---

## 💡 The Solution

**HealthIA** is a cross-platform health assistant powered by Azure AI and OpenAI. It connects to wearables or accepts manual input to generate customized health plans and proactive recommendations. Our goal is to fight inactivity and poor nutrition with real-time, data-driven interventions.

It includes:

- Smart health chatbot (Q&A + insights)
- OCR meal plate scanner using AI Vision
- Personalized diet and workout recommendations
- Medical condition-aware routines
- Real-time feedback and gamification

---

## 🔍 Core Features

### 1. 🧠 Health Chatbot
Powered by Azure OpenAI, our assistant responds to queries like:
> “I work remotely and feel back pain.”

→ Suggests stretches, hydration, and custom wellness tips.

### 2. 📸 Meal Scanner with AI Vision
- **Snap a photo** of your meal.
- **Azure AI Vision** compares it with the **Harvard Plate Model**.
- Personalized suggestions: _Add more greens_, _Reduce carbs_, _Increase protein_.

👉 Try it at **[www.sergioyupanqui.com](https://sergioyupanqui.com)** from your phone or PC!

### 3. 🥗 Diet Generator
- Recipes tailored to health profile and preferences.
- Nutritional values and ingredient lists.
- WhatsApp sharing and video tutorials.

### 4. 🏋️ Workout Planner
- Weekly plans based on user metrics.
- Tutorials for each exercise.
- Adjusts based on calories burned.

### 5. 🩺 Medical Personalization
- Input allergies, conditions, and goals.
- AI filters recommendations for safety.
- Smart reminders based on real-time analysis.

---

## 📊 System Architecture

<img src="https://github.com/user-attachments/assets/8378437f-831f-47f9-82fd-a9bb34a6db81" alt="Azure Architecture" />

Our Azure-hosted backend includes:
- **Azure API Management** – Manages API calls between services
- **Azure Functions** – Serverless logic for meals, routines, OCR, metrics
- **Azure Blob Storage** – Secure storage for images
- **Azure AI Vision** – OCR & food detection
- **Azure Cosmos DB** – Stores chat, user data, metrics
- **OpenAI API (via Azure)** – Smart chatbot + NLP
- **Azure MySQL** – Profile & login data
- **Azure Monitor + Logs** – Real-time system health
- **Azure FrontDoor + WAF + DDoS Protection** – Security and global delivery

# 📈 Sequence Diagrams – HealthIA

---

## 1️⃣ Meal Scanner + AI Feedback

This diagram shows the process flow from the user uploading a meal photo to receiving personalized nutritional feedback using Azure services and OpenAI.

📄 Diagram:

<img src="https://github.com/user-attachments/assets/bbb6732a-99c7-4301-a17d-fa31efb634f7" alt="HealthIA 1"/>

---

## 2️⃣ Health Chatbot Interaction

This diagram demonstrates how the user interacts with the HealthIA chatbot, which leverages Azure Cosmos DB and OpenAI to respond intelligently.

📄 Diagram:

<img src="https://github.com/user-attachments/assets/5de7974e-f949-4f0b-982c-3affa87efb0c" alt="HealthIA 2"/>

## 3️⃣ Workout Routine Generator

This sequence outlines how HealthIA generates a personalized workout routine using stored user data and Azure-integrated OpenAI.

📄 Diagram:
<img src="https://github.com/user-attachments/assets/d42d17c3-b126-4b8d-a72f-0a5659cfb5c5" alt="HealthIA 3"/>

---

## 📆 Development Timeline

Our Agile roadmap:

1. ✅ Q1: Core App (chatbot, plans, UX)
2. 📷 Q2: Vision OCR + Plate Analyzer
3. 🎮 Q3: Gamification Engine
4. 🚀 Q4: Final Release & Azure Marketplace

---

## 💰 Business Model

Freemium:
- **Free**: 5 meal/exercise plans/week
- **Premium** ($4.99/mo): Unlimited, real-time tracking, AI personalization

---

## 🌍 Social Impact

HealthIA combats:
- Poor diet and sedentary lifestyle
- Dropout in traditional health apps
- Lack of real-time personalized care

We empower users with tools to take control of their wellness using familiar tech and friendly AI.

---

## 👥 Meet the Team

<p align="center">
  <a href="https://www.linkedin.com/in/fransua-leon/" target="_blank">
    <img src="https://img.shields.io/badge/Fransua%20Leon-LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" />
  </a>
  <a href="https://www.linkedin.com/in/sergioyupanquigomez/" target="_blank">
    <img src="https://img.shields.io/badge/Sergio%20Yupanqui-LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" />
  </a>
  <a href="https://www.linkedin.com/in/luisangelorp/" target="_blank">
    <img src="https://img.shields.io/badge/Luis%20Rodriguez-LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" />
  </a>
  <a href="https://www.linkedin.com/in/andrepachecot/" target="_blank">
    <img src="https://img.shields.io/badge/André%20Pacheco-LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" />
  </a>
</p>

---

## 📇 Contact

Built with ❤️ by Team **CevicherIA** — National University of Engineering (UNI)

- 🌐 [sergioyupanqui.com](https://sergioyupanqui.com)
