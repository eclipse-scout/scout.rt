package org.eclipse.scout.rt.client.extension.ui.form.fields.composer;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerValueBoxChains.ComposerValueBoxChangedValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerValueBox;

public abstract class AbstractComposerValueBoxExtension<OWNER extends AbstractComposerValueBox> extends AbstractGroupBoxExtension<OWNER> implements IComposerValueBoxExtension<OWNER> {

  public AbstractComposerValueBoxExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execChangedValue(ComposerValueBoxChangedValueChain chain) throws ProcessingException {
    chain.execChangedValue();
  }
}
