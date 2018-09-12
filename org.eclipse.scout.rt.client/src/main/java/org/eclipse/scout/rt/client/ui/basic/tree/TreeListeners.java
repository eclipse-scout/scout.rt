/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineEvent;
import org.eclipse.scout.rt.platform.util.event.AbstractGroupedListenerList;

public final class TreeListeners extends AbstractGroupedListenerList<TreeListener, TreeEvent, Integer> {
  private static final Set<Integer> KNOWN_EVENT_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
      TreeEvent.TYPE_NODES_INSERTED,
      TreeEvent.TYPE_NODES_UPDATED,
      TreeEvent.TYPE_NODE_FILTER_CHANGED,
      TreeEvent.TYPE_NODES_DELETED,
      TreeEvent.TYPE_ALL_CHILD_NODES_DELETED,
      TreeEvent.TYPE_BEFORE_NODES_SELECTED,
      TreeEvent.TYPE_NODES_SELECTED,
      TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED,
      TreeEvent.TYPE_NODE_EXPANDED,
      TreeEvent.TYPE_NODE_COLLAPSED,
      TreeEvent.TYPE_NODE_EXPANDED_RECURSIVE,
      TreeEvent.TYPE_NODE_COLLAPSED_RECURSIVE,
      TreeEvent.TYPE_NODE_ACTION,
      TreeEvent.TYPE_NODES_DRAG_REQUEST,
      TreeEvent.TYPE_DRAG_FINISHED,
      TreeEvent.TYPE_NODE_DROP_ACTION,
      TreeEvent.TYPE_NODE_REQUEST_FOCUS,
      TreeEvent.TYPE_NODE_ENSURE_VISIBLE,
      TreeEvent.TYPE_REQUEST_FOCUS,
      TreeEvent.TYPE_NODE_CLICK,
      TreeEvent.TYPE_SCROLL_TO_SELECTION,
      TreeEvent.TYPE_NODE_CHANGED,
      TreeEvent.TYPE_NODE_DROP_TARGET_CHANGED,
      TreeEvent.TYPE_NODES_CHECKED,
      OutlineEvent.TYPE_PAGE_CHANGED,
      OutlineEvent.TYPE_PAGE_BEFORE_DATA_LOADED,
      OutlineEvent.TYPE_PAGE_AFTER_DATA_LOADED,
      OutlineEvent.TYPE_PAGE_AFTER_TABLE_INIT,
      OutlineEvent.TYPE_PAGE_AFTER_PAGE_INIT,
      OutlineEvent.TYPE_PAGE_AFTER_SEARCH_FORM_START,
      OutlineEvent.TYPE_PAGE_AFTER_DISPOSE,
      OutlineEvent.TYPE_PAGE_ACTIVATED)));

  @Override
  protected Integer eventType(TreeEvent event) {
    return event.getType();
  }

  @Override
  protected Set<Integer> knownEventTypes() {
    return KNOWN_EVENT_TYPES;
  }

  @Override
  protected Integer otherEventsType() {
    return null;
  }

  @Override
  protected void handleEvent(TreeListener listener, TreeEvent event) {
    listener.treeChanged(event);
  }
}
