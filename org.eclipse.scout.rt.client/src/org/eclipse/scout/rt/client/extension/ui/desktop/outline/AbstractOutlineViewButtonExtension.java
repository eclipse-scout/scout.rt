package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import org.eclipse.scout.rt.client.extension.ui.action.view.AbstractViewButtonExtension;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineViewButton;

public abstract class AbstractOutlineViewButtonExtension<OWNER extends AbstractOutlineViewButton> extends AbstractViewButtonExtension<OWNER> implements IOutlineViewButtonExtension<OWNER> {

  public AbstractOutlineViewButtonExtension(OWNER owner) {
    super(owner);
  }
}
