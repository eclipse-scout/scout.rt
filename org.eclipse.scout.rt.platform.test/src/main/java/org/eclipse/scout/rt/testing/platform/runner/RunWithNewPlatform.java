/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.testing.platform.runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.testing.platform.TestingDefaultPlatform;

/**
 * Annotation to execute test-methods using a new {@link IPlatform}, not the globally shared Platform. This annotation
 * requires to be executed by the {@link PlatformTestRunner} ore one of its subclasses.
 * <p>
 * If no platform is specified, the {@link TestingDefaultPlatform} will be used. This platform may also be used as a
 * template or super-class whenever using own platforms.
 * </p>
 *
 * @since 5.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface RunWithNewPlatform {
  /**
   * @return the platform class to be used
   */
  Class<? extends IPlatform> platform() default TestingDefaultPlatform.class;
}
