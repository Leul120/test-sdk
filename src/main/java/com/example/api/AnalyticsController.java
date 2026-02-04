
[L245] @GetMapping("/users/runtime-errors/{errorType}")
    public ResponseEntity<String> testRuntimeErrors(@PathVariable String errorType) {
        // ... existing code ...
        try {
            if (data.getErrorType().equals(errorType)) {
                log.error("ERROR_THRESHOLD triggered for error type: {}", errorType, "Detailed error information: {}", data.getDetailedError());
                return ResponseEntity.status(500).body("Internal Server Error");
            } else {
                return ResponseEntity.ok("OK");
            }
        } catch (Exception e) {
            log.error("Exception caught during runtime error handling: {}", e);
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }