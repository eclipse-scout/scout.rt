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

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

/**
 * Provides a lookup row.
 */
@FunctionalInterface
interface ILookupRowByKeyProvider<LOOKUP_KEY> {

  /**
   * @return {@link ILookupRow}, <code>null</code>, if not found
   */
  ILookupRow<LOOKUP_KEY> getLookupRow(LOOKUP_KEY key);
}
