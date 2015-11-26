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
package org.eclipse.scout.rt.client.ui.form.fields.stringfield;

import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicFieldUIFacade;

public interface IStringFieldUIFacade extends IBasicFieldUIFacade {

  /**
   * The text passed to this method is tried to wellform as an URL and {@link IStringField#execAction()} will be called.
   * The link action handling has to be done by overriding the execLinkAction on the string field. If the text can not
   * be transformed into an URL the execLinkAction method is called with null.
   *
   * @param text
   */
  void fireActionFromUI();

  void setSelectionFromUI(int startOfSelection, int endOfSelection);

  TransferObject fireDragRequestFromUI();

  void fireDropActionFromUi(TransferObject scoutTransferable);
}
