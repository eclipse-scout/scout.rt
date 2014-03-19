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
package org.eclipse.scout.rt.ui.swing.basic;

import javax.swing.InputVerifier;
import javax.swing.JComponent;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

public interface ISwingScoutComposite<T extends IPropertyObserver> {
  String CLIENT_PROP_SWING_SCOUT_COMPOSITE = "ISwingScoutComposite";

  void createField(T model, ISwingEnvironment environment);

  T getScoutObject();

  ISwingEnvironment getSwingEnvironment();

  JComponent getSwingField();

  JComponent getSwingContainer();

  void connectToScout();

  void disconnectFromScout();

  /**
   * Adds an input verify listener to this composite. Will be notified when this composite gets the verify event.
   * 
   * @param listener
   *          The listener to notify.
   * @see JComponent#setInputVerifier(javax.swing.InputVerifier)
   * @see InputVerifier
   */
  void addInputVerifyListener(ISwingInputVerifyListener listener);

  /**
   * Removes the given verify listener from this composite.
   * 
   * @param listener
   *          The listener to remove.
   */
  void removeInputVerifyListener(ISwingInputVerifyListener listener);

}
