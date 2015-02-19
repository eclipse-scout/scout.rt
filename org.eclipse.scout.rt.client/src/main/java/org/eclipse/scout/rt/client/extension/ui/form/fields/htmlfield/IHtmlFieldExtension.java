package org.eclipse.scout.rt.client.extension.ui.form.fields.htmlfield;

import java.net.URL;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.htmlfield.HtmlFieldChains.HtmlFieldHyperlinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;

public interface IHtmlFieldExtension<OWNER extends AbstractHtmlField> extends IValueFieldExtension<String, OWNER> {

  void execHyperlinkAction(HtmlFieldHyperlinkActionChain chain, URL url, String path, boolean local) throws ProcessingException;
}
