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
package org.eclipse.scout.rt.ui.swt.form.fields.datefield.chooser;

import java.util.EventListener;

/**
 * Class AbstractDateSelectionListener. A listener interested in date changes.
 * 
 * @version 1.0
 */
public abstract class AbstractDateSelectionListener implements EventListener {
  /**
   * @param e
   *          the date changed event
   */
  public abstract void dateChanged(DateSelectionEvent e);
}
