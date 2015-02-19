package org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractCompositeFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox.SequenceBoxChains.SequenceBoxCheckFromToChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox.SequenceBoxChains.SequenceBoxCreateLabelSuffixChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox.SequenceBoxChains.SequenceBoxIsLabelSuffixCandidateChain;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;

public abstract class AbstractSequenceBoxExtension<OWNER extends AbstractSequenceBox> extends AbstractCompositeFieldExtension<OWNER> implements ISequenceBoxExtension<OWNER> {

  public AbstractSequenceBoxExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public boolean execIsLabelSuffixCandidate(SequenceBoxIsLabelSuffixCandidateChain chain, IFormField formField) {
    return chain.execIsLabelSuffixCandidate(formField);
  }

  @Override
  public <T extends Comparable<T>> void execCheckFromTo(SequenceBoxCheckFromToChain chain, IValueField<T>[] valueFields, int changedIndex) throws ProcessingException {
    chain.execCheckFromTo(valueFields, changedIndex);
  }

  @Override
  public String execCreateLabelSuffix(SequenceBoxCreateLabelSuffixChain chain) {
    return chain.execCreateLabelSuffix();
  }
}
