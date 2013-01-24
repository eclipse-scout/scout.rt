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
package org.eclipse.scout.rt.client.ui.desktop.navigation.internal;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.desktop.navigation.INavigationHistoryService;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.service.SERVICES;

public class ActivateNavigationHistoryMenu extends AbstractMenu {
  private final Bookmark m_bookmark;

  public ActivateNavigationHistoryMenu(Bookmark b) {
    super(false);
    m_bookmark = b;
    callInitializer();
  }

  public Bookmark getBookmark() {
    return m_bookmark;
  }

  @Override
  protected void execInitAction() {
    if (m_bookmark != null) {
      setText(m_bookmark.getTitle());
      setIconId(m_bookmark.getIconId());
    }
  }

  @Override
  protected void execAction() throws ProcessingException {
    if (m_bookmark != null) {
      SERVICES.getService(INavigationHistoryService.class).stepTo(m_bookmark);
    }
  }
}
