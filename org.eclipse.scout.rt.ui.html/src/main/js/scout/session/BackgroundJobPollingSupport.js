scout.BackgroundJobPollingStatus = {
  STOPPED: 'stopped',
  RUNNING: 'running',
  FAILURE: 'failure'
};

scout.BackgroundJobPollingSupport = function(enabled) {
  this._enabled = !!enabled;
  this._status = scout.BackgroundJobPollingStatus.STOPPED;
};

scout.BackgroundJobPollingSupport.prototype.enabled = function(enabled) {
  if (enabled !== undefined) {
    this._enabled = !!enabled;
  }
  return this._enabled;
};

scout.BackgroundJobPollingSupport.prototype.status = function(status) {
  if (status !== undefined) {
    this._status = status;
  }
  return this._status;
};

scout.BackgroundJobPollingSupport.prototype.setFailed = function() {
  this.status(scout.BackgroundJobPollingStatus.FAILURE);
};

scout.BackgroundJobPollingSupport.prototype.setRunning = function() {
  this.status(scout.BackgroundJobPollingStatus.RUNNING);
};

scout.BackgroundJobPollingSupport.prototype.setStopped = function() {
  this.status(scout.BackgroundJobPollingStatus.STOPPED);
};

scout.BackgroundJobPollingSupport.prototype.isFailed = function() {
  return this.status() === scout.BackgroundJobPollingStatus.FAILURE;
};

scout.BackgroundJobPollingSupport.prototype.isRunning = function() {
  return this.status() === scout.BackgroundJobPollingStatus.RUNNING;
};

scout.BackgroundJobPollingSupport.prototype.isStopped = function() {
  return this.status() === scout.BackgroundJobPollingStatus.STOPPED;
};
