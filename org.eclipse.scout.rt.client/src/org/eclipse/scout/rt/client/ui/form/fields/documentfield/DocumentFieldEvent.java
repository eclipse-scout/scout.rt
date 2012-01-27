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

import java.util.EventObject;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public class DocumentFieldEvent extends EventObject {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DocumentFieldEvent.class);

  private static final long serialVersionUID = 1L;

  public static final int TYPE_SAVE_AS = 10;
  public static final int TYPE_SAVE_NEEDED = 12;

  public static final int TYPE_AUTORESIZE_DOCUMENT = 20;

  private final int m_type;
  private final Object m_data;

  public DocumentFieldEvent(IDocumentField source, int type) {
    super(source);
    m_type = type;
    m_data = null;
  }

  public DocumentFieldEvent(IDocumentField source, int type, Object data) {
    super(source);
    m_type = type;
    m_data = data;
  }

  public IDocumentField getDocumentField() {
    return (IDocumentField) getSource();
  }

  public int getType() {
    return m_type;
  }

  public Object getData() {
    return m_data;
  }

  public String getDataString() {
    try {
      return TypeCastUtility.castValue(m_data, String.class);
    }
    catch (ClassCastException e) {
      LOG.error("Could not cast data to String");
    }
    return null;
  }

  public boolean getDataBool() {
    try {
      return TypeCastUtility.castValue(m_data, boolean.class);
    }
    catch (ClassCastException e) {
      LOG.error("Could not cast data to Boolean");
    }
    return false;
  }

  public Long getDataLong() {
    try {
      return TypeCastUtility.castValue(m_data, Long.class);
    }
    catch (ClassCastException e) {
      LOG.error("Could not cast data to Long");
    }
    return null;
  }

  public Integer getDataInt() {
    try {
      return TypeCastUtility.castValue(m_data, Integer.class);
    }
    catch (ClassCastException e) {
      LOG.error("Could not cast data to Integer");
    }
    return null;
  }
}
