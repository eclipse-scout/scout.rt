package org.eclipse.scout.rt.client.ui.basic.tree;

import org.eclipse.scout.rt.platform.util.event.AbstractGroupedListenerList;

public final class TreeListeners extends AbstractGroupedListenerList<TreeListener, TreeEvent, Integer> {

  @Override
  protected Integer eventType(TreeEvent event) {
    return event.getType();
  }

  @Override
  protected Integer allEventsType() {
    return null;
  }

  @Override
  protected void handleEvent(TreeListener listener, TreeEvent event) {
    listener.treeChanged(event);
  }
}
