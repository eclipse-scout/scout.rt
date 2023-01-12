/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
  public IUndecorator decorate() {
    final ThreadInfo threadInfo = ThreadInfo.CURRENT.get();
    threadInfo.updateThreadName(IFuture.CURRENT.get().getJobInput().getThreadName(), null);

    // Restore to the original thread name.
    return threadInfo::reset;
  }
}
