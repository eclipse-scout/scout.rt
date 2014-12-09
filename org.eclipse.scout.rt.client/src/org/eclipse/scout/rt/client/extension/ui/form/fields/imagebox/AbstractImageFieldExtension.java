package org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox;

import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.ImageFieldChains.ImageFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.ImageFieldChains.ImageFieldDropRequestChain;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.AbstractImageField;

public abstract class AbstractImageFieldExtension<OWNER extends AbstractImageField> extends AbstractFormFieldExtension<OWNER> implements IImageFieldExtension<OWNER> {

  public AbstractImageFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public TransferObject execDragRequest(ImageFieldDragRequestChain chain) throws ProcessingException {
    return chain.execDragRequest();
  }

  @Override
  public void execDropRequest(ImageFieldDropRequestChain chain, TransferObject transferObject) throws ProcessingException {
    chain.execDropRequest(transferObject);
  }
}
