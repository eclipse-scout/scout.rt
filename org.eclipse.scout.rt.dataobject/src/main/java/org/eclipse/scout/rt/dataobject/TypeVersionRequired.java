/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to specify that subclasses/implementations of the annotated class must have a type version.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;TypeVersionRequired
 * public class IMyConfigDo extends IDoEntity {
 *   ...
 * }
 * </pre>
 *
 * @see TypeVersion
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface TypeVersionRequired {

}
