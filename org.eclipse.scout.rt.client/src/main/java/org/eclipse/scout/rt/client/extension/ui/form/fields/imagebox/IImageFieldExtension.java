package org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox;

import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.ImageFieldChains.ImageFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.ImageFieldChains.ImageFieldDropRequestChain;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.AbstractImageField;

public interface IImageFieldExtension<OWNER extends AbstractImageField> extends IFormFieldExtension<OWNER> {

  TransferObject execDragRequest(ImageFieldDragRequestChain chain) throws ProcessingException;

  void execDropRequest(ImageFieldDropRequestChain chain, TransferObject transferObject) throws ProcessingException;
}
