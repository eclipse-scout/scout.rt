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
package org.eclipse.scout.rt.ui.rap.form.fields.placeholder;

import org.eclipse.scout.rt.client.ui.form.fields.placeholder.IPlaceholderField;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.swt.widgets.Composite;

public class RwtScoutPlaceholderField extends RwtScoutFieldComposite<IPlaceholderField> implements IRwtScoutPlaceholderField {

  @Override
  protected void initializeUi(Composite parent) {
    super.initializeUi(parent);
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    setUiContainer(container);
  }
}
