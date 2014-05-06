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
package org.eclipse.scout.rt.client.ui.form.fields.booleanfield;


public interface IBooleanFieldUIFacade {

  /**
   * Sets the selection of the BooleanField by using the toggled current selection as
   * an input value. The return value is the output value calculated by the model.
   * 
   * @return the new calculated selection of the model.
   * @since 4.0.0-M7
   */
  boolean setSelectedFromUI();
}
