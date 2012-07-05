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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.groupbox;

import org.eclipse.scout.rt.ui.rap.form.fields.groupbox.RwtScoutGroupBox;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileGroupBox extends RwtScoutGroupBox {

  @Override
  protected Composite createButtonbar(Composite parent) {
    if (getScoutObject().getForm().getRootGroupBox() == getScoutObject()) {
      //Button bar of the main box must not be visible
      return null;
    }

    return super.createButtonbar(parent);
  }
}
