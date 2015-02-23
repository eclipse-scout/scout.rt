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
package org.eclipse.scout.rt.ui.swt.form.fields.placeholder;

import org.eclipse.scout.rt.client.ui.form.fields.placeholder.IPlaceholderField;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.swt.widgets.Composite;

public class SwtScoutPlaceholderField extends SwtScoutFieldComposite<IPlaceholderField> implements ISwtScoutPlaceholderField {

  @Override
  protected void initializeSwt(Composite parent) {
    super.initializeSwt(parent);
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    setSwtContainer(container);
  }
}
