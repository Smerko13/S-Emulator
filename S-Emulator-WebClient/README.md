# S-Emulator Web Client

This web client talks to the existing Tomcat server (no server changes required).  
It uses the same REST APIs as the JavaFX client.

## Prerequisites

- **Node.js 18+** is installed (npm included)
- The **Tomcat server** is already running at: `http://localhost:8080/S_Emulator_Server`
  - If your server is at a different URL, update `SERVER_BASE_URL` in the `.bat` file.

## Quick Start (Windows)

1. **Double-click**: `start_dev.bat`  
   This will:
   - Install dependencies (`npm install`)
   - Set the API base URL
   - Start the dev server

2. **Open the client in your browser**: [http://localhost:5173](http://localhost:5173)

## Production Build (optional)

- Run: `build_and_preview.bat`
- Open: [http://localhost:4173](http://localhost:4173)

## Configuration

- API base URL is read from env var `SERVER_BASE_URL`.  
  Defaults to `http://localhost:8080/S_Emulator_Server`.
- To change the server URL, edit the `SERVER_BASE_URL` variable in `start_dev.bat`

## Technology Stack

- **Framework**: React 18.2
- **Build Tool**: Vite 5.0
- **Language**: JavaScript (JSX)
- **Styling**: Pure CSS (no external UI libraries)

## Features Implemented

### ✅ Login Screen
- Works exactly the same as in the JavaFX application
- Follows all existing login requirements and logic
- Session-based authentication via the server

### ✅ Dashboard Screen
- Layout and appearance resembles the JavaFX client's dashboard
- **Active Users Panel**: Shows all online users with their program/execution counts
- **Available Programs Panel**: Displays all programs uploaded by JavaFX clients
  - Shows program statistics (instructions, functions, max depth, total runs, avg duration)
  - Click "Open Execution" to execute a program
- **Available Functions Panel**: Shows all helper functions from programs
- **Execution History Panel**: Shows execution history when a user is selected
- **Chat Panel**: Real-time chat with all users (JavaFX and Web clients)
  - Send and receive messages
  - Auto-scrolls to new messages
  - Shows message timestamps
  - Highlights your own messages
- **Real-time Updates**: Polls the server every 1 second for fresh data
- **View-Only**: No file upload capability (as specified)

### ✅ Execution Screen
- Similar look and behavior to JavaFX execution screen
- **Input Variables Panel**: Enter values for program inputs
- **Output Variables Panel**: Displays execution results
- **Execution Controls**: 
  - **Architecture Selection**: Choose from Generation I-IV (different credit costs)
  - **Credit Tracking**: Shows your current credits, validates before execution
  - **Expansion Degree**: Set expansion degree for program execution
- **Instructions Panel**: View instructions at different expansion levels
- **Functions Panel**: Lists all available functions
- **Real-time Status**: Shows execution status, cycles used, and remaining credits
- **Credit System**: 
  - Generation I: 5 credits + CPU cycles
  - Generation II: 100 credits + CPU cycles
  - Generation III: 500 credits + CPU cycles
  - Generation IV: 1000 credits + CPU cycles
- **Architecture Validation**: Prevents execution if program uses incompatible instructions

### ❌ Intentionally Not Supported (as per requirements)

- **No DEBUG mode** (Resume, Stop, etc.)
- **No command history table**
- **No highlight selection capabilities**
- **No file upload** (view-only dashboard data accumulated by JavaFX clients)
- **No bonus features from Exercise 3**

## How It Works

### API Communication
The web client communicates with the existing Tomcat server using the same REST APIs:

- `POST /login` - User authentication
- `GET /userslist` - Fetch active users
- `GET /programs` - Fetch available programs (includes functions)
- `GET /executionHistory?userId={id}` - Fetch execution history for a user
- `GET /credits` - Fetch current user's credit balance
- `GET /exec/open?target={name}` - Open program for execution
- `POST /exec/execute` - Execute program with inputs and architecture selection
- `POST /exec/updateInput` - Update input variable values
- `POST /exec/setDegree?value={n}` - Set expansion degree
- `GET /api/chat/messages?since={timestamp}` - Fetch chat messages
- `POST /api/chat/messages` - Send a chat message

### Proxy Configuration
The Vite dev server is configured to proxy all API requests to the Tomcat server, avoiding CORS issues:

```javascript
// vite.config.js proxies /login, /userslist, /programs, /exec, /api, /executionHistory
// to http://localhost:8080 (or VITE_SERVER_BASE_URL)
```

### Polling Strategy
- **Dashboard**: Polls users, programs, and credits every 1000ms (1 second)
- **Chat**: Polls for new messages every 500ms
- **Execution**: Polls execution state every 500ms when running

## Project Structure

```
S-Emulator-WebClient/
├── src/
│   ├── components/
│   │   ├── Login/
│   │   │   ├── Login.jsx
│   │   │   └── Login.css
│   │   ├── Dashboard/
│   │   │   ├── Dashboard.jsx
│   │   │   └── Dashboard.css
│   │   └── Execution/
│   │       ├── ExecutionScreen.jsx
│   │       └── ExecutionScreen.css
│   ├── App.jsx
│   ├── App.css
│   ├── main.jsx
│   └── index.css
├── index.html
├── vite.config.js
├── package.json
├── start_dev.bat          ← Double-click this to run
├── build_and_preview.bat  ← Optional: production build
└── README.md
```

## Troubleshooting

### Server Connection Issues
- **Error**: "Network error. Please ensure the server is running..."
- **Solution**: Make sure Tomcat is running at `http://localhost:8080/S_Emulator_Server`
- **Check**: Open `http://localhost:8080/S_Emulator_Server` in your browser to verify

### Port Already in Use
- **Error**: "Port 5173 is already in use"
- **Solution**: Close any other Vite dev servers or change the port in `vite.config.js`

### Dependencies Installation Fails
- **Error**: npm install fails
- **Solution**: 
  - Check internet connection
  - Clear npm cache: `npm cache clean --force`
  - Delete `node_modules` folder and try again

### Login Fails
- **Error**: Login unsuccessful even with valid username
- **Solution**: 
  - Verify Tomcat server is running
  - Check browser console for detailed error messages
  - Ensure server's session management is working

## Development Notes

### No Server Changes Required
This web client is **completely standalone** and requires **no changes** to the existing Tomcat server. It uses the same servlet endpoints that the JavaFX client uses.

### Session Management
The web client relies on the server's existing session management (cookies). Once logged in, the browser maintains the session automatically.

### Data Freshness
All data (users, programs, execution history) is fetched from the server in real-time. The web client has no local storage or caching beyond the current session.

### Browser Compatibility
Tested on modern browsers (Chrome, Firefox, Edge). Requires JavaScript enabled.

## URL to Open

**Development Mode**: [http://localhost:5173](http://localhost:5173)  
**Production Preview**: [http://localhost:4173](http://localhost:4173)

---

**Important**: This web client is designed to work alongside the JavaFX client. JavaFX clients upload programs, and the web client can view and execute them. Multiple users (JavaFX or web) can be logged in simultaneously.

