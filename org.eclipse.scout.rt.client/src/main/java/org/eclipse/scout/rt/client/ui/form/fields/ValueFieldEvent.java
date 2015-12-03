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
package org.eclipse.scout.rt.client.ui.form.fields;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;

public class ValueFieldEvent<T> extends EventObject implements IModelEvent {

  public static final int TYPE_CUT = 100;
  public static final int TYPE_COPY = 200;
  public static final int TYPE_PASTE = 300;

  private static final long serialVersionUID = 1L;

  private final int m_type;

  public ValueFieldEvent(IValueField<T> source, int type) {
    super(source);
    m_type = type;
  }

  @Override
  @SuppressWarnings("unchecked")
  public IValueField<T> getSource() {
    return (IValueField<T>) super.getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }
}
