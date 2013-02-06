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
package org.eclipse.scout.rt.client.ui.desktop.navigation;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.desktop.navigation.internal.UserNavigationHistory;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;

public class NavigationHistoryEvent extends EventObject {
  private static final long serialVersionUID = 1L;
  private final int m_type;
  private final Bookmark m_bookmark;

  public static final int TYPE_CHANGED = 10;
  public static final int TYPE_BOOKMARK_ADDED = 20;
  public static final int TYPE_BOOKMARK_REMOVDED = 30;

  public NavigationHistoryEvent(UserNavigationHistory service, int type) {
    this(service, type, null);
  }

  public NavigationHistoryEvent(UserNavigationHistory service, int type, Bookmark bookmark) {
    super(service);
    m_type = type;
    m_bookmark = bookmark;
  }

  public UserNavigationHistory getNavigationService() {
    return (UserNavigationHistory) getSource();
  }

  public int getType() {
    return m_type;
  }

  public Bookmark getBookmark() {
    return m_bookmark;
  }

}
