package org.eclipse.scout.rt.client.extension.ui.form.fields.pagefield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.fields.pagefield.AbstractPageField;

public abstract class AbstractPageFieldExtension<T extends IPage, OWNER extends AbstractPageField<T>> extends AbstractGroupBoxExtension<OWNER> implements IPageFieldExtension<T, OWNER> {

  public AbstractPageFieldExtension(OWNER owner) {
    super(owner);
  }
}
