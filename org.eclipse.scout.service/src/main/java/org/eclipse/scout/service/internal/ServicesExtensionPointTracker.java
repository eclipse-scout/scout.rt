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
package org.eclipse.scout.service.internal;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.runtime.ExtensionPointTracker;
import org.eclipse.scout.service.CreateServiceImmediatelySchedulingRule;

/**
 * Extension point tracker that handles Scout services.
 */
public class ServicesExtensionPointTracker extends ExtensionPointTracker {

  public ServicesExtensionPointTracker(IExtensionRegistry registry, String extensionPointId, Listener listener) {
    super(registry, extensionPointId, listener);
  }

  @Override
  public boolean open() {
    // unsafe check (performance)
    if (isOpen()) {
      return false;
    }
    /* open extension point tracker within a job that is using a CreateServiceImmediatelySchedulingRule to ensure
     * that create-immediately services are not started before all IServiceInitializerFactory have been registered.
     * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=392799
     */
    final BooleanHolder openHolder = new BooleanHolder(false);
    Job openJob = new Job("Open " + ServicesExtensionPointTracker.class.getSimpleName()) {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        boolean b = ServicesExtensionPointTracker.super.open();
        openHolder.setValue(b);
        return Status.OK_STATUS;
      }
    };
    openJob.setRule(new CreateServiceImmediatelySchedulingRule());
    openJob.schedule();
    try {
      openJob.join();
    }
    catch (InterruptedException e) {
      //nop
    }

    if (openHolder.getValue()) {
      //wait for all "create immediately" services
      Job job = new Job("Wait for all 'create immediately' services") {
        @Override
        protected IStatus run(IProgressMonitor monitor) {
          return Status.OK_STATUS;
        }
      };
      job.setRule(new CreateServiceImmediatelySchedulingRule());
      job.schedule();
      try {
        job.join();
      }
      catch (InterruptedException e) {
        //nop
      }
    }
    return openHolder.getValue();
  }
}
