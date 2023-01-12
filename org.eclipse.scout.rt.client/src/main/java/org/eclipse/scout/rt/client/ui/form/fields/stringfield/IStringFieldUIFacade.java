/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.stringfield;

import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicFieldUIFacade;

public interface IStringFieldUIFacade extends IBasicFieldUIFacade {

  void fireActionFromUI();

  void setSelectionFromUI(int startOfSelection, int endOfSelection);

  TransferObject fireDragRequestFromUI();

  void fireDropActionFromUi(TransferObject scoutTransferable);
}
