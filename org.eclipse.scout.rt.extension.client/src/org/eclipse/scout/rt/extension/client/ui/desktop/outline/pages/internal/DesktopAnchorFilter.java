/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal;

import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.IPageExtensionFilter;

/**
 * @since 3.9.0
 */
public class DesktopAnchorFilter implements IPageExtensionFilter {

  private final Class<? extends IOutline> m_outlineFilterClass;
  private final Class<? extends IPage> m_parentPageFilterClass;

  public DesktopAnchorFilter(Class<? extends IOutline> outlineFilterClass, Class<? extends IPage> parentPageFilterClass) {
    m_outlineFilterClass = outlineFilterClass;
    m_parentPageFilterClass = parentPageFilterClass;
  }

  public Class<? extends IOutline> getOutlineFilterClass() {
    return m_outlineFilterClass;
  }

  public Class<? extends IPage> getParentPageFilterClass() {
    return m_parentPageFilterClass;
  }

  @Override
  public boolean accept(IOutline outline, IPage parentPage, IPage affectedPage) {
    if (getParentPageFilterClass() != null) {
      return parentPage != null && getParentPageFilterClass().isInstance(parentPage);
    }
    if (getOutlineFilterClass() != null) {
      return outline != null && parentPage == null && getOutlineFilterClass().isInstance(outline);
    }
    return true;
  }
}
