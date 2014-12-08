package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.AbstractTreeFieldExtension;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineTreeField;

public abstract class AbstractOutlineTreeFieldExtension<OWNER extends AbstractOutlineTreeField> extends AbstractTreeFieldExtension<OWNER> implements IOutlineTreeFieldExtension<OWNER> {

  public AbstractOutlineTreeFieldExtension(OWNER owner) {
    super(owner);
  }
}
