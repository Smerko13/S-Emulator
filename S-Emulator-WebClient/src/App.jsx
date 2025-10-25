import { useState, useEffect } from 'react'
import Login from './components/Login/Login'
import Dashboard from './components/Dashboard/Dashboard'
import ExecutionScreen from './components/Execution/ExecutionScreen'
import './App.css'

function App() {
  const [currentUser, setCurrentUser] = useState(null)
  const [currentScreen, setCurrentScreen] = useState('login') // 'login', 'dashboard', 'execution'
  const [selectedProgram, setSelectedProgram] = useState(null)

  useEffect(() => {
    // Check if user is already logged in on mount
    checkSession()
  }, [])

  const checkSession = async () => {
    // The server uses sessions, so we just need to check if we're authenticated
    // We'll do this by trying to fetch the users list
    try {
      const response = await fetch('/userslist')
      if (response.ok) {
        // We're logged in, but we need to get our username
        // For now, we'll stay on login screen until explicit login
      }
    } catch (err) {
      console.log('Not logged in')
    }
  }

  const handleLogin = (username) => {
    setCurrentUser(username)
    setCurrentScreen('dashboard')
  }

  const handleLogout = () => {
    setCurrentUser(null)
    setCurrentScreen('login')
    setSelectedProgram(null)
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
      {currentScreen === 'login' && (
        <Login onLogin={handleLogin} />
      )}
      {currentScreen === 'dashboard' && (
        <Dashboard
          currentUser={currentUser}
          onLogout={handleLogout}
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
    </div>
  )
}

export default App

