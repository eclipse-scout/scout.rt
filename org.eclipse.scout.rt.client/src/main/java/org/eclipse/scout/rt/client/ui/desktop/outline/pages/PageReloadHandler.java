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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.ui.basic.table.IReloadHandler;

/**
 * This class triggers the <code>reloadPage()</code> method of a referenced page.
 * 
 * @since 5.0.0
 */
public class PageReloadHandler implements IReloadHandler {

  private final IPage m_page;

  public PageReloadHandler(IPage page) {
    m_page = page;
  }

  @Override
  public void reload() {
    m_page.reloadPage();
  }

}
