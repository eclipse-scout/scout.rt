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
package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.ThreadInfo;

/**
 * Processor to decorate the thread name of the worker thread during the time of executing a job.
 *
 * @since 5.1
 */
@Bean
public class ThreadNameDecorator implements ICallableDecorator {

  @Override
  public IUndecorator decorate() throws Exception {
    final ThreadInfo threadInfo = ThreadInfo.CURRENT.get();
    threadInfo.updateThreadName(IFuture.CURRENT.get().getJobInput().getThreadName(), null);

    return new IUndecorator() {

      @Override
      public void undecorate() {
        threadInfo.reset(); // Restore to the original thread name.
      }
    };
  }
}
