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
package org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

/**
 * A page extension filter is used to control whether a page extension is applied in a particular environment.
 * 
 * @since 3.9.0
 */
public interface IPageExtensionFilter {

  /**
   * This method is called to decide whether an extension is used in the given context. The parameter
   * <code>outline</code> is never <code>null</code>. The parameter <code>parentPage</code> however is <code>null</code>
   * if the filter is invoked in the context of an outline (i.e. whenever an extension is checked to be applied on an
   * outline's root level).
   * 
   * @param outline
   *          the current context's outline instance. Never <code>null</code>.
   * @param parentPage
   *          the current context's parent page instance or <code>null</code>, if there is no parent page.
   * @param affectedPage
   *          the page to be modified or removed or <code>null</code>, if the filter is applied on a page contribution
   *          extension.
   * @return Returns <code>true</code> if the extension has to be applied in the given context. Otherwise
   *         <code>false</code>.
   */
  boolean accept(IOutline outline, IPage parentPage, IPage affectedPage);
}
