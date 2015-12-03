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
package org.eclipse.scout.rt.client.ui.desktop.navigation;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;
import org.eclipse.scout.rt.client.ui.desktop.navigation.internal.UserNavigationHistory;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;

/**
 * An event describing a change in the navigation history.
 */
public class NavigationHistoryEvent extends EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;
  private final int m_type;
  private final Bookmark m_bookmark;

  /**
   * event when the navigation in the history has changed. (e.g. when stepping backwards)
   */
  public static final int TYPE_CHANGED = 10;
  /**
   * event when bookmark was added
   */
  public static final int TYPE_BOOKMARK_ADDED = 20;
  /**
   * event when bookmark was removed
   */
  public static final int TYPE_BOOKMARK_REMOVDED = 30;

  /**
   * Creates a new NavigationHistoryEvent object.
   *
   * @param service
   *          The source navigation history on which the Event initially occurred.
   * @param type
   *          the event type: one of {@link #TYPE_CHANGED}, {@link #TYPE_BOOKMARK_ADDED},{@link #TYPE_BOOKMARK_REMOVDED}
   */
  public NavigationHistoryEvent(UserNavigationHistory service, int type) {
    this(service, type, null);
  }

  /**
   * Creates a new NavigationHistoryEvent object.
   *
   * @param service
   *          The source navigation history on which the Event initially occurred.
   * @param type
   *          the event type: one of {@link #TYPE_CHANGED}, {@link #TYPE_BOOKMARK_ADDED},{@link #TYPE_BOOKMARK_REMOVDED}
   * @param bookmark
   *          the bookmark associated with the event: The added or removed bookmark.
   */
  public NavigationHistoryEvent(UserNavigationHistory service, int type, Bookmark bookmark) {
    super(service);
    m_type = type;
    m_bookmark = bookmark;
  }

  /**
   * @return The source navigation history
   */
  public UserNavigationHistory getNavigationService() {
    return (UserNavigationHistory) getSource();
  }

  /**
   * @return the event type: one of {@link #TYPE_CHANGED}, {@link #TYPE_BOOKMARK_ADDED},{@link #TYPE_BOOKMARK_REMOVDED}
   */
  @Override
  public int getType() {
    return m_type;
  }

  /**
   * @return the bookmark associated with the event: The added or removed bookmark or <code>null</code> if unknown.
   */
  public Bookmark getBookmark() {
    return m_bookmark;
  }

}
