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
package org.eclipse.scout.rt.client.services.common.bookmark;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

/**
 * Standard case bookmark adapter
 */
public class DefaultBookmarkAdapter implements IBookmarkAdapter {

  private IPage<?> m_page;

  public DefaultBookmarkAdapter(IPage<?> page) {
    m_page = page;
  }

  protected IPage<?> getPage() {
    return m_page;
  }

  public void setPage(IPage<?> page) {
    m_page = page;
  }

  @Override
  public String getIdentifier() {
    return m_page.getUserPreferenceContext();
  }

  @Override
  public String getTitle() {
    return m_page.getCell().getText();
  }

  @Override
  public String getText() {
    return m_page.getCell().getText();
  }

  @Override
  public String getIconId() {
    return m_page.getCell().getIconId();
  }

  @Override
  public String getOutlineClassName() {
    return m_page.getOutline().getClass().getName();
  }

  @Override
  public String getOutlineTitle() {
    return m_page.getOutline().getTitle();
  }

}
