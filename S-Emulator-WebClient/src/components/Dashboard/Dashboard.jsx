import { useState, useEffect } from 'react'
import Chat from '../Chat/Chat'
import AddCredits from '../AddCredits/AddCredits'
import './Dashboard.css'

function Dashboard({ currentUser, onLogout, onOpenExecution }) {
  const [users, setUsers] = useState([])
  const [programs, setPrograms] = useState([])
  const [functions, setFunctions] = useState([])
  const [selectedUser, setSelectedUser] = useState(null)
  const [executionHistory, setExecutionHistory] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [userCredits, setUserCredits] = useState(0)

  useEffect(() => {
    // Start polling for users and programs
    fetchUsers()
    fetchPrograms()
    fetchCredits()

    const interval = setInterval(() => {
      fetchUsers()
      fetchPrograms()
      fetchCredits()
    }, 1000) // Poll every second

    return () => clearInterval(interval)
  }, [])

  useEffect(() => {
    if (selectedUser) {
      fetchExecutionHistory(selectedUser)
    }
  }, [selectedUser])

  const fetchUsers = async () => {
    try {
      const response = await fetch('/userslist', {
        credentials: 'include'
      })
      if (response.ok) {
        const data = await response.json()
        setUsers(data)
        setError('')
      }
    } catch (err) {
      setError('Failed to fetch users')
    } finally {
      setLoading(false)
    }
  }

  const fetchPrograms = async () => {
    try {
      const response = await fetch('/programs', {
        credentials: 'include'
      })
      if (response.ok) {
        const data = await response.json()
        setPrograms(data.programs || [])
        setFunctions(data.functions || [])
      }
    } catch (err) {
      console.error('Failed to fetch programs:', err)
    }
  }

  const fetchCredits = async () => {
    try {
      const response = await fetch(`/credits?userId=${encodeURIComponent(currentUser)}`, {
        credentials: 'include'
      })
      if (response.ok) {
        const data = await response.json()
        if (data.credits !== undefined) {
          setUserCredits(data.credits)
        }
      }
    } catch (err) {
      console.error('Failed to fetch credits:', err)
    }
  }

  const fetchExecutionHistory = async (userId) => {
    try {
      const response = await fetch(`/executionHistory?userId=${encodeURIComponent(userId)}`, {
        credentials: 'include'
      })
      if (response.ok) {
        const data = await response.json()
        setExecutionHistory(Array.isArray(data) ? data : [])
      }
    } catch (err) {
      console.error('Failed to fetch execution history:', err)
    }
  }

  const handleUserClick = (userId) => {
    setSelectedUser(userId === selectedUser ? null : userId)
  }

  const handleOpenExecution = (program) => {
    onOpenExecution(program)
  }

  const handleCreditsAdded = (newCredits) => {
    setUserCredits(newCredits)
  }

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <h1>S-Emulator Dashboard</h1>
        <div className="header-info">
          <span className="credits-badge">Credits: <strong>{userCredits}</strong></span>
          <span className="user-badge">Logged in as: <strong>{currentUser}</strong></span>
          <button onClick={onLogout} className="logout-btn">Logout</button>
        </div>
      </header>

      {error && <div className="error-banner">{error}</div>}

      <div className="dashboard-content">
        <div className="left-column">
          <div className="panel add-credits-panel">
            <AddCredits currentUser={currentUser} onCreditsAdded={handleCreditsAdded} />
          </div>

          <div className="panel users-panel">
            <h2>Active Users ({users.length})</h2>
          {loading ? (
            <p>Loading users...</p>
          ) : users.length === 0 ? (
            <p className="empty-state">No users online</p>
          ) : (
            <div className="users-list">
              {users.map((user) => (
                <div
                  key={user.username}
                  className={`user-item ${selectedUser === user.username ? 'selected' : ''}`}
                  onClick={() => handleUserClick(user.username)}
                >
                  <div className="user-info">
                    <span className="user-name">{user.username}</span>
                    <span className="user-stats">
                      {user.programs} program{user.programs !== 1 ? 's' : ''} |
                      {user.functions} function{user.functions !== 1 ? 's' : ''} |
                      {user.executions} execution{user.executions !== 1 ? 's' : ''}
                    </span>
                    <span className="user-credits">
                      Credits: {user.creditsAvailable} | Used: {user.creditsUsed}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
          </div>
        </div>

        <div className="panel programs-panel">
          <h2>Available Programs ({programs.length})</h2>
          {programs.length === 0 ? (
            <p className="empty-state">No programs available. Upload a program using the JavaFX client.</p>
          ) : (
            <div className="programs-grid">
              {programs.map((program, idx) => (
                <div key={idx} className="program-card">
                  <div className="program-header">
                    <h3>{program.programName}</h3>
                    <span className="program-owner">by {program.uploaderName}</span>
                  </div>
                  <div className="program-stats">
                    <div className="stat">
                      <span className="stat-label">Instructions:</span>
                      <span className="stat-value">{program.numOfInstructions}</span>
                    </div>
                    <div className="stat">
                      <span className="stat-label">Max Degree:</span>
                      <span className="stat-value">{program.maxDegree}</span>
                    </div>
                    <div className="stat">
                      <span className="stat-label">Total Runs:</span>
                      <span className="stat-value">{program.numOfExecutions}</span>
                    </div>
                    <div className="stat">
                      <span className="stat-label">Avg Cost:</span>
                      <span className="stat-value">{program.avgCreditCost.toFixed(2)}</span>
                    </div>
                  </div>
                  <button
                    className="execute-btn"
                    onClick={() => handleOpenExecution(program)}
                  >
                    Open Execution
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="panel functions-panel">
          <h2>Available Functions ({functions.length})</h2>
          {functions.length === 0 ? (
            <p className="empty-state">No helper functions available</p>
          ) : (
            <div className="functions-grid">
              {functions.map((func, idx) => (
                <div key={idx} className="function-card">
                  <div className="function-header">
                    <h3>{func.functionName}</h3>
                    <span className="function-program">from {func.associatedProgram}</span>
                    <span className="function-user">by {func.associatedUser}</span>
                  </div>
                  <div className="function-stats">
                    <div className="stat">
                      <span className="stat-label">Instructions:</span>
                      <span className="stat-value">{func.numOfInstructions}</span>
                    </div>
                    <div className="stat">
                      <span className="stat-label">Max Degree:</span>
                      <span className="stat-value">{func.maxDegree}</span>
                    </div>
                  </div>
                  <button
                    className="execute-btn"
                    onClick={() => handleOpenExecution({
                      programName: func.functionName,
                      uploaderName: func.associatedUser,
                      maxDegree: func.maxDegree
                    })}
                  >
                    Open Function
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {selectedUser && (
          <div className="panel execution-history-panel">
            <h2>Execution History for {selectedUser}</h2>
            {executionHistory.length === 0 ? (
              <p className="empty-state">No execution history for this user</p>
            ) : (
              <div className="history-list">
                {executionHistory.map((execution, idx) => (
                  <div key={idx} className="history-item">
                    <div className="history-header">
                      <strong>{execution.programName}</strong>
                      <span className="history-time">{new Date(execution.timestamp).toLocaleString()}</span>
                    </div>
                    <div className="history-details">
                      <span>Duration: {execution.duration}ms</span>
                      <span>Steps: {execution.steps}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        <div className="panel chat-panel">
          <Chat currentUser={currentUser} />
        </div>
      </div>
    </div>
  )
}

export default Dashboard

