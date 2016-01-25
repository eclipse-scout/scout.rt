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
package org.eclipse.scout.rt.client.ui.form;

public interface IFormUIFacade {

  void fireFormActivatedFromUI();

  /**
   * Request to close the form<br>
   * This request might be ignored when the form is not ready for closing or in pending state.
   */
  void fireFormClosingFromUI();

  /**
   * Notification that ui closed the form view<br>
   * This request is a forced close of the form from the ui and closes the form model regardless of its state.
   */
  void fireFormKilledFromUI();

}
