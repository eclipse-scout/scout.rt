package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.form.fields.ModelVariant;
import org.eclipse.scout.rt.shared.TEXTS;

@ModelVariant("NavigateDown")
public class OutlineNavigateDownMenu extends AbstractOutlineNavigationMenu {

  public OutlineNavigateDownMenu(IOutline outline) {
    super(outline);
  }

  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  protected IMenuType getMenuType() {
    return TableMenuType.SingleSelection;
  }

  @Override
  protected String getConfiguredText() {
    return TEXTS.get("Show");
  }

}
