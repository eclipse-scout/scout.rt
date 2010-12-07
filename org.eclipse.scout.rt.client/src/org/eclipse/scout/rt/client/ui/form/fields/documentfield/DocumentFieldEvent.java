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
package org.eclipse.scout.rt.client.ui.form.fields.documentfield;

import java.io.File;
import java.util.EventObject;

public class DocumentFieldEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_SAVE_AS = 10;

  public static final int TYPE_INSERT_TEXT = 11;

  public static final int TYPE_TOGGLE_RIBBONS = 20;

  public static final int TYPE_AUTORESIZE_DOCUMENT = 30;

  private final int m_type;
  private String m_text;
  private File m_saveAsFile;
  private String m_saveAsType;

  public DocumentFieldEvent(IDocumentField source, int type) {
    super(source);
    m_type = type;
  }

  public DocumentFieldEvent(IDocumentField source, int type, String text) {
    this(source, type);
    m_text = text;
  }

  public DocumentFieldEvent(IDocumentField source, int type, File saveAsFile, String saveAsType) {
    this(source, type);
    m_saveAsFile = saveAsFile;
    m_saveAsType = saveAsType;
  }

  public IDocumentField getDocumentField() {
    return (IDocumentField) getSource();
  }

  public int getType() {
    return m_type;
  }

  public String getText() {
    return m_text;
  }

  public File getSaveAsFile() {
    return m_saveAsFile;
  }

  /**
   * @return the file extension to write (html, doc, odf, ...)
   */
  public String getSaveAsType() {
    return m_saveAsType;
  }

}
