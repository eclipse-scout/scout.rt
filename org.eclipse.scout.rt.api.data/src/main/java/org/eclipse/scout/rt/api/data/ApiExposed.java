/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks elements that should be exposed by the Scout REST api.
 * <p>
 * Important: If this annotation is added to a supported Scout element (like a CodeType), this element is exposed to the
 * Scout REST api by default and is therefore accessible e.g. by the browser. Do not add this annotation to elements
 * that contain sensitive or privacy relevant data!
 * <p>
 * Typically, an {@link IApiExposedItemContributor} is implemented which contributes all elements having this
 * annotation.
 */
@Inherited
@Documented
@Target({TYPE, FIELD, METHOD})
@Retention(RUNTIME)
public @interface ApiExposed {
}
