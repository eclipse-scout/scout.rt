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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.shared.ui.menu.AbstractMenu5;

public abstract class AbstractOutlineNavigationMenu extends AbstractMenu5 {

  private IOutline m_outline;

  public AbstractOutlineNavigationMenu(IOutline outline) {
    super(false);
    m_outline = outline;
    callInitializer();
  }

  @Override
  public boolean isInheritAccessibility() {
    return false;
  }

  public final IOutline getOutline() {
    return m_outline;
  }

}
