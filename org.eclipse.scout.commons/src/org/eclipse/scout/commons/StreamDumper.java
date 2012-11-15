/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

public class StreamDumper extends Thread {
  private BufferedReader m_in;
  private StringWriter m_sw;

  public StreamDumper(InputStream in, StringWriter sw) {
    setName("StreamDumper");
    m_sw = sw;
    m_in = new BufferedReader(new InputStreamReader(in));
  }

  @Override
  public void run() {
    String line;
    try {
      while ((line = m_in.readLine()) != null) {
        m_sw.write(line + "\n");
      }
    }
    catch (EOFException e) {
    }
    catch (Exception e) {
      e.printStackTrace(new PrintWriter(m_sw, true));
    }
  }
}
