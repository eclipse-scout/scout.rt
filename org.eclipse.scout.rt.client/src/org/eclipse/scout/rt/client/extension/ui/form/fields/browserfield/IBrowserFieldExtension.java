package org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.BrowserFieldChains.BrowserFieldAcceptLocationChangeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.BrowserFieldChains.BrowserFieldLocationChangedChain;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.AbstractBrowserField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

public interface IBrowserFieldExtension<OWNER extends AbstractBrowserField> extends IValueFieldExtension<RemoteFile, OWNER> {

  void execLocationChanged(BrowserFieldLocationChangedChain chain, String location, String path, boolean local) throws ProcessingException;

  boolean execAcceptLocationChange(BrowserFieldAcceptLocationChangeChain chain, String location, String path, boolean local) throws ProcessingException;
}
