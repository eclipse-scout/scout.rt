// Info: dummy module for missing imports/constants from original Scout
// should not be required anymore in final implementation
export const BackgroundJobPollingStatus = {
  STOPPED: 'stopped',
  RUNNING: 'running',
  FAILURE: 'failure'
};

export const FileInput = {
  DEFAULT_MAXIMUM_UPLOAD_SIZE: 50 * 1024 * 1024 // 50 MB
};

export const JsonResponseError = {
  STARTUP_FAILED: 5,
  SESSION_TIMEOUT: 10,
  UI_PROCESSING: 20,
  UNSAFE_UPLOAD: 30,
  VERSION_MISMATCH: 40
};

export const Severity = {
  OK: 0x01,
  INFO: 0x100,
  WARNING: 0x10000,
  ERROR: 0x1000000
};
