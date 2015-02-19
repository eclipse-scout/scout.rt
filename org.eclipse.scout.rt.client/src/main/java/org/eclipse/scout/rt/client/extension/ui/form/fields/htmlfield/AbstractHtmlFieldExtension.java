package org.eclipse.scout.rt.client.extension.ui.form.fields.htmlfield;

import java.net.URL;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.htmlfield.HtmlFieldChains.HtmlFieldHyperlinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;

public abstract class AbstractHtmlFieldExtension<OWNER extends AbstractHtmlField> extends AbstractValueFieldExtension<String, OWNER> implements IHtmlFieldExtension<OWNER> {

  public AbstractHtmlFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execHyperlinkAction(HtmlFieldHyperlinkActionChain chain, URL url, String path, boolean local) throws ProcessingException {
    chain.execHyperlinkAction(url, path, local);
  }
}
