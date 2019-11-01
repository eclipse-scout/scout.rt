/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {objects} from '../index';
import {arrays} from '../index';
import {strings} from '../index';
import * as $ from 'jquery';
import {scout} from '../index';

/**
 * Represents a robust "call" that, when it fails, is retried automatically for a specific
 * number of times, before failing ultimately. The call implementation must be provided
 * by a subclass by overriding the _callImpl() method.
 */
export default class Call {

constructor() {
  // Delays in ms between retries (from left to right). The call eventually fails when this list gets empty.
  // Example: [100, 500, 500, 500]
  this.retryIntervals = [];

  // Minimal assumed call duration (throttles consecutive calls)
  this.minCallDuration = 500; // ms

  // Counts how many times this call was actually performed (normally, only 1 try is expected)
  this.callCounter = 0;

  this.deferred = $.Deferred();
  this.aborted = false;
  this.pendingCall = null;
  this.type = null; // Identifier for the type of call (default is 'call'), used to build the uniqueName
  this.name = null; // Identifier for the call, used to build the uniqueName
  this.uniqueName = null; // Unique identifier of this call instance for logging and debugging purposes
  this.logPrefix = ''; // All log messages are prefixed with this string. It contains the uniqueName and the current state (e.g. callCounter)
}

static GLOBAL_SEQ = 0;

init(model) {
  $.extend(this, model);

  // Ensure "retryIntervals" is a valid array
  if (typeof this.maxRetries === 'number') {
    this.retryIntervals = arrays.init(this.maxRetries, 0);
  } else {
    // Do not modify the passed value -> create a copy
    this.retryIntervals = (this.retryIntervals ? this.retryIntervals.slice() : []);
    // Remember initial number of retries (for logging)
    this.maxRetries = this.retryIntervals.length;
  }

  // Assign a unique name to the call to help distinguish different calls in the log
  this.uniqueName = scout.nvl(this.type, 'call') + '-' + (Call.GLOBAL_SEQ++) + strings.box(' ', this.name, '');

  this.initialized = true;
}

_checkInitialized() {
  if (!this.initialized) {
    throw new Error('Not initialized');
  }
}

_updateLogPrefix() {
  this.logPrefix = this.callCounter + '/' + (this.maxRetries + 1) + ' [' + this.uniqueName + '] ';
}

_resolve() {
  $.log.isTraceEnabled() && $.log.trace(this.logPrefix + '[RESOLVE]');
  this.deferred.resolve.apply(this.deferred, this.result);
}

_reject() {
  $.log.isTraceEnabled() && $.log.trace(this.logPrefix + '[REJECT]');
  this.deferred.reject.apply(this.deferred, this.result);
}

//==================================================================================

/**
 * Performs the call with retries.
 *
 * Returns a promise that is ...
 * ... RESOLVED when the call was successful (possibly after some retries).
 * ... REJECTED when the call failed and no more retries are possible.
 *
 *
 *     | (promise)
 *     |   ^
 *     v   |
 *   +--------+           +---------+           .---------------.  (yes)
 *   | call() | . . . . . | _call() | ------>  <    success?     > ------> [RESOLVE]
 *   +--------+           +---------+           '---------------'
 *                             ^                       |(no)
 *                             |                       |
 *                             |                       v
 *                             |                .---------------.  (yes)
 *                             |               <    aborted?     > ------> [REJECT]
 *                             |                '---------------'
 *                             |                       |(no)
 *                             |                       |
 *                             |                       v
 *                             |                .---------------.  (no)
 *                             |               < retry possible? > ------> [REJECT]
 *                             |                '---------------'
 *                             |                       |(yes)
 *                             |        sleep          |
 *                             +-------- %%% ----------+
 */
call() {
  this._checkInitialized();
  this._call();
  return this.deferred.promise();
}

/**
 * Aborts the call. If the request is currently running, it is aborted (interrupted).
 * If a retry is scheduled, that retry is cancelled.
 *
 * The promise returned by call() is REJECTED.
 */
abort() {
  this._checkInitialized();
  this._abort();
}

// ==================================================================================

_call() {
  if (this.aborted) {
    throw new Error('Call is aborted');
  }

  this.callTimeoutId = null;
  this.callStartTimestamp = Date.now();
  this.callCounter++;
  this._updateLogPrefix();

  this.pendingCall = this._callImpl()
    .always(function() {
      this.pendingCall = null;
      // Store the last callback's arguments. They will be used be _resolve() and _reject().
      // We could pass them through the _onCallX functions, but when a call is aborted while
      // it is only scheduled (setTimeout), we would not have any values to pass to _reject().
      this.result = objects.argumentsToArray(arguments);
    }.bind(this))
    .done(this._onCallDone.bind(this))
    .fail(this._onCallFail.bind(this));
}

/**
 * Performs the actual request.
 *
 * >>> This method MUST be implemented by a subclass. <<<
 */
_callImpl() {
  throw new Error('Missing implemention: _callImpl()');
}

_onCallDone() {
  // Call successful -> RESOLVE
  this._resolve();
}

_onCallFail() {
  // Aborted? -> REJECT
  if (this.aborted) {
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'Call aborted');
    this._reject();
    return;
  }

  // Retry impossible? -> REJECT
  var nextInterval = this._nextRetryImpl.apply(this, arguments);
  if (typeof nextInterval !== 'number') {
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'No retries remaining');
    this._reject();
    return;
  }

  // Retry
  var callDuration = Date.now() - this.callStartTimestamp;
  var additionalDelay = Math.max(this.minCallDuration - callDuration, 0);
  var retryInterval = nextInterval + additionalDelay;
  $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'Try again in ' + retryInterval + ' ms...');
  this.callTimeoutId = setTimeout(this._call.bind(this), retryInterval);
}

/**
 * Checks if the call can be retried. If a number is returned, a retry is performed
 * with a delay of the corresponding amount of milliseconds.
 *
 * All other values indicate that no retry must be performed. (It is recommended
 * to return 'false' or 'null' in this case.)
 *
 * >>> This method MAY be overridden by a subclass. <<<
 */
_nextRetryImpl() {
  if (this.retryIntervals.length) {
    return this.retryIntervals.shift();
  }
  return false;
}

//==================================================================================

_abort() {
  this.aborted = true;

  // Abort while waiting for the next retry (there is no running call)
  if (this.callTimeoutId) {
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'Cancelled scheduled retry');
    clearTimeout(this.callTimeoutId);
    this.callTimeoutId = null;
    this._reject();
    return;
  }

  // Abort a running call
  this._abortImpl();
}

/**
 * >>> This method MAY be overridden by a subclass. <<<
 */
_abortImpl() {
  if (this.pendingCall && typeof this.pendingCall.abort === 'function') {
    this.pendingCall.abort();
  }
}
}
