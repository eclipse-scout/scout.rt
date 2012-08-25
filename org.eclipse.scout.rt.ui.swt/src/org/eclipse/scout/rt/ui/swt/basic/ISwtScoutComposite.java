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
package org.eclipse.scout.rt.ui.swt.basic;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>ISwtScoutComposite</h3> ...
 * 
 * @author Andreas Hoegger
 */
public interface ISwtScoutComposite<T extends IPropertyObserver> {
  String PROP_SWT_SCOUT_COMPOSITE = "ISwtScoutComposite";

  void createField(Composite parent, T model, ISwtEnvironment environment);

  T getScoutObject();

  ISwtEnvironment getEnvironment();

  Control getSwtField();

  Composite getSwtContainer();

  boolean isDisposed();

  /**
   * is not thought to override. This method is to call to dispose a composite.
   */
  void dispose();

}
