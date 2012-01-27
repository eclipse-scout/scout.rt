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
package org.eclipse.scout.rt.client.ui.form.fields.documentfield.eventdata;


public class SaveAsData {
  private String m_name;
  private String m_format;

  public SaveAsData() {
  }

  public SaveAsData(String name, String format) {
    m_name = name;
    m_format = format;
  }

  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }

  public String getFormat() {
    return m_format;
  }

  public void setFormat(String format) {
    m_format = format;
  }
}
