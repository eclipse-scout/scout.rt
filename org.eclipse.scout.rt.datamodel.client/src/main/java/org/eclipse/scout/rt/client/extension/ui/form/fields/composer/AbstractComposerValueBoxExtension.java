/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.composer;

import java.util.Map;

import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerValueBoxChains.ComposerValueBoxChangedValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerValueBoxChains.ComposerValueBoxInitOperatorToFieldMapChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerValueBox;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerValueField;

public abstract class AbstractComposerValueBoxExtension<OWNER extends AbstractComposerValueBox> extends AbstractGroupBoxExtension<OWNER> implements IComposerValueBoxExtension<OWNER> {

  public AbstractComposerValueBoxExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execChangedValue(ComposerValueBoxChangedValueChain chain) {
    chain.execChangedValue();
  }

  @Override
  public void execInitOperatorToFieldMap(ComposerValueBoxInitOperatorToFieldMapChain chain, Map<Integer /* operator */, Map<Integer /* field type */, IComposerValueField>> operatorTypeToFieldMap) {
    chain.execInitOperatorToFieldMap(operatorTypeToFieldMap);
  }
}
