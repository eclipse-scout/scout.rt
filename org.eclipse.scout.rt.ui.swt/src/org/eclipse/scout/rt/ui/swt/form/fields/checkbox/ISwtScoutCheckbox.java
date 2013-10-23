/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.form.fields.checkbox;

import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.ui.swt.ext.ILabelComposite;
import org.eclipse.scout.rt.ui.swt.ext.MultilineCheckbox;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;

/**
 * <h3>ISwtScoutCheckbox</h3> ...
 * 
 * @since 1.0.0 14.04.2008
 */
public interface ISwtScoutCheckbox extends ISwtScoutFormField<IBooleanField> {

  @Override
  MultilineCheckbox getSwtField();

  ILabelComposite getPlaceholderLabel();
}
