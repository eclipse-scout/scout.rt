/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
