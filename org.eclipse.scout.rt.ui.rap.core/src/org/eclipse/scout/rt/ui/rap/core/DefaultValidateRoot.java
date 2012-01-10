/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.core;

import org.eclipse.swt.widgets.Composite;

public class DefaultValidateRoot implements IValidateRoot {
  private final Composite m_root;

  public DefaultValidateRoot(Composite root) {
    m_root = root;
  }

  @Override
  public void validate() {
    if (m_root != null && !m_root.isDisposed()) {
      m_root.layout(true, true);
    }
  }

  @Override
  public Composite getUiComposite() {
    return m_root;
  }

}
