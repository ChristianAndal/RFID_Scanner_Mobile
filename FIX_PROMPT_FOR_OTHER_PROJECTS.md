# Fix Prompt: RFID Scanner Device Shutdown After Scanning

## Problem Description
The RFID scanner device shuts down permanently after scanning starts (approximately 5 seconds). The device doesn't just disconnect - it completely powers off/shuts down.

## Root Cause
The issue is caused by a buggy disconnect timer that triggers a disconnect right after scanning stops, which causes the device hardware to shut down. There are three critical bugs:

1. **Multiple Timer Bug**: The disconnect timer doesn't cancel existing timers before starting new ones, causing multiple timers to run simultaneously
2. **Race Condition Bug**: The timer can disconnect while scanning is active or immediately after scanning stops
3. **Timing Logic Bug**: The timer decrements the count AFTER checking if it should disconnect, causing immediate disconnects

## Required Fixes

### Fix 1: Cancel Existing Timer Before Starting New One

**Location**: In the method that starts the disconnect timer (usually `startDisconnectTimer()`)

**Before**:
```java
private void startDisconnectTimer(long time) {
    timeCountCur = time;
    timerTask = new DisconnectTimerTask();
    mDisconnectTimer.schedule(timerTask, 0, period);
}
```

**After**:
```java
private void startDisconnectTimer(long time) {
    // CRITICAL FIX: Cancel any existing timer first to prevent multiple timers running
    cancelDisconnectTimer();
    
    timeCountCur = time;
    timerTask = new DisconnectTimerTask();
    mDisconnectTimer.schedule(timerTask, 0, period);
}
```

### Fix 2: Fix Timer Task Logic - Decrement First, Never Disconnect During Scanning

**Location**: In the `DisconnectTimerTask` class (usually a TimerTask subclass)

**Before**:
```java
private class DisconnectTimerTask extends TimerTask {
    @Override
    public void run() {
        Log.e(TAG, "timeCountCur = " + timeCountCur);
        Message msg = mHandler.obtainMessage(RUNNING_DISCONNECT_TIMER, timeCountCur);
        mHandler.sendMessage(msg);
        if(isScanning) {
            resetDisconnectTime();
        } else if (timeCountCur <= 0){
            disconnect(true);
        }
        timeCountCur -= period;  // BUG: Decrements AFTER check
    }
}
```

**After**:
```java
private class DisconnectTimerTask extends TimerTask {
    @Override
    public void run() {
        Log.e(TAG, "timeCountCur = " + timeCountCur + ", isScanning = " + isScanning);
        Message msg = mHandler.obtainMessage(RUNNING_DISCONNECT_TIMER, timeCountCur);
        mHandler.sendMessage(msg);
        
        // CRITICAL FIX: Decrement first to prevent immediate disconnect
        timeCountCur -= period;
        
        // CRITICAL FIX: Never disconnect while scanning or right after scanning stops
        if (isScanning) {
            // Reset timer while scanning to prevent disconnection
            resetDisconnectTime();
        } else if (timeCountCur <= 0) {
            // Only disconnect if NOT scanning and timer has expired
            disconnect(true);
        }
    }
}
```

### Fix 3: Reset Timer When Scanning Stops

**Location**: In the method that stops scanning (usually `stop()` or `stopInventory()` method in the scanning fragment/activity)

**Before**:
```java
private void stop() {
    cancelInventoryTask();
    mContext.isScanning = false;
}
```

**After**:
```java
private void stop() {
    cancelInventoryTask();
    mContext.isScanning = false;
    // CRITICAL FIX: Reset disconnect timer when scanning stops
    // This prevents immediate disconnect after scanning which causes device shutdown
    mContext.resetDisconnectTime();
}
```

## Key Points to Look For

1. **Timer Management**: Look for `Timer`, `TimerTask`, `schedule()` calls
2. **Disconnect Logic**: Look for `disconnect()` calls in timer tasks
3. **Scanning State**: Look for `isScanning` boolean variable
4. **Timer Variables**: Look for variables like `timeCountCur`, `disconnectTime`, `timerTask`
5. **Scanning Stop Methods**: Look for methods that set `isScanning = false`

## Testing After Fix

1. Connect the RFID device
2. Start scanning
3. Stop scanning
4. Verify device does NOT shut down
5. Verify device stays connected
6. Test with disconnect timer enabled (if applicable)

## Expected Behavior After Fix

- Device should NOT shut down after scanning stops
- Device should remain connected after scanning
- Disconnect timer should only trigger when:
  - Timer has expired (timeCountCur <= 0)
  - Device is NOT currently scanning
  - Device is NOT in the process of stopping a scan

## Notes

- The shutdown is likely a hardware protection mechanism triggered by improper disconnection during active scanning operations
- These fixes prevent the disconnect from happening at the wrong time
- The fixes are defensive - they ensure the timer never disconnects during critical operations

