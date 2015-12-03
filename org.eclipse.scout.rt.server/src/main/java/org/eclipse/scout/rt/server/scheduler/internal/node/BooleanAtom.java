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

public class BooleanAtom extends AbstractNode implements INode {
  private Boolean m_value;

  public BooleanAtom(Boolean value) {
    m_value = value;
  }

  public Boolean getValue() {
    return m_value;
  }

  @Override
  public void format(IFormatVisitor v) {
    v.print(m_value.booleanValue() ? "true" : "false");
  }

  @Override
  public Object eval(IEvalVisitor v) {
    return m_value;
  }

}
