/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox;

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
  public <T extends Comparable<T>> void execCheckFromTo(SequenceBoxCheckFromToChain chain, IValueField<T>[] valueFields, int changedIndex) {
    chain.execCheckFromTo(valueFields, changedIndex);
  }

  @Override
  public String execCreateLabelSuffix(SequenceBoxCreateLabelSuffixChain chain) {
    return chain.execCreateLabelSuffix();
  }
}
