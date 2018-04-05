package org.eclipse.scout.rt.client.ui.basic.tree;

import org.eclipse.scout.rt.platform.util.event.AbstractCompositeEventListenerList;

public final class TreeListeners extends AbstractCompositeEventListenerList<TreeListener, TreeEvent> {

  @Override
  protected int eventType(TreeEvent event) {
    return event.getType();
  }

  @Override
  protected void handleEvent(TreeListener listener, TreeEvent event) {
    listener.treeChanged(event);
  }
}
