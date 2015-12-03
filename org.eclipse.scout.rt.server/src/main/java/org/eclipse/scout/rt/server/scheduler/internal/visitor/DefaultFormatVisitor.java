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

public class DefaultFormatVisitor implements IFormatVisitor {
  private StringBuffer m_buf;
  private String m_text;

  @Override
  public void start() {
    m_buf = new StringBuffer();
  }

  @Override
  public void end() {
    m_text = m_buf.toString();
    m_buf = null;
  }

  @Override
  public void print(String s) {
    m_buf.append(s);
  }

  @Override
  public String getText() {
    return m_text;
  }

}
