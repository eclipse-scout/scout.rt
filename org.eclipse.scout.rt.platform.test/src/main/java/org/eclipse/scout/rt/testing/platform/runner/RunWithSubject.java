/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.security.SimplePrincipal;

/**
 * Annotation to execute test-methods under a particular user. This is used in conjunction with
 * {@link PlatformTestRunner} and its subclasses.
 * <p>
 * It runs the tests inside {@link Subject#doAs(Subject, java.security.PrivilegedAction)} with a {@link SimplePrincipal}
 *
 * @since 5.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface RunWithSubject {

  String value();
}
