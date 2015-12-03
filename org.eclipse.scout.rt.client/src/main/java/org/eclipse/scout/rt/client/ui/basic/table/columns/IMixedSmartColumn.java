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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

public interface IMixedSmartColumn<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> extends IContentAssistColumn<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> {

  /**
   * see {@link #setSortCodesByDisplayText(boolean)}
   * 
   * @since 04.11.2009
   */
  boolean isSortCodesByDisplayText();

  /**
   * Sorting of columns with attached {@link ICodeType} can be based on the codes sort order or their display texts.
   * Default is sort by codes sort order.
   * 
   * @since 04.11.2009 ticket 82478
   */
  void setSortCodesByDisplayText(boolean b);
}
