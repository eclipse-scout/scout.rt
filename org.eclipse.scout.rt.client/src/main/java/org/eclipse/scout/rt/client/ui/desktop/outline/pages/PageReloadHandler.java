/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
  public void reload(String reloadReason) {
    m_page.reloadPage(reloadReason);
  }
}
