import { useState, useEffect } from 'react'
import './ExecutionScreen.css'

function ExecutionScreen({ currentUser, program, onBack }) {
  const [executionState, setExecutionState] = useState(null)
  const [instructions, setInstructions] = useState([])
  const [functions, setFunctions] = useState([])
  const [inputs, setInputs] = useState({})
  const [outputs, setOutputs] = useState({})
  const [degree, setDegree] = useState(1)
  const [isRunning, setIsRunning] = useState(false)
  const [error, setError] = useState('')
  const [expandedLevel, setExpandedLevel] = useState(0)
  const [selectedArchitecture, setSelectedArchitecture] = useState('GENERATION_I')
  const [userCredits, setUserCredits] = useState(0)

  // Architecture definitions (matching server-side enum)
  const architectures = [
    { name: 'GENERATION_I', displayName: 'Generation I', cost: 5 },
    { name: 'GENERATION_II', displayName: 'Generation II', cost: 100 },
    { name: 'GENERATION_III', displayName: 'Generation III', cost: 500 },
    { name: 'GENERATION_IV', displayName: 'Generation IV', cost: 1000 }
  ]

  useEffect(() => {
    // Open the program for execution
    openProgram()
  }, [program])

  useEffect(() => {
    if (isRunning) {
      const interval = setInterval(() => {
        pollExecutionState()
      }, 500)
      return () => clearInterval(interval)
    }
  }, [isRunning])

  const openProgram = async () => {
    try {
      // The ExecutionServlet expects 'target' parameter which is the program/function name
      const response = await fetch(`/exec/open?target=${encodeURIComponent(program.programName)}`, {
        credentials: 'include'
      })
      if (response.ok) {
        const data = await response.json()

        // The response is ExecutionStateDTO
        // Instructions come from data.instructions array
        if (data.instructions && Array.isArray(data.instructions)) {
          setInstructions(data.instructions)
        }

        // Function names come from data.functionNames array
        if (data.functionNames && Array.isArray(data.functionNames)) {
          setFunctions(data.functionNames.map(name => ({ name, type: 'Function' })))
        }

        // Initialize inputs AND outputs from allVariables
        const initialInputs = {}
        const initialOutputs = {}

        if (data.allVariables && Array.isArray(data.allVariables)) {
          data.allVariables.forEach(variable => {
            if (variable.type === 'Input') {
              initialInputs[variable.name] = variable.value || 0
            } else if (variable.type === 'Output' || variable.type === 'Work') {
              initialOutputs[variable.name] = variable.value || 0
            }
          })
        }

        // Also check inputVariables for backward compatibility
        if (data.inputVariables && Array.isArray(data.inputVariables)) {
          data.inputVariables.forEach(input => {
            initialInputs[input.name] = input.value || 0
          })
        }

        setInputs(initialInputs)
        setOutputs(initialOutputs)

        // Set initial degree from response
        if (data.currentDegree !== undefined) {
          setDegree(data.currentDegree)
        }

        // Extract user credits
        if (data.userCredits !== undefined) {
          setUserCredits(data.userCredits)
        }

        // Store the initial execution state
        setExecutionState(data)
      } else {
        const errorText = await response.text()
        setError('Failed to open program: ' + errorText)
      }
    } catch (err) {
      setError('Network error: ' + err.message)
    }
  }

  const pollExecutionState = async () => {
    try {
      // After execution, we need to check the state by fetching updated ExecutionStateDTO
      // The execution is synchronous, so we just need to get the current state
      const response = await fetch(`/exec/open?target=${encodeURIComponent(program.programName)}`, {
        credentials: 'include'
      })
      if (response.ok) {
        const data = await response.json()
        setExecutionState(data)

        // Update user credits
        if (data.userCredits !== undefined) {
          setUserCredits(data.userCredits)
        }

        // Extract outputs from allVariables (filter for output/work variables)
        if (data.allVariables && Array.isArray(data.allVariables)) {
          const outputValues = {}
          data.allVariables.forEach(variable => {
            // Include both Output and Work variables in outputs
            if (variable.type === 'Output' || variable.type === 'Work') {
              outputValues[variable.name] = variable.value
            }
          })
          setOutputs(outputValues)
        }
      }
    } catch (err) {
      console.error('Failed to poll execution state:', err)
    }
  }

  const handleInputChange = (name, value) => {
    setInputs(prev => ({ ...prev, [name]: value }))
  }

  const handleExecute = async () => {
    setError('')
    setOutputs({})

    // Get selected architecture cost
    const arch = architectures.find(a => a.name === selectedArchitecture)
    const architectureCost = arch ? arch.cost : 5

    // Validate user has enough credits (rough estimate - architecture cost + potential cycles)
    // The exact cost will be calculated server-side (architecture + actual CPU cycles)
    if (userCredits < architectureCost) {
      setError(`Insufficient credits! You have ${userCredits} credits, but ${arch.displayName} requires at least ${architectureCost} credits (+ CPU cycles).`)
      return
    }

    setIsRunning(true)

    try {
      // First, update input variables on the server
      for (const [name, value] of Object.entries(inputs)) {
        await fetch('/exec/updateInput', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          credentials: 'include',
          body: JSON.stringify({
            variableName: name,
            newValue: value.toString()
          })
        })
      }

      // Execute with the selected architecture
      const requestBody = {
        architecture: selectedArchitecture
      }

      const response = await fetch('/exec/execute', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify(requestBody)
      })

      if (response.ok) {
        const data = await response.json()

        // Check if there's a validation error
        if (data.validation && !data.validation.valid) {
          const incompatibleInstructions = data.validation.incompatibleInstructionIds || []
          setError(`Architecture validation failed: ${arch.displayName} cannot execute this program. Incompatible instructions at lines: ${incompatibleInstructions.join(', ')}`)
          setIsRunning(false)
          return
        }

        // Execution is synchronous, so get the updated state
        await pollExecutionState()
        setIsRunning(false)
      } else if (response.status === 402) {
        // HTTP 402 Payment Required - insufficient credits
        const errorText = await response.text()
        setError('Insufficient credits: ' + errorText)
        setIsRunning(false)
      } else {
        const errorText = await response.text()
        setError('Execution failed: ' + errorText)
        setIsRunning(false)
      }
    } catch (err) {
      setError('Network error: ' + err.message)
      setIsRunning(false)
    }
  }

  const handleDegreeChange = async (newDegree) => {
    setDegree(newDegree)
    try {
      // Use the setDegree endpoint with 'value' parameter
      const response = await fetch(`/exec/setDegree?value=${newDegree}`, {
        method: 'POST',
        credentials: 'include'
      })

      if (response.ok) {
        const data = await response.json()
        // Response is ExecutionStateDTO with updated instructions
        if (data.instructions) {
          setInstructions(data.instructions)
        }
        setExecutionState(data)
      }
    } catch (err) {
      console.error('Failed to set degree:', err)
    }
  }

  const handleLevelChange = async (level) => {
    // In the web client, we're simplifying this - the degree controls what instructions are shown
    // The server returns instructions at the current degree level
    setExpandedLevel(level)
    if (level !== degree) {
      await handleDegreeChange(level)
    }
  }

  return (
    <div className="execution-container">
      <header className="execution-header">
        <div className="header-left">
          <button onClick={onBack} className="back-btn">← Back to Dashboard</button>
          <h1>{program.programName}</h1>
        </div>
        <div className="header-right">
          <span className="user-badge">User: {currentUser}</span>
        </div>
      </header>

      {error && <div className="error-banner">{error}</div>}

      <div className="execution-content">
        <div className="left-panel">
          <div className="panel inputs-panel">
            <h2>Input Variables</h2>
            {Object.keys(inputs).length === 0 ? (
              <p className="empty-state">No inputs required</p>
            ) : (
              <div className="variables-list">
                {Object.entries(inputs).map(([name, value]) => (
                  <div key={name} className="variable-item">
                    <label>{name}:</label>
                    <input
                      type="text"
                      value={value}
                      onChange={(e) => handleInputChange(name, e.target.value)}
                      disabled={isRunning}
                      placeholder="Enter value"
                    />
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="panel outputs-panel">
            <h2>Output Variables</h2>
            {Object.keys(outputs).length === 0 ? (
              <p className="empty-state">No outputs yet</p>
            ) : (
              <div className="variables-list">
                {Object.entries(outputs).map(([name, value]) => (
                  <div key={name} className="variable-item">
                    <label>{name}:</label>
                    <div className="output-value">{value}</div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="panel controls-panel">
            <h2>Execution Controls</h2>

            <div className="credits-display">
              <span className="credits-label">Your Credits:</span>
              <span className="credits-value">{userCredits}</span>
            </div>

            <div className="control-item">
              <label>Architecture:</label>
              <select
                value={selectedArchitecture}
                onChange={(e) => setSelectedArchitecture(e.target.value)}
                disabled={isRunning}
                className="architecture-select"
              >
                {architectures.map(arch => (
                  <option key={arch.name} value={arch.name}>
                    {arch.displayName} ({arch.cost} credits)
                  </option>
                ))}
              </select>
              <div className="architecture-info">
                {architectures.find(a => a.name === selectedArchitecture)?.displayName}
                {' '}costs {architectures.find(a => a.name === selectedArchitecture)?.cost} credits + CPU cycles
              </div>
            </div>

            <div className="control-item">
              <label>Expansion Degree:</label>
              <select
                value={degree}
                onChange={(e) => handleDegreeChange(parseInt(e.target.value))}
                disabled={isRunning}
              >
                {[...Array(program.maxDegree || 5)].map((_, i) => (
                  <option key={i + 1} value={i + 1}>{i + 1}</option>
                ))}
              </select>
            </div>

            <button
              onClick={handleExecute}
              disabled={isRunning || userCredits < (architectures.find(a => a.name === selectedArchitecture)?.cost || 5)}
              className="execute-btn-large"
            >
              {isRunning ? 'Running...' : 'Execute Program'}
            </button>

            {executionState && (
              <div className="execution-status">
                <strong>Current Degree:</strong> {executionState.currentDegree} / {executionState.maxDegree}
                <br/>
                <strong>Cycles Used:</strong> {executionState.cycles}
                {executionState.debugging && (
                  <><br/><strong>Mode:</strong> Debug</>
                )}
              </div>
            )}
          </div>

          <div className="panel functions-panel">
            <h2>Functions ({functions.length})</h2>
            {functions.length === 0 ? (
              <p className="empty-state">No functions defined</p>
            ) : (
              <div className="functions-list">
                {functions.map((func, idx) => (
                  <div key={idx} className="function-item">
                    <strong>{func.name}</strong>
                    <span className="function-type">{func.type || 'Custom'}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="right-panel">
          <div className="panel instructions-panel">
            <div className="panel-header">
              <h2>Instructions ({instructions.length})</h2>
              <div className="level-control">
                <label>View Level:</label>
                <select
                  value={expandedLevel}
                  onChange={(e) => handleLevelChange(parseInt(e.target.value))}
                >
                  {[...Array(degree + 1)].map((_, i) => (
                    <option key={i} value={i}>Level {i}</option>
                  ))}
                </select>
              </div>
            </div>
            {instructions.length === 0 ? (
              <p className="empty-state">No instructions at this level</p>
            ) : (
              <div className="instructions-list">
                {instructions.map((instruction, idx) => (
                  <div
                    key={instruction.id || idx}
                    className={`instruction-item ${executionState?.highlightedInstructionId === instruction.id ? 'current' : ''}`}
                  >
                    <span className="instruction-number">{instruction.id || (idx + 1)}</span>
                    <span className="instruction-command">{instruction.type || instruction.instruction}</span>
                    {instruction.arguments && (
                      <span className="instruction-args">
                        ({instruction.arguments})
                      </span>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default ExecutionScreen

