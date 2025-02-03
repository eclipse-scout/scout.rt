/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.filter.event;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventData;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.junit.Test;

public class JobEventFilterTest {

  @Test
  public void test1() {
    JobEventFilter filter = new JobEventFilter(JobEventType.JOB_STATE_CHANGED);

    assertTrue(filter.test(new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED, new JobEventData())));
    assertFalse(filter.test(new JobEvent(mock(IJobManager.class), JobEventType.JOB_MANAGER_SHUTDOWN, new JobEventData())));
    assertFalse(filter.test(new JobEvent(mock(IJobManager.class), JobEventType.JOB_EXECUTION_HINT_ADDED, new JobEventData())));
  }

  @Test
  public void test2() {
    JobEventFilter filter = new JobEventFilter(JobEventType.JOB_STATE_CHANGED, JobEventType.JOB_MANAGER_SHUTDOWN);

    assertTrue(filter.test(new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED, new JobEventData())));
    assertTrue(filter.test(new JobEvent(mock(IJobManager.class), JobEventType.JOB_MANAGER_SHUTDOWN, new JobEventData())));
    assertFalse(filter.test(new JobEvent(mock(IJobManager.class), JobEventType.JOB_EXECUTION_HINT_ADDED, new JobEventData())));
  }
}
