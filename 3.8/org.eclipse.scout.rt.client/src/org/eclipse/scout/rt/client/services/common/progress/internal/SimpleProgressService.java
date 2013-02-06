/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.progress.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.common.progress.ISimpleProgress;
import org.eclipse.scout.rt.client.services.common.progress.ISimpleProgressService;
import org.eclipse.scout.service.AbstractService;

/**
 * Thread-safe
 */
@Priority(-1)
public class SimpleProgressService extends AbstractService implements ISimpleProgressService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SimpleProgressService.class);

  public SimpleProgressService() {
  }

  @Override
  public ISimpleProgress addProgress(String name) {
    final Holder<SimpleProgress> monitorHolder = new Holder<SimpleProgress>(SimpleProgress.class);
    Job job = new Job(name) {
      private boolean m_done;
      private final Object m_doneLock = new Object();

      @Override
      protected IStatus run(IProgressMonitor monitor) {
        synchronized (monitorHolder) {
          SimpleProgress p = new SimpleProgress(this, monitor) {
            @Override
            public void done() {
              super.done();
              synchronized (m_doneLock) {
                m_done = true;
                m_doneLock.notify();
              }
            }
          };
          monitorHolder.setValue(p);
          monitorHolder.notifyAll();
        }
        synchronized (m_doneLock) {
          while (!monitor.isCanceled() && !m_done) {
            try {
              m_doneLock.wait();
            }
            catch (InterruptedException e) {
              // nop
            }
          }
        }
        return Status.OK_STATUS;
      }
    };
    synchronized (monitorHolder) {
      job.schedule();
      try {
        monitorHolder.wait(10000L);
        if (monitorHolder.getValue() != null) {
          return monitorHolder.getValue();
        }
      }
      catch (InterruptedException e) {
      }
      return new SimpleProgress(job, new NullProgressMonitor());
    }
  }

  @Override
  public void removeProgress(ISimpleProgress monitor) {
    if (monitor != null) {
      monitor.done();
    }
  }

}
