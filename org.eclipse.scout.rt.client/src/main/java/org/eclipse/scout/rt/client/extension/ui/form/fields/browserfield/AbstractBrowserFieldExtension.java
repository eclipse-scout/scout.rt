package org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.BrowserFieldChains.BrowserFieldLocationChangedChain;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.AbstractBrowserField;

public abstract class AbstractBrowserFieldExtension<OWNER extends AbstractBrowserField> extends AbstractFormFieldExtension<OWNER> implements IBrowserFieldExtension<OWNER> {

  public AbstractBrowserFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execPostMessage(BrowserFieldLocationChangedChain chain, String data, String origin) {
    chain.execPostMessage(data, origin);
  }

}
