import { useState, useEffect } from 'react'
import Login from './components/Login/Login'
import Dashboard from './components/Dashboard/Dashboard'
import ExecutionScreen from './components/Execution/ExecutionScreen'
import './App.css'

function App() {
  const [currentUser, setCurrentUser] = useState(null)
  const [currentScreen, setCurrentScreen] = useState('login') // 'login', 'dashboard', 'execution'
  const [selectedProgram, setSelectedProgram] = useState(null)
  const [isCheckingSession, setIsCheckingSession] = useState(true) // Add loading state

  useEffect(() => {
    // Check if user is already logged in on mount
    checkSession()
  }, [])

  const checkSession = async () => {
    // Check if we have a stored username from a previous session
    const storedUsername = localStorage.getItem('currentUser')

    if (!storedUsername) {
      // No stored username - show login page
      setIsCheckingSession(false)
      console.log('No stored username, showing login page')
      return
    }

    // We have a stored username - verify the session is still valid on the server
    try {
      const response = await fetch('/executionHistory', {
        credentials: 'include'
      })

      if (response.ok) {
        // Session is valid - auto-login and go directly to dashboard
        setCurrentUser(storedUsername)
        setCurrentScreen('dashboard')
        setIsCheckingSession(false)
        console.log('Auto-logged in as:', storedUsername)
      } else {
        // Session expired - clear localStorage and show login
        localStorage.removeItem('currentUser')
        setIsCheckingSession(false)
        console.log('Session expired, showing login page')
      }
    } catch (err) {
      // Network error - clear stale data and show login page
      localStorage.removeItem('currentUser')
      setIsCheckingSession(false)
      console.log('Error checking session, showing login page')
    }
  }

  const handleLogin = (username) => {
    setCurrentUser(username)
    setCurrentScreen('dashboard')
    // Store username in localStorage so we can retrieve it when checking session
    localStorage.setItem('currentUser', username)
  }


  const handleOpenExecution = (program) => {
    setSelectedProgram(program)
    setCurrentScreen('execution')
  }

  const handleBackToDashboard = () => {
    setCurrentScreen('dashboard')
    setSelectedProgram(null)
  }

  return (
    <div className="app">
      {isCheckingSession ? (
        // Show nothing (or a loading spinner) while checking for existing session
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
          <p>Loading...</p>
        </div>
      ) : (
        <>
          {currentScreen === 'login' && (
            <Login onLogin={handleLogin} />
          )}
          {currentScreen === 'dashboard' && (
            <Dashboard
              currentUser={currentUser}
              onOpenExecution={handleOpenExecution}
            />
          )}
          {currentScreen === 'execution' && (
            <ExecutionScreen
              currentUser={currentUser}
              program={selectedProgram}
              onBack={handleBackToDashboard}
            />
          )}
        </>
      )}
    </div>
  )
}

export default App

