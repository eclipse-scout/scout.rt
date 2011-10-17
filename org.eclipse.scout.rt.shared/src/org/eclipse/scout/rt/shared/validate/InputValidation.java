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
package org.eclipse.scout.rt.shared.validate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use any of the {@link IValidationStrategy} values or a custom value
 * <p>
 * Method annotation on server service methods. This annotation is read by the DefaultValidator in the
 * DefaultTransactionDelegate to perform central input validation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface InputValidation {

  /**
   * Any of the {@link IValidationStrategy} values or a custom strategy
   */
  Class<? extends IValidationStrategy> value();

}
