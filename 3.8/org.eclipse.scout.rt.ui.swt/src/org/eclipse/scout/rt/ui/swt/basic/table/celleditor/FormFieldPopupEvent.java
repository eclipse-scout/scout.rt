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
package org.eclipse.scout.rt.ui.swt.basic.table.celleditor;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public class FormFieldPopupEvent extends EventObject {

  private static final long serialVersionUID = 1L;

  public static final int TYPE_OK = 1 << 0;
  public static final int TYPE_CANCEL = 1 << 1;
  public static final int TYPE_OPEN = 1 << 2;
  public static final int TYPE_FOCUS_NEXT = 1 << 3;
  public static final int TYPE_FOCUS_BACK = 1 << 4;

  private int m_type;

  public FormFieldPopupEvent(IFormField source, int type) {
    super(source);
    m_type = type;
  }

  public int getType() {
    return m_type;
  }

  @Override
  public IFormField getSource() {
    return (IFormField) super.getSource();
  }
}
