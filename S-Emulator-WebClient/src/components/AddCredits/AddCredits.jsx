import { useState } from 'react'
import './AddCredits.css'

function AddCredits({ currentUser, onCreditsAdded }) {
  const [amount, setAmount] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const handleAddCredits = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    const credits = parseInt(amount)
    if (isNaN(credits) || credits <= 0) {
      setError('Please enter a valid positive number')
      return
    }

    setLoading(true)

    try {
      const formData = new URLSearchParams()
      formData.append('userId', currentUser)
      formData.append('credits', credits.toString())

      const response = await fetch('/credits', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        credentials: 'include',
        body: formData.toString()
      })

      if (response.ok) {
        const data = await response.json()
        setSuccess(`Successfully added ${credits} credits! New balance: ${data.newCredits || 'N/A'}`)
        setAmount('')
        if (onCreditsAdded) {
          onCreditsAdded(data.newCredits)
        }
      } else {
        const errorText = await response.text()
        setError('Failed to add credits: ' + errorText)
      }
    } catch (err) {
      setError('Network error: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="add-credits-container">
      <h3>💰 Add Credits</h3>
      <form onSubmit={handleAddCredits} className="add-credits-form">
        <div className="credits-input-group">
          <input
            type="number"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="Enter amount"
            min="1"
            disabled={loading}
            className="credits-input"
          />
          <button
            type="submit"
            disabled={loading || !amount}
            className="add-credits-btn"
          >
            {loading ? 'Adding...' : 'Add Credits'}
          </button>
        </div>

        {error && <div className="credits-error">{error}</div>}
        {success && <div className="credits-success">{success}</div>}
      </form>
    </div>
  )
}

export default AddCredits

