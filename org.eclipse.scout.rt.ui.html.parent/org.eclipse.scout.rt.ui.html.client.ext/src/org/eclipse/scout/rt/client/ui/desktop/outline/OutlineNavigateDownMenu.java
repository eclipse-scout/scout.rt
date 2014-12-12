package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
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
  @SuppressWarnings("unchecked")
  protected Set<? extends IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.hashSet(OutlineMenuType.Navigation, TableMenuType.SingleSelection);
  }

  @Override
  protected String getConfiguredText() {
    return TEXTS.get("Show");
  }

}
