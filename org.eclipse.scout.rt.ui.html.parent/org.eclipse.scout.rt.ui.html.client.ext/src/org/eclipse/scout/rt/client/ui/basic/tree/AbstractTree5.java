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
package org.eclipse.scout.rt.client.ui.basic.tree;

public abstract class AbstractTree5 extends AbstractTree implements ITree5 {

  private boolean m_filterEnabled;

  @Override
  public void setFilterEnabled(boolean filterEnabled) {
    m_filterEnabled = filterEnabled;
  }

  @Override
  public boolean isFilterEnabled() {
    return m_filterEnabled;
  }

}
