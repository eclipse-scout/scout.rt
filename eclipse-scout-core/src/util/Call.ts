/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, CallModel, InitModelOf, objects, ObjectWithType, scout, strings} from '../index';
import $ from 'jquery';

/**
 * Represents a robust "call" that, when it fails, is retried automatically for a specific
 * number of times, before failing ultimately. The call implementation must be provided
 * by a subclass by overriding the _callImpl() method.
 */
export abstract class Call implements CallModel, ObjectWithType {
  declare model: CallModel;

  objectType: string;
  retryIntervals: number[];
  minCallDuration: number;
  callCounter: number;
  maxRetries: number;
  deferred: JQuery.Deferred<any>;
  aborted: boolean;
  initialized: boolean;
  pendingCall: JQuery.Promise<any>;
  callTimeoutId: number;
  callStartTimestamp: number;
  type: string;
  name: string;
  uniqueName: string;
  logPrefix: string;
  result: any;

  constructor() {
    this.initialized = false;
    this.retryIntervals = [];
    this.maxRetries = 0;
    this.minCallDuration = 500;
    /**
     * Counts how many times this call was actually performed (normally, only 1 try is expected)
     */
    this.callCounter = 0;
    this.deferred = $.Deferred();
    this.aborted = false;
    this.pendingCall = null;
    this.callTimeoutId = null;
    this.callStartTimestamp = null;
    this.type = null;
    this.name = null;
    /**
     * Unique identifier of this call instance for logging and debugging purposes
     */
    this.uniqueName = null;
    this.logPrefix = '';
    this.result = null;
  }

  static GLOBAL_SEQ = 0;

  init(model: InitModelOf<this>) {
    $.extend(this, model);

    // Ensure "retryIntervals" is a valid array
    if (!objects.isNullOrUndefined(this.maxRetries)) {
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

  protected _checkInitialized() {
    if (!this.initialized) {
      throw new Error('Not initialized');
    }
  }

  protected _updateLogPrefix() {
    this.logPrefix = this.callCounter + '/' + (this.maxRetries + 1) + ' [' + this.uniqueName + '] ';
  }

  protected _resolve() {
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + '[RESOLVE]');
    this.deferred.resolve(...arrays.ensure(this.result));
  }

  protected _reject() {
    $.log.isTraceEnabled() && $.log.trace(this.logPrefix + '[REJECT]');
    this.deferred.reject(...arrays.ensure(this.result));
  }

  protected _setResult(...args: any[]) {
    this.result = args;
  }

  // ==================================================================================

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
  call(): JQuery.Promise<any> {
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

  protected _call() {
    if (this.aborted) {
      throw new Error('Call is aborted');
    }

    this.callTimeoutId = null;
    this.callStartTimestamp = Date.now();
    this.callCounter++;
    this._updateLogPrefix();

    this.pendingCall = this._callImpl()
      .always(() => {
        this.pendingCall = null;
      })
      .done(this._setResultDone.bind(this))
      .done(this._onCallDone.bind(this))
      .fail(this._setResultFail.bind(this))
      .fail(this._onCallFail.bind(this));
  }

  /**
   * Performs the actual request.
   */
  protected abstract _callImpl(): JQuery.Promise<any>;

  protected _setResultDone(...args: any[]) {
    this._setResult(...args);
  }

  protected _setResultFail(...args: any[]) {
    this._setResult(...args);
  }

  protected _onCallDone(...args: any[]) {
    // Call successful -> RESOLVE
    this._resolve();
  }

  protected _onCallFail(...args: any[]) {
    // Aborted? -> REJECT
    if (this.aborted) {
      $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'Call aborted');
      this._reject();
      return;
    }

    // Retry impossible? -> REJECT
    let nextInterval = this._nextRetryImpl(...args);
    if (typeof nextInterval !== 'number') {
      $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'No retries remaining');
      this._reject();
      return;
    }

    // Retry
    let callDuration = Date.now() - this.callStartTimestamp;
    let additionalDelay = Math.max(this.minCallDuration - callDuration, 0);
    let retryInterval = nextInterval + additionalDelay;
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
  protected _nextRetryImpl(...args: any[]): number | boolean {
    if (this.retryIntervals.length) {
      return this.retryIntervals.shift();
    }
    return false;
  }

  // ==================================================================================

  protected _abort() {
    this.aborted = true;

    // Abort while waiting for the next retry (there is no running call)
    if (this.callTimeoutId) {
      $.log.isTraceEnabled() && $.log.trace(this.logPrefix + 'Cancelled scheduled retry');
      clearTimeout(this.callTimeoutId);
      this.callTimeoutId = null;
      this._reject();
      return;
    }

    // Abort a running call if necessary
    this._abortImpl();
  }

  protected _abortImpl() {
    // This method MAY be overridden by a subclass.
  }
}
