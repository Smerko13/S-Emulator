import { useState, useEffect, useRef } from 'react'
import './Chat.css'

function Chat({ currentUser }) {
  const [messages, setMessages] = useState([])
  const [newMessage, setNewMessage] = useState('')
  const [error, setError] = useState('')
  const [nextSince, setNextSince] = useState(null)
  const messagesEndRef = useRef(null)

  useEffect(() => {
    // Initial load
    fetchMessages()

    // Poll for new messages every 500ms
    const interval = setInterval(() => {
      fetchMessages()
    }, 500)

    return () => clearInterval(interval)
  }, [nextSince])

  useEffect(() => {
    // Auto-scroll to bottom when new messages arrive
    scrollToBottom()
  }, [messages])

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  const fetchMessages = async () => {
    try {
      const url = nextSince
        ? `/api/chat/messages?since=${encodeURIComponent(nextSince)}`
        : '/api/chat/messages'

      const response = await fetch(url, {
        credentials: 'include'
      })

      if (response.ok) {
        const data = await response.json()

        if (data.messages && data.messages.length > 0) {
          setMessages(prev => [...prev, ...data.messages])
        }

        if (data.nextSince) {
          setNextSince(data.nextSince)
        }
      }
    } catch (err) {
      console.error('Failed to fetch messages:', err)
    }
  }

  const handleSendMessage = async (e) => {
    e.preventDefault()

    if (!newMessage.trim()) {
      return
    }

    if (newMessage.length > 1000) {
      setError('Message too long (max 1000 characters)')
      return
    }

    try {
      const response = await fetch('/api/chat/messages', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({
          message: newMessage.trim()
        })
      })

      if (response.ok) {
        setNewMessage('')
        setError('')
        // Message will appear in next poll
      } else if (response.status === 429) {
        setError('Sending messages too fast. Please wait a moment.')
      } else {
        const errorData = await response.json()
        setError(errorData.error || 'Failed to send message')
      }
    } catch (err) {
      setError('Network error: ' + err.message)
    }
  }

  const formatTime = (timestamp) => {
    const date = new Date(timestamp)
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  return (
    <div className="chat-container">
      <div className="chat-header">
        <h3>💬 Chat</h3>
        <span className="chat-count">{messages.length} messages</span>
      </div>

      <div className="chat-messages">
        {messages.length === 0 ? (
          <div className="chat-empty">No messages yet. Start the conversation!</div>
        ) : (
          messages.map((msg, idx) => (
            <div
              key={msg.messageId || idx}
              className={`chat-message ${msg.username === currentUser ? 'own-message' : ''}`}
            >
              <div className="message-header">
                <span className="message-user">{msg.username}</span>
                <span className="message-time">{formatTime(msg.timestamp)}</span>
              </div>
              <div className="message-content">{msg.message}</div>
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      <form onSubmit={handleSendMessage} className="chat-input-form">
        {error && <div className="chat-error">{error}</div>}
        <div className="chat-input-container">
          <input
            type="text"
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            placeholder="Type a message..."
            maxLength={1000}
            className="chat-input"
          />
          <button
            type="submit"
            disabled={!newMessage.trim()}
            className="chat-send-btn"
          >
            Send
          </button>
        </div>
        <div className="char-count">
          {newMessage.length}/1000
        </div>
      </form>
    </div>
  )
}

export default Chat

