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
    boolean opened = super.open();
    if (opened) {
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
    return opened;
  }
}
