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
package org.eclipse.scout.rt.platform;

import java.util.List;

import org.eclipse.scout.rt.platform.internal.BeanImplementor;
import org.eclipse.scout.rt.platform.job.JobExceptionHandler;
import org.eclipse.scout.rt.platform.job.NullJobExceptionHandler;

public class PlatformListener implements IPlatformListener {

  @Override
  public void stateChanged(final PlatformEvent event) throws PlatformException {
    if (event.getState() == IPlatform.State.BeanContextPrepared) {
      registerNullJobExceptionHandler(event.getSource().getBeanContext());
    }
  }

  /**
   * TODO [dwi][abr]: Temporary workaround to register a bean that does not implement an interface.
   */
  private void registerNullJobExceptionHandler(final IBeanContext beanContext) {
    final List<IBean<JobExceptionHandler>> beans = beanContext.getBeans(JobExceptionHandler.class);
    for (final IBean<?> bean : beans) {
      beanContext.unregisterBean(bean);
    }

    // Register NOOP-JobExceptionHandler to ignore exceptions thrown by intention to test job manager's internals.
    beanContext.registerBean(new BeanImplementor<>(JobExceptionHandler.class), new NullJobExceptionHandler());
  }
}
