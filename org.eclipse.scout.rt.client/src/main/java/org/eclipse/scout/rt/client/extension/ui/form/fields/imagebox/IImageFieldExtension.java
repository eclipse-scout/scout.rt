package org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.ImageFieldChains.ImageFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.ImageFieldChains.ImageFieldDropRequestChain;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.imagefield.AbstractImageField;

public interface IImageFieldExtension<OWNER extends AbstractImageField> extends IFormFieldExtension<OWNER> {

  TransferObject execDragRequest(ImageFieldDragRequestChain chain);

  void execDropRequest(ImageFieldDropRequestChain chain, TransferObject transferObject);
}
