package org.eclipse.scout.rt.client.extension.ui.action.menu.checkbox;

import org.eclipse.scout.rt.client.extension.ui.action.menu.AbstractMenuExtension;
import org.eclipse.scout.rt.client.ui.action.menu.checkbox.AbstractCheckBoxMenu;

public abstract class AbstractCheckBoxMenuExtension<OWNER extends AbstractCheckBoxMenu> extends AbstractMenuExtension<OWNER> implements ICheckBoxMenuExtension<OWNER> {

  public AbstractCheckBoxMenuExtension(OWNER owner) {
    super(owner);
  }
}
