package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractSearchForm;

public abstract class AbstractSearchFormExtension<OWNER extends AbstractSearchForm> extends AbstractFormExtension<OWNER> implements ISearchFormExtension<OWNER> {

  public AbstractSearchFormExtension(OWNER owner) {
    super(owner);
  }
}
