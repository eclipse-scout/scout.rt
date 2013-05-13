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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.treefield;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.ui.rap.form.fields.treefield.RwtScoutTreeField;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutActionBar;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileTreeField extends RwtScoutTreeField {

  @Override
  protected IRwtScoutActionBar<? extends IPropertyObserver> createRwtScoutActionBar() {
    RwtScoutTreeActionBar actionBar = new RwtScoutTreeActionBar();
    actionBar.createUiField(getUiContainer(), getScoutObject(), getUiEnvironment());
    return actionBar;
  }
}
