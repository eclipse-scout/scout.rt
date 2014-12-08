package org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ICompositeFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox.SequenceBoxChains.SequenceBoxCheckFromToChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox.SequenceBoxChains.SequenceBoxCreateLabelSuffixChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox.SequenceBoxChains.SequenceBoxIsLabelSuffixCandidateChain;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;

public interface ISequenceBoxExtension<OWNER extends AbstractSequenceBox> extends ICompositeFieldExtension<OWNER> {

  boolean execIsLabelSuffixCandidate(SequenceBoxIsLabelSuffixCandidateChain chain, IFormField formField);

  <T extends Comparable<T>> void execCheckFromTo(SequenceBoxCheckFromToChain chain, IValueField<T>[] valueFields, int changedIndex) throws ProcessingException;

  String execCreateLabelSuffix(SequenceBoxCreateLabelSuffixChain chain);
}
