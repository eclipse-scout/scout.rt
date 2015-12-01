/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job.internal;

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.chain.IInvocationDecorator;
import org.eclipse.scout.rt.platform.chain.InvocationChain;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;

/**
 * Processor to fire a job lifecycle event.
 * <p>
 * Instances of this class are to be added to a {@link InvocationChain} to participate in the execution of a
 * {@link Callable}.
 *
 * @since 5.1
 */
public class FireJobLifecycleEventProcessor<RESULT> implements IInvocationDecorator<RESULT> {

  private final JobManager m_jobManager;
  private final JobEventType m_eventType;
  private final IFuture<RESULT> m_future;

  public FireJobLifecycleEventProcessor(final JobEventType eventType, final JobManager jobManager, final IFuture<RESULT> future) {
    m_jobManager = jobManager;
    m_eventType = eventType;
    m_future = future;
  }

  @Override
  public IUndecorator<RESULT> decorate() throws Exception {
    m_jobManager.fireEvent(new JobEvent(m_jobManager, m_eventType).withFuture(m_future));
    return null;
  }
}
