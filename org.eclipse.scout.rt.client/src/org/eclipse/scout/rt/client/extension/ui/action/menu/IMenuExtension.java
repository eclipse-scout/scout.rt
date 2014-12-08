package org.eclipse.scout.rt.client.extension.ui.action.menu;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.action.menu.MenuChains.MenuAboutToShowChain;
import org.eclipse.scout.rt.client.extension.ui.action.menu.MenuChains.MenuOwnerValueChangedChain;
import org.eclipse.scout.rt.client.extension.ui.action.tree.IActionNodeExtension;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

public interface IMenuExtension<OWNER extends AbstractMenu> extends IActionNodeExtension<IMenu, OWNER> {

  void execAboutToShow(MenuAboutToShowChain chain) throws ProcessingException;

  void execOwnerValueChanged(MenuOwnerValueChangedChain chain, Object newOwnerValue) throws ProcessingException;
}
