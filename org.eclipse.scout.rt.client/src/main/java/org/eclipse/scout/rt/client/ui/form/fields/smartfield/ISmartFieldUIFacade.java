/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.rt.client.ui.form.fields.IValueFieldUIFacade;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public interface ISmartFieldUIFacade<VALUE> extends IValueFieldUIFacade<VALUE> {

  void setActiveFilterFromUI(TriState activeFilter);

  void setLookupRowFromUI(ILookupRow<VALUE> lookupRow);
}
