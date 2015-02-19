package org.eclipse.scout.rt.client.extension.ui.action.view;

import org.eclipse.scout.rt.client.extension.ui.action.AbstractActionExtension;
import org.eclipse.scout.rt.client.ui.action.view.AbstractViewButton;

public abstract class AbstractViewButtonExtension<OWNER extends AbstractViewButton> extends AbstractActionExtension<OWNER> implements IViewButtonExtension<OWNER> {

  public AbstractViewButtonExtension(OWNER owner) {
    super(owner);
  }
}
