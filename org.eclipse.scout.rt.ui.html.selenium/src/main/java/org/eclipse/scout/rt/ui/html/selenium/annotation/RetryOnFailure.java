/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.selenium.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.ui.html.selenium.junit.RetryOnFailureRule;

/**
 * Indicates that the annotated test (or tests if used on class level) should be retried automatically when it fails.
 * Only after all of the given number of retries have failed, the test is reported as broken.
 *
 * @see RetryOnFailureRule
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface RetryOnFailure {
  /**
   * Number of retries (default is 1)
   */
  int value() default 1;
}
