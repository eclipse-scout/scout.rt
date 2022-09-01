/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
export default interface CallModel {
  /**
   * Delays in ms between retries (from left to right). The call eventually fails when this list gets empty.
   * Example: [100, 500, 500, 500].
   */
  retryIntervals?: number[];
  /**
   * Maximum number of retries to perform with default interval. Alternatively use {@link retryIntervals} to specify specific intervals for each retry.
   */
  maxRetries?: number;
  /**
   * Minimal assumed call duration (throttles consecutive calls) in milliseconds
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
