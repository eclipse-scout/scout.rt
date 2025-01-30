/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.log;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies that REST calls to the annotated target should not be logged to the application's <i>access log</i>.
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface NoLog {

  /**
   * @return <code>true</code> to disable logging for this target. This is the default value. It can be set to
   * <code>false</code> to re-enable the logging.
   */
  boolean value() default true;
}
