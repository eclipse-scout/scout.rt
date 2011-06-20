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
package org.eclipse.scout.rt.shared.data.form;

/**
 * Extendable enum (therefore it is an interface). This enum is read by DefaultInputValidator and DefaultOutputValidator
 * in the
 * DefaultTransactionDelegate
 * to perform central input/output validation.
 */
public abstract interface ValidationStrategy {

  /**
   * Perform no checks on arguments of the annotated method. Use this annotation on a service if you check the arguments
   * yourself.
   */
  int NO_CHECK = 10;
  /**
   * Only perform max length checks on the arguments of the annotated method
   */
  int QUERY = 11;
  /**
   * Perform all checks on the arguments of the annotated method
   */
  int PROCESS = 12;

}
