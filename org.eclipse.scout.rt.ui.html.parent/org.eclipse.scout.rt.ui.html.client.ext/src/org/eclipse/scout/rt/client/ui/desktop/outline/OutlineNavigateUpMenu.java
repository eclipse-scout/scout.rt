package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.form.fields.ModelVariant;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * When the user presses the "navigate up" button, we want to display either the detail table or the detail form
 * depending on the current state of the node. But when the user clicks on a node we _always_ display the detail
 * form (if one exists). So the behavior is slightly different in these two cases.
 */
@ModelVariant("NavigateUp")
@Order(-200)
public class OutlineNavigateUpMenu extends AbstractOutlineNavigationMenu {

  public OutlineNavigateUpMenu(IOutline outline) {
    super(outline);
  }

  @Override
  protected Set<? extends IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.hashSet(OutlineMenuType.Navigation);
  }

  @Override
  protected String getConfiguredText() {
    return TEXTS.get("Up");
  }

}
