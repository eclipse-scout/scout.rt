package org.eclipse.scout.rt.client.extension.ui.form.fields.composer;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerValueBoxChains.ComposerValueBoxChangedValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.IGroupBoxExtension;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerValueBox;

public interface IComposerValueBoxExtension<OWNER extends AbstractComposerValueBox> extends IGroupBoxExtension<OWNER> {

  void execChangedValue(ComposerValueBoxChangedValueChain chain) throws ProcessingException;
}
