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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.text.NumberFormat;

import org.eclipse.scout.rt.client.ui.valuecontainer.INumberValueContainer;

public interface INumberColumn<T extends Number> extends IColumn<T>, INumberValueContainer<T> {

  void setValidateOnAnyKey(boolean b);

  boolean isValidateOnAnyKey();

  /**
   * @deprecated Will be removed with scout 5.0. Use {@link #getFormat()}.
   */
  @Deprecated
  NumberFormat getNumberFormat();

}
