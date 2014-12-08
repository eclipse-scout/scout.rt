package org.eclipse.scout.rt.client.extension.ui.action.tool;

import org.eclipse.scout.rt.client.extension.ui.action.AbstractActionExtension;
import org.eclipse.scout.rt.client.ui.action.tool.AbstractToolButton;

public abstract class AbstractToolButtonExtension<OWNER extends AbstractToolButton> extends AbstractActionExtension<OWNER> implements IToolButtonExtension<OWNER> {

  public AbstractToolButtonExtension(OWNER owner) {
    super(owner);
  }
}
