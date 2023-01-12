/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates to run the annotated target (method, class) on behalf of a {@link RunContext} as produced by
 * {@link RunContextProducer}.
 *
 * @since 5.1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RunWithRunContext {

  /**
   * The producer to produce a {@link RunContext}. By default, {@link RunContextProducer} is returned.
   */
  Class<? extends RunContextProducer> value() default RunContextProducer.class;
}
