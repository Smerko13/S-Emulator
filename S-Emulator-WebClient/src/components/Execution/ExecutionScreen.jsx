import { useState, useEffect } from 'react'
import './ExecutionScreen.css'

function ExecutionScreen({ currentUser, program, onBack }) {
  const [executionState, setExecutionState] = useState(null)
  const [instructions, setInstructions] = useState([])
  const [functions, setFunctions] = useState([])
  const [inputs, setInputs] = useState({})
  const [outputs, setOutputs] = useState({})
  const [degree, setDegree] = useState(1)
  const [maxDegree, setMaxDegree] = useState(5) // Store actual max degree from server
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
    // Fetch credits separately since /exec/open doesn't include them
    fetchCredits()

    // Poll credits every 2 seconds to keep them updated
    const creditsInterval = setInterval(() => {
      fetchCredits()
    }, 2000)

    return () => clearInterval(creditsInterval)
  }, [program, currentUser])

  useEffect(() => {
    if (isRunning) {
      const interval = setInterval(() => {
        pollExecutionState()
      }, 500)
      return () => clearInterval(interval)
    }
  }, [isRunning])

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
            if (variable.type === 'Input' || variable.type === 'InputVariable') {
              initialInputs[variable.name] = variable.value || 0
            } else if (variable.type === 'Output' || variable.type === 'OutputVariable' ||
                       variable.type === 'Work' || variable.type === 'WorkVariable') {
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

        // Set max degree from response
        if (data.maxDegree !== undefined) {
          setMaxDegree(data.maxDegree)
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

        // Refresh user credits immediately after execution
        await fetchCredits()

        // Extract outputs from allVariables (filter for output/work variables)
        if (data.allVariables && Array.isArray(data.allVariables)) {
          const outputValues = {}
          data.allVariables.forEach(variable => {
            // Include both Output and Work variables in outputs
            // Handle both short form ('Output', 'Work') and class name form ('OutputVariable', 'WorkVariable')
            if (variable.type === 'Output' || variable.type === 'OutputVariable' ||
                variable.type === 'Work' || variable.type === 'WorkVariable') {
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
      // The server expects 'name' and 'value' as query parameters, NOT JSON body
      for (const [name, value] of Object.entries(inputs)) {
        console.log(`Updating input: ${name} = ${value}`)
        const updateResponse = await fetch(`/exec/updateInput?name=${encodeURIComponent(name)}&value=${encodeURIComponent(value)}`, {
          method: 'POST',
          credentials: 'include'
        })

        if (!updateResponse.ok) {
          const errorText = await updateResponse.text()
          console.error(`Failed to update input ${name}:`, errorText)
          setError(`Failed to update input ${name}: ${errorText}`)
          setIsRunning(false)
          return
        } else {
          const result = await updateResponse.text()
          console.log(`Input ${name} updated:`, result)
        }
      }

      console.log('All inputs updated, now executing...')

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

        // Debug: Log the execution response to see what we're getting
        console.log('Execution response:', data)
        console.log('All variables:', data.allVariables)
        console.log('Cycles:', data.cycles)

        // Check if there's a validation error
        if (data.validation && !data.validation.valid) {
          const incompatibleInstructions = data.validation.incompatibleInstructionIds || []
          setError(`Architecture validation failed: ${arch.displayName} cannot execute this program. Incompatible instructions at lines: ${incompatibleInstructions.join(', ')}`)
          setIsRunning(false)
          return
        }

        // The response IS an ExecutionStateDTO with all updated variables
        // Update the execution state directly from the response
        setExecutionState(data)

        // Update instructions if included in response
        if (data.instructions && Array.isArray(data.instructions)) {
          setInstructions(data.instructions)
        }

        // Extract and update ALL output and work variables from the execution result
        // Include ALL non-input variables (both from allVariables and inputVariables)
        const outputValues = {}

        // First, get all variables from allVariables
        if (data.allVariables && Array.isArray(data.allVariables)) {
          data.allVariables.forEach(variable => {
            // Include ALL non-input variables (Output, Work, and any other type)
            const varType = variable.type || ''
            if (!varType.toLowerCase().includes('input')) {
              outputValues[variable.name] = variable.value
              console.log(`Found variable: ${variable.name} = ${variable.value} (type: ${variable.type})`)
            }
          })
        }

        console.log('Output values to display:', outputValues)
        setOutputs(outputValues)

        // Refresh credits immediately
        await fetchCredits()
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
    setExpandedLevel(newDegree) // Keep expanded level in sync
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
            <h2>Output & Work Variables</h2>
            {Object.keys(outputs).length === 0 ? (
              <p className="empty-state">No outputs yet</p>
            ) : (
              <div className="variables-list scrollable">
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
              <label>Expansion Degree (View Level):</label>
              <select
                value={degree}
                onChange={(e) => handleDegreeChange(parseInt(e.target.value))}
                disabled={isRunning}
              >
                {[...Array(maxDegree + 1)].map((_, i) => (
                  <option key={i} value={i}>Level {i}</option>
                ))}
              </select>
              <div style={{ fontSize: '0.85rem', color: '#666', marginTop: '0.3rem' }}>
                Controls instruction expansion and view level
              </div>
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
                <div>
                  <strong>Current Degree:</strong> <span className="status-value">{executionState.currentDegree} / {executionState.maxDegree}</span>
                </div>
                <div>
                  <strong>Cycles Used:</strong> <span className="status-value">{executionState.cycles}</span>
                </div>
                {executionState.debugging && (
                  <div><strong>Mode:</strong> <span className="status-value">Debug</span></div>
                )}
              </div>
            )}
          </div>
        </div>

        <div className="right-panel">
          <div className="panel instructions-panel">
            <h2>Instructions ({instructions.length})</h2>
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
                    <span className="instruction-command">{instruction.instruction || instruction.text}</span>
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

