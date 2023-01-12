/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BackgroundJobPollingStatus} from '../index';

export class BackgroundJobPollingSupport {
  enabled: boolean;
  status: BackgroundJobPollingStatus;

  constructor(enabled?: boolean) {
    this.enabled = !!enabled;
    this.status = BackgroundJobPollingStatus.STOPPED;
  }

  setFailed() {
    this.status = BackgroundJobPollingStatus.FAILURE;
  }

  setRunning() {
    this.status = BackgroundJobPollingStatus.RUNNING;
  }

  setStopped() {
    this.status = BackgroundJobPollingStatus.STOPPED;
  }

  isFailed(): boolean {
    return this.status === BackgroundJobPollingStatus.FAILURE;
  }

  isRunning(): boolean {
    return this.status === BackgroundJobPollingStatus.RUNNING;
  }

  isStopped(): boolean {
    return this.status === BackgroundJobPollingStatus.STOPPED;
  }
}
