package org.eclipse.scout.rt.client.extension.ui.form.fields.mailfield;

import java.io.File;

import javax.mail.internet.MimeMessage;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.mailfield.MailFieldChains.MailFieldAttachementActionChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.mailfield.MailFieldChains.MailFieldHyperlinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.mailfield.AbstractMailField;

public abstract class AbstractMailFieldExtension<OWNER extends AbstractMailField> extends AbstractValueFieldExtension<MimeMessage, OWNER> implements IMailFieldExtension<OWNER> {

  public AbstractMailFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execHyperlinkAction(MailFieldHyperlinkActionChain chain, String ref) {
    chain.execHyperlinkAction(ref);
  }

  @Override
  public void execAttachementAction(MailFieldAttachementActionChain chain, File file) {
    chain.execAttachementAction(file);
  }
}
