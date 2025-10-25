import { useState } from 'react'
import './Login.css'

function Login({ onLogin }) {
  const [username, setUsername] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (!username.trim()) {
      setError('Username is required')
      return
    }

    setLoading(true)

    try {
      const formData = new URLSearchParams()
      formData.append('username', username.trim())

      const response = await fetch('/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        credentials: 'include',
        body: formData.toString()
      })

      if (response.ok) {
        const data = await response.json()
        if (data.status === 'ok' || data.username) {
          // New account created successfully
          localStorage.setItem('currentUser', username.trim())
          console.log('Login successful for:', username.trim())
          onLogin(username.trim())
        } else {
          setError('Login failed. Please try again.')
        }
      } else if (response.status === 409) {
        // 409 can mean two things:
        // 1. Username already exists in system (line 65-68)
        // 2. User already has active session (line 86-90)

        const errorData = await response.json().catch(() => ({ error: '' }))
        const errorMessage = errorData.error || ''

        if (errorMessage.includes('already exists in the system')) {
          // Username is already registered by someone else
          const uniqueUsername = username.trim() + '_' + Math.floor(Math.random() * 10000)
          setError(`Username "${username}" is already registered. Try "${uniqueUsername}".`)
        } else {
          // User already has an active session - just use it!
          // Get the username from localStorage (the existing session's username)
          const existingUser = localStorage.getItem('currentUser')

          if (existingUser) {
            // We know who is logged in - use that session
            console.log('Using existing session for:', existingUser)
            onLogin(existingUser)
          } else {
            // We have a session but don't know the username
            // Store the attempted username and use the session
            localStorage.setItem('currentUser', username.trim())
            console.log('Using existing session, assuming username:', username.trim())
            onLogin(username.trim())
          }
        }
      } else {
        const errorText = await response.text()
        setError(errorText || 'Login failed. Please try again.')
      }
    } catch (err) {
      setError('Network error. Please ensure the server is running at http://localhost:8080/S_Emulator_Server')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-container">
      <div className="login-box">
        <h1>S-Emulator Web Client</h1>
        <p className="login-subtitle">Create Account</p>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">Username:</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter a unique username"
              disabled={loading}
              autoFocus
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" disabled={loading}>
            {loading ? 'Creating Account...' : 'Create Account & Login'}
          </button>
        </form>

        <div className="login-info">
          <p>⚠️ Server: http://localhost:8080/S_Emulator_Server</p>
          <p style={{ fontSize: '12px', marginTop: '10px', color: '#666' }}>
            <strong>Important:</strong> Each username can only be registered once (until server restart).
            Use unique names like "alice", "bob_123", "test_user", etc.
          </p>
          <p style={{ fontSize: '11px', marginTop: '5px', color: '#888' }}>
            <strong>Note:</strong> Browser tabs share the same session. For multiple users, use incognito windows.
          </p>
        </div>
      </div>
    </div>
  )
}

export default Login

