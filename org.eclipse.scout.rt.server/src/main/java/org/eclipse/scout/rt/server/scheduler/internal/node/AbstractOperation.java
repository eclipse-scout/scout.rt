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

import org.eclipse.scout.rt.server.scheduler.internal.visitor.IEvalVisitor;
import org.eclipse.scout.rt.server.scheduler.internal.visitor.IFormatVisitor;

public abstract class AbstractOperation extends AbstractNode implements INode {
  private INode m_leftCmd;
  private INode m_rightCmd;

  public AbstractOperation(INode leftCmd, INode rightCmd) {
    m_leftCmd = leftCmd;
    m_rightCmd = rightCmd;
  }

  public INode getLeftSide() {
    return m_leftCmd;
  }

  public INode getRightSide() {
    return m_rightCmd;
  }

  @Override
  public void format(IFormatVisitor v) {
    m_leftCmd.format(v);
    v.print(" ");
    formatOpImpl(v);
    v.print(" ");
    m_rightCmd.format(v);
  }

  @Override
  public Object eval(IEvalVisitor v) {
    return evalImpl(v, m_leftCmd.eval(v), m_rightCmd.eval(v));
  }

  public abstract void formatOpImpl(IFormatVisitor v);

  public abstract Object evalImpl(IEvalVisitor v, Object a, Object b);

}
