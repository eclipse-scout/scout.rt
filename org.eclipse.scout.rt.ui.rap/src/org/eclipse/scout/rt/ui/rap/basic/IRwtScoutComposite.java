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
package org.eclipse.scout.rt.ui.rap.basic;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface IRwtScoutComposite<T extends IPropertyObserver> {
  String PROP_RWT_SCOUT_COMPOSITE = "IRwtScoutComposite";

  void createUiField(Composite parent, T model, IRwtEnvironment environment);

  boolean isCreated();

  boolean isUiDisposed();

  T getScoutObject();

  IRwtEnvironment getUiEnvironment();

  Control getUiField();

  Composite getUiContainer();

  /**
   * is not thought to override. This method is to call to dispose a composite.
   */
  void dispose();

}
