/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;

/**
 * Interface required for interaction between JsonOutline and JsonOutlineTable.
 */
@FunctionalInterface
public interface IJsonOutlineAdapter {

  /**
   * Returns the JSON node ID for the given table row.
   */
  String getNodeId(ITableRow tableRow);

}
