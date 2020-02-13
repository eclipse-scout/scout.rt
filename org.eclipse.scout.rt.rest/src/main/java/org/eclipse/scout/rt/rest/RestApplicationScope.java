/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
