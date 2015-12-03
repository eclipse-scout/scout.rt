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
package org.eclipse.scout.rt.server.jdbc.fixture;

import org.eclipse.scout.rt.platform.util.VerboseUtility;

public class VerboseMock {
  private final StringBuffer m_protocol;

  public VerboseMock(StringBuffer protocol) {
    m_protocol = protocol;
  }

  public void log(Class<?> c, String methodName, Object... args) {
    m_protocol.append(c.getSimpleName());
    m_protocol.append(".");
    m_protocol.append(methodName);
    m_protocol.append("(");
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        if (i > 0) {
          m_protocol.append(", ");
        }
        m_protocol.append(VerboseUtility.dumpObject(args[i]));
      }
    }
    m_protocol.append(")\n");
  }

  public StringBuffer getProtocol() {
    return m_protocol;
  }
}
