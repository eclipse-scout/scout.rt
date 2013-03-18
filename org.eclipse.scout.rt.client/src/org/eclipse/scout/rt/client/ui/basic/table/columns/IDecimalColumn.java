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

/**
 *
 */
public interface IDecimalColumn<T extends Number> extends IColumn<T> {

  void setFormat(String s);

  String getFormat();

  void setMinFractionDigits(int i);

  int getMinFractionDigits();

  void setMaxFractionDigits(int i);

  int getMaxFractionDigits();

  void setGroupingUsed(boolean b);

  boolean isGroupingUsed();

  void setPercent(boolean b);

  boolean isPercent();

  void setFractionDigits(int i);

  int getFractionDigits();

  void setMinValue(T d);

  T getMinValue();

  void setMaxValue(T d);

  T getMaxValue();

  void setMultiplier(int b);

  int getMultiplier();

  NumberFormat getNumberFormat();
}
