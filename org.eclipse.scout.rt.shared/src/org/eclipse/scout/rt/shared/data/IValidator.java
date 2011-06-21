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
package org.eclipse.scout.rt.shared.data;

import org.eclipse.scout.rt.shared.data.form.ValidationStrategy;

/**
 * Does input/output validation of arbitrary data.
 */
public interface IValidator {

  /**
   * @return a {@link ValidationStrategy} value or a custom value
   */
  int getValidationStrategy();

  /**
   * @return set a {@link ValidationStrategy} value or a custom value
   */
  void setValidationStrategy(int strategy);

  /**
   * @return default max length for a string (250)
   */
  int getDefaultMaxLengthForString();

  /**
   * set default max length for a string
   */
  void setDefaultMaxLengthForString(int defaultMaxLengthForString);

  /**
   * @return default max length for a char[] (64MB)
   */
  int getDefaultMaxLengthForClob();

  /**
   * set default max length for a char[]
   */
  void setDefaultMaxLengthForClob(int defaultMaxLengthForClob);

  /**
   * @return default max length for a byte[] (64MB)
   */
  int getDefaultMaxLengthForBlob();

  /**
   * set default max length for a byte[]
   */
  void setDefaultMaxLengthForBlob(int defaultMaxLengthForBlob);

  /**
   * @return default max length for arbitrary arrays (10'000 items)
   */
  int getDefaultMaxLengthForArray();

  /**
   * set default max length for arbitrary arrays
   */
  void setDefaultMaxLengthForArray(int defaultMaxLengthForArray);

  /**
   * validate the object and its complete substructure tree
   */
  void validate(Object obj) throws Exception;

  /**
   * Helper: Does default max lenght checks on String, clob/blob (char[], byte[]) and arbitrary arrays using
   * {@link #getDefaultMaxLengthForString()}, {@link #getDefaultMaxLengthForClob()},
   * {@link #getDefaultMaxLengthForBlob()}, {@link #getDefaultMaxLengthForArray()}
   */
  void checkMaxLenghtDefault(Object obj) throws Exception;
}
