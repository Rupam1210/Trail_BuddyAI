# 🌿 TrailBuddy AI — Plan Less on Your Phone. Explore More Outside.

**An open-source AI outdoor activity planner built with React, Spring Boot, and Ollama.**

This project is my submission for the [Hacktoberfest Open-Source AI Challenge — Week 1: Touch Grass](https://dev.to/challenges/hacktoberfest-week1-2026-10-05).

TrailBuddy AI helps people spend less time on screens and more time exploring the real world. It creates personalized outdoor plans based on the user's activity, available time, experience level, location, and interests.

**The idea is simple: use AI to plan the adventure, then put the phone away.**

## ✨ Features

- 🥾 Outdoor planning for hiking, walking, gardening, birdwatching, and running
- 📍 Customizable location, duration, pace, and interests
- 🧭 Personalized activity itineraries
- 🎒 Interactive packing checklist
- 🌱 Nature missions with completion tracking
- 🛡️ Safety reminders and responsible outdoor guidance
- 🖨️ Print or save the plan as PDF
- 🤖 Optional local AI inference using Ollama and Qwen2.5 3B
- 🔄 Built-in fallback planner when local AI is unavailable
- 📱 Responsive user interface

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| React | Frontend user interface |
| Vite | Frontend development server and build tool |
| Java 17+ | Backend programming language |
| Spring Boot 3 | REST API and application logic |
| Maven | Backend dependency management |
| Ollama | Local AI inference |
| Qwen2.5 3B | Default open-weight language model |
| REST API | Communication between frontend and backend |

## 🏗️ Architecture

```text
         React + Vite
        localhost:5173
               |
               | HTTP / JSON
               v
       Spring Boot Backend
        localhost:8080
               |
        +------+------+
        |             |
        v             v
  Local Ollama    Fallback Planner
  Qwen2.5 3B      Built-in Logic
        |             |
        +------+------+
               |
               v
    Outdoor Activity Plan
```

## 🚀 Getting Started

### Prerequisites

Install the following tools before running the project:

- [Java JDK 17 or newer](https://adoptium.net/)
- [Node.js 18 or newer](https://nodejs.org/)
- [Apache Maven](https://maven.apache.org/download.cgi)
- [Ollama](https://ollama.com/) — optional, for local AI

Verify your installations:

```bash
java -version
mvn -version
node --version
npm --version
```

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_GITHUB_USERNAME/trailbuddy-ai.git
cd trailbuddy-ai
```

### 2. Run the Spring Boot Backend

Open a terminal in the `backend` directory:

```bash
cd backend
mvn spring-boot:run
```

The backend runs at:

`http://localhost:8080`

Keep this terminal running.

### 3. Run the React Frontend

Open a second terminal in the project root:

```bash
cd frontend
npm install
npm run dev
```

Open the URL displayed by Vite, normally:

`http://localhost:5173`

The frontend communicates with the Spring Boot backend through the configured Vite API proxy.

## 🤖 Setting Up Local Open-Weight AI

TrailBuddy can work without an AI model using its built-in fallback planner. To enable AI-generated plans, install Ollama and download the default model.

### Download the model

```bash
ollama pull qwen2.5:3b
```

Ensure Ollama is running. If necessary, start it with:

```bash
ollama serve
```

The Spring Boot backend uses the following default configuration:

- Ollama URL: `http://127.0.0.1:11434`
- Model: `qwen2.5:3b`

You can configure these using the `OLLAMA_URL` and `OLLAMA_MODEL` environment variables.

**Note:** The model weights are downloaded separately and are not included in this repository. The initial download requires internet access; subsequent local inference can work without an internet connection.

## 🔌 API Endpoints

### Health Check

`GET /api/health`

Checks whether the backend is running and whether Ollama and the configured model are available.

### Generate an Outdoor Plan

`POST /api/plan`

Example request:

```json
{
  "place": "a nearby park or trail",
  "activity": "hiking",
  "duration": 120,
  "level": "beginner",
  "interests": "birds, quiet, and nature"
}
```

The API returns an outdoor plan containing:

- Title and introduction
- Activity itinerary
- Packing checklist
- Nature missions
- Safety recommendations
- Plan generation source

Supported activities include `hiking`, `walking`, `gardening`, `birding`, and `running`.

## 💚 Why Does Open Innovation Matter?

### 🔒 Privacy and Local Inference

When Ollama is running locally, the AI prompt is processed on the user's own computer rather than being sent to a hosted AI API. This provides greater control over personal data.

### 🔧 Freedom to Experiment

Developers can change the model, customize prompts, and modify planning logic without being locked into a single proprietary AI provider.

### 💸 Accessible AI Development

The project does not require a paid AI API key. After downloading a compatible model, inference uses the computer's own resources instead of a per-request hosted AI service.

The built-in fallback planner also allows users to try the application without configuring local AI.

### 🌍 AI That Encourages Real-World Experiences

TrailBuddy is designed to make the screen a short stop before an outdoor activity, not another destination for endless scrolling.

The ultimate goal is to help people explore nature, observe their surroundings, and enjoy more time away from their devices.

## 🌱 Responsible Use and Limitations

- The application does not currently fetch live weather, trail conditions, maps, park hours, or local regulations.
- Verify weather, access, daylight, and route conditions before heading outside.
- AI-generated recommendations can be inaccurate.
- Respect wildlife, follow permitted paths, and leave no trace.
- Local AI inference uses your computer's processing power, memory, and electricity.

## 📁 Project Structure

```text
trailbuddy-ai/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/trailbuddy/
│       │   ├── TrailBuddyApplication.java
│       │   ├── controller/PlanController.java
│       │   ├── model/PlanRequest.java
│       │   └── service/PlanService.java
│       └── resources/application.properties
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.jsx
│       ├── App.jsx
│       └── styles.css
└── README.md
```

## 🔮 Future Improvements

- Live weather and verified trail information
- Map-based route recommendations
- Bird-call identification using open-source audio models
- Seasonal gardening recommendations
- Downloadable outdoor plans for offline access

 
---

**Built for the Touch Grass challenge. Plan the adventure. Then go live it. 🌿**
