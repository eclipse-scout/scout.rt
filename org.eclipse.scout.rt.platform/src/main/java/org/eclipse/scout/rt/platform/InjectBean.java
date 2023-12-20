/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is the scout platform simplified version of jakarta.inject.Inject in the sense that it allows for
 * injecting beans into constructor parameters, method parameters, and fields.
 * <p>
 * Other than jakarta.inject.Inject it does only support for the scope {@link ApplicationScoped} which is similar to
 * jakarta.inject.Singleton.
 * <p>
 * {@link InjectBean} can also be used to have non-scoped beans injected (will inject a new instance of the bean for
 * every call to BEANS.get()). Only with regard to a <i>specific scope</i> scout is supporting the one and only scope
 * called {@link ApplicationScoped}.
 * <p>
 * There is no pendant to the jakarta.inject.Provider since this kind of code can easily be achieved by using lazy
 * {@link BEANS#get(Class)}
 *
 * @since 7.0
 */
@Target({METHOD, CONSTRUCTOR, FIELD})
@Retention(RUNTIME)
@Documented
public @interface InjectBean {

}
