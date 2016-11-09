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
package org.eclipse.scout.rt.client.extension.ui.form.fields.composer;

import java.util.Map;

import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerValueBoxChains.ComposerValueBoxChangedValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerValueBoxChains.ComposerValueBoxInitOperatorToFieldMapChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.IGroupBoxExtension;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerValueBox;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerValueField;

public interface IComposerValueBoxExtension<OWNER extends AbstractComposerValueBox> extends IGroupBoxExtension<OWNER> {

  void execChangedValue(ComposerValueBoxChangedValueChain chain);

  void execInitOperatorToFieldMap(ComposerValueBoxInitOperatorToFieldMapChain chain, Map<Integer /* operator */, Map<Integer /* field type */, IComposerValueField>> operatorTypeToFieldMap);
}
