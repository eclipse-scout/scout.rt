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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

/**
 * This interface is used for modifying {@link IPage}s. Classes implementing this interface must provide a default
 * constructor.
 * 
 * @since 3.9.0
 */
public interface IPageModifier<T extends IPage> {

  /**
   * This method allows modifying configured pages. The parameter <code>outline</code> is never <code>null</code>. The
   * parameter <code>parentPage</code> however is <code>null</code> if this method is invoked in the context of an
   * outline.
   * 
   * @param outline
   *          the current context's outline instance. Never <code>null</code>.
   * @param parentPage
   *          the current context's parent page instance or <code>null</code>, if there is no parent page.
   * @param page
   *          the page to be modified.
   */
  void modify(IOutline outline, IPage parentPage, T page) throws ProcessingException;
}
