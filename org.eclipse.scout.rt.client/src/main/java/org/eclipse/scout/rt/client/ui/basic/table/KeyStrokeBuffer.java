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
package org.eclipse.scout.rt.client.ui.basic.table;

public class KeyStrokeBuffer {
  private final StringBuilder m_buf;
  private long m_lastActivity;
  private final long m_resetTimeoutMillis;

  public KeyStrokeBuffer(long resetTimeoutMillis) {
    m_buf = new StringBuilder();
    m_resetTimeoutMillis = resetTimeoutMillis;
    m_lastActivity = System.currentTimeMillis();
  }

  public void append(String s) {
    checkTimeout();
    m_buf.append(s);
    m_lastActivity = System.currentTimeMillis();
  }

  public String getText() {
    checkTimeout();
    return m_buf.toString();
  }

  private void checkTimeout() {
    if (System.currentTimeMillis() - m_lastActivity >= m_resetTimeoutMillis) {
      m_buf.setLength(0);
    }
  }
}
