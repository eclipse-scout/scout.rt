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

public class ContentAssistFieldEvent extends java.util.EventObject {
  private static final long serialVersionUID = 1L;

  private final int m_type;

  public ContentAssistFieldEvent(IContentAssistField source, int type) {
    super(source);
    m_type = type;
  }

  public IContentAssistField getSmartField() {
    return (IContentAssistField) getSource();
  }

  public int getType() {
    return m_type;
  }

}
