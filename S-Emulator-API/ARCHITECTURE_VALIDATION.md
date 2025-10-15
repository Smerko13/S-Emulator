# Architecture Validation Feature

## Overview
This feature validates that a user's selected architecture is compatible with all instructions in a program before execution. If the user tries to execute a program with an architecture that's too low, an error message will be displayed and incompatible instructions will be marked in red.

## Components

### 1. ArchitectureValidationDTO
Located in `src/api/dto/ArchitectureValidationDTO.java`

This DTO contains validation results:
- `isValid`: Boolean indicating if the architecture is sufficient
- `errorMessage`: Human-readable error message
- `incompatibleInstructionIds`: List of instruction IDs that require a higher architecture
- `requiredArchitecture`: The minimum architecture needed to run the program
- `providedArchitecture`: The architecture the user selected

### 2. ExecuteProgramResponse (Modified)
Located in `src/api/dto/ExecuteProgramResponse.java`

Now includes:
- `executionId`: The ID of the execution (if successful)
- `validationError`: Architecture validation information (if validation fails)

### 3. InstructionDTO (Modified)
Located in `src/api/dto/InstructionDTO.java`

New fields added:
- `incompatibleArchitecture`: Boolean flag to mark instructions incompatible with current architecture
- `requiredArchitecture`: The minimum architecture required for this instruction

### 4. ArchitectureValidator (New Utility)
Located in `src/api/util/ArchitectureValidator.java`

Provides static methods:
- `validate()`: Validates all instructions against provided architecture
- `markIncompatibleInstructions()`: Marks instructions that are incompatible

## Usage Example

### Backend Service/Servlet Implementation

```java
// In your execute program endpoint
public ExecuteProgramResponse executeProgram(ExecuteProgramRequest request) {
    // Load the program and its instructions
    List<InstructionDTO> instructions = loadProgramInstructions(request.programId);
    
    // Validate architecture compatibility
    ArchitectureValidationDTO validation = ArchitectureValidator.validate(
        instructions, 
        request.architecture
    );
    
    // If validation fails, return error response
    if (!validation.isValid()) {
        return new ExecuteProgramResponse(validation);
    }
    
    // Validation passed, proceed with execution
    String executionId = startExecution(request);
    return new ExecuteProgramResponse(executionId);
}

// When loading program instructions, set required architecture for each
private List<InstructionDTO> loadProgramInstructions(String programId) {
    List<InstructionDTO> instructions = new ArrayList<>();
    
    // Load commands from database/engine
    for (Command cmd : program.getCommands()) {
        InstructionDTO instruction = new InstructionDTO(
            cmd.getId(), 
            cmd.getInstruction(), 
            cmd.getArguments()
        );
        
        // Set the required architecture based on the command
        instruction.setRequiredArchitecture(getRequiredArchitecture(cmd));
        instructions.add(instruction);
    }
    
    return instructions;
}

// Determine required architecture based on command type
private Architecture getRequiredArchitecture(Command cmd) {
    // Example logic - adjust based on your command definitions
    switch (cmd.getInstruction().toUpperCase()) {
        case "ADD":
        case "SUB":
        case "MOV":
            return Architecture.GENERATION_I;
        
        case "MUL":
        case "DIV":
        case "JMP":
            return Architecture.GENERATION_II;
        
        case "CALL":
        case "RET":
        case "PUSH":
        case "POP":
            return Architecture.GENERATION_III;
        
        case "VECTOR_ADD":
        case "MATRIX_MUL":
            return Architecture.GENERATION_IV;
        
        default:
            return Architecture.GENERATION_I;
    }
}
```

### Frontend Client Implementation

```javascript
// Execute program with architecture validation
async function executeProgram(programId, architecture) {
    const response = await fetch('/api/execute', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ programId, architecture })
    });
    
    const result = await response.json();
    
    // Check if there's a validation error
    if (result.validationError && !result.validationError.isValid) {
        // Display error message to user
        showError(result.validationError.errorMessage);
        
        // Mark incompatible instructions in red
        const incompatibleIds = result.validationError.incompatibleInstructionIds;
        highlightIncompatibleInstructions(incompatibleIds);
        
        return;
    }
    
    // Execution started successfully
    console.log('Execution started:', result.executionId);
}

// Highlight incompatible instructions in the UI
function highlightIncompatibleInstructions(instructionIds) {
    instructionIds.forEach(id => {
        const element = document.querySelector(`[data-instruction-id="${id}"]`);
        if (element) {
            element.classList.add('incompatible-architecture');
            element.style.color = 'red';
            element.style.backgroundColor = '#ffebee';
        }
    });
}

// When displaying instructions, check the incompatibleArchitecture flag
function renderInstruction(instruction) {
    const div = document.createElement('div');
    div.setAttribute('data-instruction-id', instruction.id);
    
    // Apply red styling if incompatible
    if (instruction.incompatibleArchitecture) {
        div.classList.add('incompatible-architecture');
        div.style.color = 'red';
        div.title = `Requires ${instruction.requiredArchitecture.displayName}`;
    }
    
    div.textContent = `${instruction.instruction} ${instruction.arguments}`;
    return div;
}
```

## Error Message Format

Example error message when validation fails:
```
Cannot execute program with Generation I. This program requires at least Generation III. 
5 instruction(s) are incompatible with your selected architecture.
```

## Integration Checklist

- [ ] Set `requiredArchitecture` for each instruction when loading programs
- [ ] Call `ArchitectureValidator.validate()` before starting execution
- [ ] Return validation errors in `ExecuteProgramResponse`
- [ ] Display error message in UI when validation fails
- [ ] Highlight incompatible instructions in red
- [ ] Show tooltip with required architecture on hover
- [ ] Clear incompatible highlights when architecture is changed

## Notes

- Instructions without a `requiredArchitecture` set default to `GENERATION_I`
- Architecture levels (in order): GENERATION_I < GENERATION_II < GENERATION_III < GENERATION_IV
- The `incompatibleArchitecture` flag is automatically set by the validator
- Frontend should display both the error message AND highlight the problematic instructions

