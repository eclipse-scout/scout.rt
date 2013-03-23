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

package org.eclipse.scout.testing.client.form;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;

/**
 * Deprecated: use {@link org.eclipse.scout.rt.testing.client.form.DynamicForm} instead
 * will be removed with the L-Release.
 */
@Deprecated
public class DynamicForm extends org.eclipse.scout.rt.testing.client.form.DynamicForm {

  /**
   * @param title
   * @param mainBox
   * @throws ProcessingException
   */
  public DynamicForm(String title, IGroupBox mainBox) throws ProcessingException {
    super(title, mainBox);
  }
}
