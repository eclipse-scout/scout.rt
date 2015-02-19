package org.eclipse.scout.rt.client.extension.ui.form.fields.documentfield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.documentfield.DocumentFieldChains.DocumentFieldComReadyStatusChangedChain;
import org.eclipse.scout.rt.client.ui.form.fields.documentfield.AbstractDocumentField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

public interface IDocumentFieldExtension<OWNER extends AbstractDocumentField> extends IValueFieldExtension<RemoteFile, OWNER> {

  void execComReadyStatusChanged(DocumentFieldComReadyStatusChangedChain chain, boolean ready) throws ProcessingException;
}
