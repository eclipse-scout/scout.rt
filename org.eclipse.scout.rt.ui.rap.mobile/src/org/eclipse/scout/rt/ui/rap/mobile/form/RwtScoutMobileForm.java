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
package org.eclipse.scout.rt.ui.rap.mobile.form;

import org.eclipse.scout.rt.ui.rap.form.RwtScoutForm;

/**
 * @since 3.8.0
 */
public class RwtScoutMobileForm extends RwtScoutForm {

  @Override
  public void setInitialFocus() {
    // If a textfield gets the focus the keyboard pops up.
    // This does not seem to work when opening a form, at least with iOS. The focus is set but no keyboard is shown.
    // But a keyboard popup on the form opening isn't the wanted behaviour anyway. So that's why this function does nothing.
  }

}
