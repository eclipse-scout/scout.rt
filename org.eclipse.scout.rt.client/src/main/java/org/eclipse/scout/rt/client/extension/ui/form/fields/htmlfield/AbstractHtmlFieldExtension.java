package org.eclipse.scout.rt.client.extension.ui.form.fields.htmlfield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.htmlfield.HtmlFieldChains.HtmlFieldAppLinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;

public abstract class AbstractHtmlFieldExtension<OWNER extends AbstractHtmlField> extends AbstractValueFieldExtension<String, OWNER>implements IHtmlFieldExtension<OWNER> {

  public AbstractHtmlFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execAppLinkAction(HtmlFieldAppLinkActionChain chain, String ref) throws ProcessingException {
    chain.execAppLinkAction(ref);
  }
}
