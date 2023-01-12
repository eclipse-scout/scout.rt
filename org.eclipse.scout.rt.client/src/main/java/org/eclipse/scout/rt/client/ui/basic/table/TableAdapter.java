/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table;

/**
 * inside gui handling or in non-model threads don't use this adapter because it might reduce performance when batch
 * events are handled as single events
 */
public class TableAdapter implements TableListener {

  @Override
  public void tableChanged(TableEvent e) {
    // expected to be overridden by subclass
  }
}
