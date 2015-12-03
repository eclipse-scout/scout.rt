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
package org.eclipse.scout.rt.client.extension.ui.form.fields.wrappedform;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.wrappedform.WrappedFormFieldChains.WrappedFormFieldInnerFormChangedChain;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;

public interface IWrappedFormFieldExtension<FORM extends IForm, OWNER extends AbstractWrappedFormField<FORM>> extends IFormFieldExtension<OWNER> {

  void execInnerFormChanged(WrappedFormFieldInnerFormChangedChain<FORM> chain, FORM oldInnerForm, FORM newInnerForm);
}
