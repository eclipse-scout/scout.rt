package org.eclipse.scout.rt.client.extension.ui.action.menu;

import org.eclipse.scout.rt.client.extension.ui.action.menu.MenuChains.MenuOwnerValueChangedChain;
import org.eclipse.scout.rt.client.extension.ui.action.tree.AbstractActionNodeExtension;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

public abstract class AbstractMenuExtension<OWNER extends AbstractMenu> extends AbstractActionNodeExtension<IMenu, OWNER> implements IMenuExtension<OWNER> {

  public AbstractMenuExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execOwnerValueChanged(MenuOwnerValueChangedChain chain, Object newOwnerValue) {
    chain.execOwnerValueChanged(newOwnerValue);
  }
}
