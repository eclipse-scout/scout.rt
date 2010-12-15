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

import java.io.File;

public class DocumentFile {
  private File m_file;
  private String m_format;

  public DocumentFile() {
  }

  public DocumentFile(File file, String format) {
    m_file = file;
    m_format = format;
  }

  public File getFile() {
    return m_file;
  }

  public void File(File file) {
    m_file = file;
  }

  public String getFormat() {
    return m_format;
  }

  public void setFormat(String format) {
    m_format = format;
  }
}
