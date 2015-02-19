package org.eclipse.scout.rt.client.extension.ui.form.fields.documentfield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.documentfield.DocumentFieldChains.DocumentFieldComReadyStatusChangedChain;
import org.eclipse.scout.rt.client.ui.form.fields.documentfield.AbstractDocumentField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

public abstract class AbstractDocumentFieldExtension<OWNER extends AbstractDocumentField> extends AbstractValueFieldExtension<RemoteFile, OWNER> implements IDocumentFieldExtension<OWNER> {

  public AbstractDocumentFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execComReadyStatusChanged(DocumentFieldComReadyStatusChangedChain chain, boolean ready) throws ProcessingException {
    chain.execComReadyStatusChanged(ready);
  }
}
