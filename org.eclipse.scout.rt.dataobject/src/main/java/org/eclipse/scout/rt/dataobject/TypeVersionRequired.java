/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
