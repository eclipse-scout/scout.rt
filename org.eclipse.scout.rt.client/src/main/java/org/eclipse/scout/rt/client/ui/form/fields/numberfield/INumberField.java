/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.numberfield;

import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.scout.rt.client.ui.valuecontainer.INumberValueContainer;

/**
 * Field type representing a number
 *
 * @param <NUMBER>
 */
public interface INumberField<NUMBER extends Number> extends IBasicField<NUMBER>, INumberValueContainer<NUMBER> {

  String PROP_LENIENT_DECIMAL_SEPARATORS = "lenientDecimalSeparators";

  String PROP_LENIENT_GROUPING_SEPARATORS = "lenientGroupingSeparators";

  /**
   * Sets the possible decimal separators which should be considered for lenient parsing. Value <code>null</code> or an
   * empty list means lenient parsing is disabled.
   */
  void setLenientDecimalSeparators(Set<Character> lenientDecimalSeparators);

  Set<Character> getLenientDecimalSeparators();

  /**
   * Sets the possible grouping separators which should be considered for lenient parsing. Value <code>null</code> or an
   * empty list means lenient parsing is disabled.
   */
  void setLenientGroupingSeparators(Set<Character> lenientGroupingSeparators);

  Set<Character> getLenientGroupingSeparators();
}
