# Quote Infinite Recursion Fix - Base Case Implementation

## Problem
When executing divide2.xml (x1=10, x2=3), the server hung in an infinite loop with the following stack trace pattern:
```
engine.Program.executeFunction(Program.java:612)
engine.commands.synthetic.types.Quote.execute(Quote.java:771)
engine.Program.executeProgram(Program.java:461)
engine.Program.executeFunction(Program.java:608)
engine.commands.synthetic.types.Quote.handleFunctionCall(Quote.java:935)
engine.commands.synthetic.types.Quote.resolveArgument(Quote.java:799)
engine.commands.synthetic.types.Quote.execute(Quote.java:766)
[repeating infinitely...]
```

## Root Cause
The infinite recursion occurred because the `execute()` method was trying to **recursively execute nested Quote expressions** that should have already been expanded during the initialization phase.

The call chain was:
1. `execute()` → calls `resolveArgument()` for nested function arguments
2. `resolveArgument()` → calls `handleFunctionCall()` for expressions like `(NOT,(EQUAL,z3,0))`
3. `handleFunctionCall()` → executes the nested function, which calls another Quote's `execute()`
4. Back to step 1 → **infinite recursion**

## The Real Issue
Quote has two distinct phases:
- **Expansion Phase** (during initialization): Nested Quotes are expanded into inline commands
- **Execution Phase** (during runtime): Only simple variable references should be resolved

The bug was that `execute()` was trying to handle nested Quote calls that should have already been expanded. This is fundamentally wrong because:
- Nested Quotes should be **expanded inline** during initialization
- They should **never be executed recursively** during runtime

## Solution: Implement a Base Case

Instead of adding a depth guard (which just masks the problem), we implemented a **proper base case**:

### Base Case Rule
**The `resolveArgument()` method refuses to handle nested function calls during execution.**

If it encounters a nested Quote expression starting with `(`, it throws an error:
```java
if (trimmed.startsWith("(")) {
    throw new IllegalStateException(
        "Attempted to resolve nested function call during execution: " + trimmed + 
        ". Nested Quotes should be expanded during initialization, not executed during runtime."
    );
}
```

### Re-entry Guard
Added a ThreadLocal flag to detect if we're already executing a Quote:
```java
private static ThreadLocal<Boolean> isExecuting = ThreadLocal.withInitial(() -> false);
```

In `execute()`:
```java
if (isExecuting.get()) {
    throw new StackOverflowError("Nested Quote execution detected. Possible infinite recursion in Quote calls.");
}
isExecuting.set(true);
try {
    // ...execution logic...
} finally {
    isExecuting.set(false);
}
```

## Why This is the Correct Fix

1. **Enforces Separation of Concerns**: Expansion happens during initialization, execution happens during runtime
2. **Fails Fast**: If nested Quotes aren't properly expanded, we get an immediate clear error message
3. **No Arbitrary Limits**: Unlike a depth counter, this doesn't impose artificial restrictions
4. **Reveals Real Bugs**: If this error triggers, it means the expansion logic has a bug, not the execution logic

## What This Means for divide2.xml

If the expansion logic is working correctly:
- ✅ All nested Quotes like `(NOT,(EQUAL,z3,0))` will be expanded into inline commands during initialization
- ✅ When the program executes, it will only process already-expanded commands
- ✅ The `execute()` method will only see simple variable references like `x1`, `z3`, not nested function calls
- ✅ No infinite recursion will occur

If the error still triggers:
- ❌ It means the expansion phase didn't properly handle some Quote
- ❌ We'll get a clear error message pointing to exactly which nested function wasn't expanded
- ❌ This helps us fix the actual root cause in the expansion logic

## Testing Instructions

1. **Restart the server** to load the updated Quote.java
2. **Test divide2.xml** with inputs: x1=10, x2=3
3. **Expected outcomes:**
   - **Best case**: Program executes successfully, y=3
   - **If expansion is incomplete**: Clear error message about which nested Quote wasn't expanded
   - **No more**: Silent infinite loops or server hangs

## Files Modified
- `Quote.java` - Added base case in `resolveArgument()` and re-entry guard in `execute()`

## Status
✅ Fix implemented with proper base case
✅ Compiled successfully
⏳ Awaiting testing on divide2.xml
