package org.eclipse.scout.rt.client.extension.ui.form.fields.htmlfield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.htmlfield.HtmlFieldChains.HtmlFieldAppLinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;

public interface IHtmlFieldExtension<OWNER extends AbstractHtmlField> extends IValueFieldExtension<String, OWNER> {

  void execAppLinkAction(HtmlFieldAppLinkActionChain chain, String ref) throws ProcessingException;
}
