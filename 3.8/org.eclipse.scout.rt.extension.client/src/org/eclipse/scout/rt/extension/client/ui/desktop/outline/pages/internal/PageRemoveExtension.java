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
 * This class references a particular type of pages and describes the places in the existing outline structure, where
 * they are removed.
 * 
 * @since 3.9.0
 */
public class PageRemoveExtension extends AbstractPageExtension {

  private final Class<? extends IPage> m_pageClass;

  public PageRemoveExtension(Class<? extends IPage> pageClass) {
    this(null, pageClass);
  }

  public PageRemoveExtension(IPageExtensionFilter pageFilter, Class<? extends IPage> pageClass) {
    super(pageFilter);
    m_pageClass = pageClass;
  }

  public Class<? extends IPage> getPageClass() {
    return m_pageClass;
  }

  @Override
  public boolean accept(IOutline outline, IPage parentPage, IPage page) {
    if (page == null) {
      return false;
    }
    if (!getPageClass().isInstance(page)) {
      return false;
    }
    return super.accept(outline, parentPage, page);
  }
}
