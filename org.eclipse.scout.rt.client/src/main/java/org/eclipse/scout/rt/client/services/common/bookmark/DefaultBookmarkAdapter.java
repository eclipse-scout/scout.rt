/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
    return m_page.getCell().toPlainText();
  }

  @Override
  public String getText() {
    return m_page.getCell().toPlainText();
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
