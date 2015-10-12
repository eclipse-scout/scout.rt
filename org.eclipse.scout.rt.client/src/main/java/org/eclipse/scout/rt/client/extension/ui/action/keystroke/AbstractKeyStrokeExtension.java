package org.eclipse.scout.rt.client.extension.ui.action.keystroke;

import org.eclipse.scout.rt.client.extension.ui.action.AbstractActionExtension;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;

public abstract class AbstractKeyStrokeExtension<OWNER extends AbstractKeyStroke> extends AbstractActionExtension<OWNER> implements IKeyStrokeExtension<OWNER> {

  public AbstractKeyStrokeExtension(OWNER owner) {
    super(owner);
  }
}
