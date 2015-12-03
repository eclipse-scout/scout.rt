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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public class ContentAssistFormEvent extends FormEvent {

  public static final int TYPE_NEW_LINK_EVENT = 10000;

  private static final long serialVersionUID = 1L;

  /**
   * @param form
   * @param type
   */
  public ContentAssistFormEvent(IForm form, int type) {
    super(form, type);
  }

  /**
   * @param form
   * @param type
   * @param causingField
   */
  public ContentAssistFormEvent(IForm form, int type, IFormField causingField) {
    super(form, type, causingField);
  }

}
