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
        credentials: 'include', // Important: Send cookies with request
        body: formData.toString()
      })

      if (response.ok) {
        // Parse the JSON response to confirm login
        const data = await response.json()
        if (data.status === 'ok' || data.username) {
          // Login successful - session is now established
          console.log('Login successful, session established for:', username.trim())
          onLogin(username.trim())
        } else {
          setError('Login failed. Please try again.')
        }
      } else if (response.status === 409) {
        // Username already exists - suggest adding a unique suffix
        const uniqueUsername = username.trim() + '_' + Date.now().toString().slice(-4)
        setError(`Username "${username}" already exists. Try a unique name like "${uniqueUsername}"`)
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
        <p className="login-subtitle">Login to continue</p>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">Username:</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter your username"
              disabled={loading}
              autoFocus
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <div className="login-info">
          <p>⚠️ Make sure the Tomcat server is running at http://localhost:8080/S_Emulator_Server</p>
        </div>
      </div>
    </div>
  )
}

export default Login

