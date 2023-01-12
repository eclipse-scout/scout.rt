/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for {@link IRestResource} that define one or several rest application scopes. A scope assigns a rest
 * resource to a concrete rest application reachable by a certain path.
 *
 * @see {@link RestApplicationScopes}
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE})
@Inherited
public @interface RestApplicationScope {

  String[] value();
}
