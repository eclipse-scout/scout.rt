/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox;

import org.eclipse.scout.rt.client.extension.ui.form.fields.ICompositeFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox.SequenceBoxChains.SequenceBoxCheckFromToChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox.SequenceBoxChains.SequenceBoxCreateLabelSuffixChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox.SequenceBoxChains.SequenceBoxIsLabelSuffixCandidateChain;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;

public interface ISequenceBoxExtension<OWNER extends AbstractSequenceBox> extends ICompositeFieldExtension<OWNER> {

  boolean execIsLabelSuffixCandidate(SequenceBoxIsLabelSuffixCandidateChain chain, IFormField formField);

  <T extends Comparable<T>> void execCheckFromTo(SequenceBoxCheckFromToChain chain, IValueField<T>[] valueFields, int changedIndex);

  String execCreateLabelSuffix(SequenceBoxCreateLabelSuffixChain chain);
}
