/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.matrix;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
@SuppressWarnings({"squid:S00116", "squid:ClassVariableVisibilityCheck"})
public class Cell {

  public final IFormField field;
  public final GridData fieldGridData;

  public Cell() {
    this(null, null);
  }

  public Cell(IFormField field, GridData gridData) {
    this.field = field;
    fieldGridData = gridData;
  }

  boolean isEmpty() {
    return field == null;
  }
}
