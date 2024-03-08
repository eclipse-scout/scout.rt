/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Call, ObjectModel} from '../index';

export interface CallModel extends ObjectModel<Call> {
  /**
   * Maximum number of retries to perform.
   * If {@link retryIntervals} are given, these intervals are used (from left to right). If maxRetries is > the number of intervals, the last value form {@link retryIntervals} is used for the remaining calls.
   * If no {@link retryIntervals} is given, {@link defaultRetryInterval} is used for all retries.
   * Use -1 to have unlimited retries.
   * Default is the number of {@link retryIntervals} or 0 if no intervals are given.
   */
  maxRetries?: number;
  /**
   * Delays in ms between retries (from left to right).
   * If no {@link maxRetries} is specified, only the retries specified in this interval are used. Then the call fails when the last interval from the array has been used.
   * If additionally to this interval the {@link maxRetries} is specified, only these number of retries are performed. If maxRetries is > the number of intervals, the last interval is used for the remaining calls.
   * Example: [100, 500, 500, 1000].
   * Default is no interval.
   */
  retryIntervals?: number[];
  /**
   * The default interval between retries in milliseconds. Used when {@link maxRetries} is given but no {@link retryIntervals}.
   * Default is 300.
   */
  defaultRetryInterval?: number;
  /**
   * Minimal assumed call duration (throttles consecutive calls) in milliseconds. Default is 500.
   */
  minCallDuration?: number;
  /**
   * Identifier for the type of call (default is 'call'), used to build the uniqueName
   */
  type?: string;
  /**
   * Identifier for the call, used to build the uniqueName
   */
  name?: string;
  /**
   * All log messages are prefixed with this string. It contains the uniqueName and the current state (e.g. callCounter)
   */
  logPrefix?: string;
}
