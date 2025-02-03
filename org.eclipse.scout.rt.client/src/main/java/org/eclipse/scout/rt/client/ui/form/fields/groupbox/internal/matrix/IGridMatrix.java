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

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
@FunctionalInterface
public interface IGridMatrix {

  boolean computeGridData(List<IFormField> fields);
}
