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
package org.eclipse.scout.rt.server.scheduler.internal.node;

import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.server.scheduler.internal.visitor.IEvalVisitor;
import org.eclipse.scout.rt.server.scheduler.internal.visitor.IFormatVisitor;

/**
 * always returns the referenced value except the method getReference() return the reference itself
 *
 * @deprecated will be removed in release 6.1; use {@link IJobManager} instead, which provides you support for triggered
 *             execution via Quartz schedule plans; see {@link JobInput#withExecutionTrigger(...)};
 */
@SuppressWarnings("deprecation")
@Deprecated
public class ArgRef extends AbstractNode implements INode {
  private int m_index;

  public ArgRef(int index) {
    m_index = index;
  }

  public int getIndex() {
    return m_index;
  }

  @Override
  public void format(IFormatVisitor v) {
    v.print("arg" + m_index);
  }

  @Override
  public Object eval(IEvalVisitor v) {
    Object[] args = v.getArgs();
    if (args != null && args.length > m_index) {
      return args[m_index];
    }
    else {
      return null;
    }
  }

}
