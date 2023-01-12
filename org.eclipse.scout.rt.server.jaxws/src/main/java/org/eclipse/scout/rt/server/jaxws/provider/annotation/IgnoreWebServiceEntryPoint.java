/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.provider.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate an entry point definition with {@link IgnoreWebServiceEntryPoint} to not generate an entry point for that
 * definition.
 * <p>
 * This annotation is to be put as a sibling annotation to {@link WebServiceEntryPoint}, and is primarily used while
 * developing an entry point, or for documentation purpose.
 *
 * @see WebServiceEntryPoint
 * @since 5.2
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Inherited
public @interface IgnoreWebServiceEntryPoint {
}
