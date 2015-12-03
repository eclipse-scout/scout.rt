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
package org.eclipse.scout.rt.client.extension.ui.form.fields.button;

import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton;

public abstract class AbstractRadioButtonExtension<T, OWNER extends AbstractRadioButton<T>> extends AbstractButtonExtension<OWNER> implements IRadioButtonExtension<T, OWNER> {

  public AbstractRadioButtonExtension(OWNER owner) {
    super(owner);
  }
}
