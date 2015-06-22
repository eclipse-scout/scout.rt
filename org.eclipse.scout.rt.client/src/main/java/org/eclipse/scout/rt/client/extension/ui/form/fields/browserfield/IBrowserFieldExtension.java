package org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.BrowserFieldChains.BrowserFieldLocationChangedChain;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.AbstractBrowserField;

public interface IBrowserFieldExtension<OWNER extends AbstractBrowserField> extends IFormFieldExtension<OWNER> {

  void execPostMessage(BrowserFieldLocationChangedChain chain, String data, String origin) throws ProcessingException;

}
