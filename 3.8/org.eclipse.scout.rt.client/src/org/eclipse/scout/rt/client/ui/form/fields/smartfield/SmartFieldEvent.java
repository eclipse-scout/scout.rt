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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

public class SmartFieldEvent extends java.util.EventObject {
  private static final long serialVersionUID = 1L;

  private int m_type;

  public SmartFieldEvent(ISmartField source, int type) {
    super(source);
    m_type = type;
  }

  public ISmartField getSmartField() {
    return (ISmartField) getSource();
  }

  public int getType() {
    return m_type;
  }

}
