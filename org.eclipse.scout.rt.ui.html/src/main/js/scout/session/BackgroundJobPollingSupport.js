/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.BackgroundJobPollingStatus = {
  STOPPED: 'stopped',
  RUNNING: 'running',
  FAILURE: 'failure'
};

scout.BackgroundJobPollingSupport = function(enabled) {
  this.enabled = !!enabled;
  this.status = scout.BackgroundJobPollingStatus.STOPPED;
};

scout.BackgroundJobPollingSupport.prototype.setFailed = function() {
  this.status = scout.BackgroundJobPollingStatus.FAILURE;
};

scout.BackgroundJobPollingSupport.prototype.setRunning = function() {
  this.status = scout.BackgroundJobPollingStatus.RUNNING;
};

scout.BackgroundJobPollingSupport.prototype.setStopped = function() {
  this.status = scout.BackgroundJobPollingStatus.STOPPED;
};

scout.BackgroundJobPollingSupport.prototype.isFailed = function() {
  return (this.status === scout.BackgroundJobPollingStatus.FAILURE);
};

scout.BackgroundJobPollingSupport.prototype.isRunning = function() {
  return (this.status === scout.BackgroundJobPollingStatus.RUNNING);
};

scout.BackgroundJobPollingSupport.prototype.isStopped = function() {
  return (this.status === scout.BackgroundJobPollingStatus.STOPPED);
};
