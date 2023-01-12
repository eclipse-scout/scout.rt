/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.BeanInvocationHint;

/**
 * Marks an interface (typically a service) that is capable of being called as client proxy to the back-end server if
 * there is no real implementation available for the service
 * <p>
 * Note that this annotation is NOT inherited to sub interfaces!
 */
@Documented
@BeanInvocationHint
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TunnelToServer {

}
