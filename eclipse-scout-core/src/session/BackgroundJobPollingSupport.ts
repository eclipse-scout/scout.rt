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
import {BackgroundJobPollingStatus} from '../index';
import {BackgroundJobPollingStatusType} from './BackgroundJobPollingStatus';

export default class BackgroundJobPollingSupport {
  enabled: boolean;
  status: BackgroundJobPollingStatusType;

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
