package org.eclipse.scout.rt.client.extension.ui.action.menu.root;

import org.eclipse.scout.rt.client.extension.ui.action.menu.AbstractMenuExtension;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractContextMenu;

public abstract class AbstractContextMenuExtension<OWNER extends AbstractContextMenu> extends AbstractMenuExtension<OWNER> implements IContextMenuExtension<OWNER> {

  public AbstractContextMenuExtension(OWNER owner) {
    super(owner);
  }
}
