/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
