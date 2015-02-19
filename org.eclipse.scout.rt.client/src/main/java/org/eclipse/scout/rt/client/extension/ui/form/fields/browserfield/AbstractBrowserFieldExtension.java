package org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.BrowserFieldChains.BrowserFieldAcceptLocationChangeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.BrowserFieldChains.BrowserFieldLocationChangedChain;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.AbstractBrowserField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

public abstract class AbstractBrowserFieldExtension<OWNER extends AbstractBrowserField> extends AbstractValueFieldExtension<RemoteFile, OWNER> implements IBrowserFieldExtension<OWNER> {

  public AbstractBrowserFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execLocationChanged(BrowserFieldLocationChangedChain chain, String location, String path, boolean local) throws ProcessingException {
    chain.execLocationChanged(location, path, local);
  }

  @Override
  public boolean execAcceptLocationChange(BrowserFieldAcceptLocationChangeChain chain, String location, String path, boolean local) throws ProcessingException {
    return chain.execAcceptLocationChange(location, path, local);
  }
}
