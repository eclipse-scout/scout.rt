/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.extension;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.swt.widgets.Composite;

public interface IFormFieldFactory {
  IRwtScoutFormField<?> createUiFormField(Composite parent, IFormField model, IRwtEnvironment uiEnvironment);
}
