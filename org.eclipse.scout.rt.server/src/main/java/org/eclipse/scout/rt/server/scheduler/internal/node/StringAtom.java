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
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.scheduler.internal.visitor.IEvalVisitor;
import org.eclipse.scout.rt.server.scheduler.internal.visitor.IFormatVisitor;

/**
 * @deprecated will be removed in release 6.1; use {@link IJobManager} instead, which provides you support for triggered
 *             execution via Quartz schedule plans; see {@link JobInput#withExecutionTrigger(...)};
 */
@SuppressWarnings("deprecation")
@Deprecated
public class StringAtom extends AbstractNode implements INode {
  private String m_value;

  public StringAtom(String value) {
    m_value = value;
  }

  public String getValue() {
    return m_value;
  }

  @Override
  public void format(IFormatVisitor v) {
    String escText = "" + m_value;
    escText = escText.replaceAll("\"", "\\\\\"");
    escText = StringUtility.escapeWhitespace(escText);
    v.print("\"" + escText + "\"");
  }

  @Override
  public Object eval(IEvalVisitor v) {
    return m_value;
  }

}
