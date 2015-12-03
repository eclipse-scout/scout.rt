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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.rt.client.ui.IModelEvent;

public class ContentAssistFieldEvent extends java.util.EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;

  private final int m_type;

  public ContentAssistFieldEvent(IContentAssistField source, int type) {
    super(source);
    m_type = type;
  }

  public IContentAssistField getSmartField() {
    return (IContentAssistField) getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }

}
