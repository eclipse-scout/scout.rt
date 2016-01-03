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
package org.eclipse.scout.rt.server.scheduler.internal.visitor;

import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.server.scheduler.TickSignal;

/**
 * @deprecated will be removed in release 6.1; use {@link IJobManager} instead, which provides you support for triggered
 *             execution via Quartz schedule plans; see {@link JobInput#withExecutionTrigger(...)};
 */
@SuppressWarnings("deprecation")
@Deprecated
public class DefaultEvalVisitor implements IEvalVisitor {

  private TickSignal m_signal;
  private Object[] m_args;

  public DefaultEvalVisitor(TickSignal signal, Object[] args) {
    m_signal = signal;
    m_args = args;
  }

  @Override
  public Object[] getArgs() {
    return m_args;
  }

  @Override
  public TickSignal getSignal() {
    return m_signal;
  }

  @Override
  public boolean toBoolean(Object o) {
    Boolean b = TypeCastUtility.castValue(o, Boolean.class);
    if (b == null) {
      b = false;
    }
    return b;
  }

  @Override
  public int toInt(Object o) {
    Integer n = TypeCastUtility.castValue(o, Integer.class);
    if (n == null) {
      n = 0;
    }
    return n;
  }
}
