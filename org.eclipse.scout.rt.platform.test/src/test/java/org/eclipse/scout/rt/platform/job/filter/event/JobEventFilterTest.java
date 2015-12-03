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
  public void test() {
    JobEventFilter filter = new JobEventFilter(JobEventType.ABOUT_TO_RUN, JobEventType.RESUMED);

    assertTrue(filter.accept(new JobEvent(mock(IJobManager.class), JobEventType.ABOUT_TO_RUN)));
    assertTrue(filter.accept(new JobEvent(mock(IJobManager.class), JobEventType.RESUMED)));
    assertFalse(filter.accept(new JobEvent(mock(IJobManager.class), JobEventType.DONE)));
    assertFalse(filter.accept(new JobEvent(mock(IJobManager.class), JobEventType.SHUTDOWN)));
  }
}
