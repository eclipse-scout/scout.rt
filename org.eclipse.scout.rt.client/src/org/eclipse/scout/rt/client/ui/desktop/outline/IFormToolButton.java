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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;

/**
 * @since 3.8.1
 */
public interface IFormToolButton<FORM> extends IToolButton {
  FORM getForm();

  /**
   * Set a new <b>started</b> form to the tool.
   * <p>
   * The form is shown whenever the tool button is activated.
   */
  void setForm(FORM f);

  /**
   * Set a new <b>started</b> form to the tool.
   * <p>
   * The form is shown whenever the tool button is activated.
   * 
   * @param force
   *          set 'f' as the new form, event when it is equal to the old form
   */
  void setForm(FORM f, boolean force);
}
