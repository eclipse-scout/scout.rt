package org.eclipse.scout.rt.client.extension.ui.form.fields.mailfield;

import java.io.File;
import java.net.URL;

import javax.mail.internet.MimeMessage;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.mailfield.MailFieldChains.MailFieldAttachementActionChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.mailfield.MailFieldChains.MailFieldHyperlinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.mailfield.AbstractMailField;

public interface IMailFieldExtension<OWNER extends AbstractMailField> extends IValueFieldExtension<MimeMessage, OWNER> {

  void execHyperlinkAction(MailFieldHyperlinkActionChain chain, URL url, String path, boolean local) throws ProcessingException;

  void execAttachementAction(MailFieldAttachementActionChain chain, File file) throws ProcessingException;
}
