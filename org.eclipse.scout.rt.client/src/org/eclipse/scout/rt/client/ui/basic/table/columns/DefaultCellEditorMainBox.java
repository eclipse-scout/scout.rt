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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;

/**
 * Dynamic group box to build an ad-hoc form for cell editing
 */
public class DefaultCellEditorMainBox extends AbstractGroupBox {
  private final IFormField[] m_injectedFields;

  public DefaultCellEditorMainBox(IFormField... fields) {
    super(false);
    m_injectedFields = fields;
    callInitializer();
  }

  /**
   * This is the place to inject fields dynamically
   */
  @Override
  protected void injectFieldsInternal(List<IFormField> fieldList) {
    if (m_injectedFields != null) {
      for (IFormField f : m_injectedFields) {
        fieldList.add(f);
      }
    }
  }
}
