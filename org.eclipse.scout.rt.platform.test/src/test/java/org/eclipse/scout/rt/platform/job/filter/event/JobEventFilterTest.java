/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job.filter.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.junit.Test;

public class JobEventFilterTest {

  @Test
  public void test1() {
    JobEventFilter filter = new JobEventFilter(JobEventType.JOB_STATE_CHANGED);

    assertTrue(filter.accept(new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED)));
    assertFalse(filter.accept(new JobEvent(mock(IJobManager.class), JobEventType.JOB_MANAGER_SHUTDOWN)));
    assertFalse(filter.accept(new JobEvent(mock(IJobManager.class), JobEventType.JOB_EXECUTION_HINT_ADDED)));
  }

  @Test
  public void test2() {
    JobEventFilter filter = new JobEventFilter(JobEventType.JOB_STATE_CHANGED, JobEventType.JOB_MANAGER_SHUTDOWN);

    assertTrue(filter.accept(new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED)));
    assertTrue(filter.accept(new JobEvent(mock(IJobManager.class), JobEventType.JOB_MANAGER_SHUTDOWN)));
    assertFalse(filter.accept(new JobEvent(mock(IJobManager.class), JobEventType.JOB_EXECUTION_HINT_ADDED)));
  }
}
