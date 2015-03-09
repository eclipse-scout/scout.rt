/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.cdi.BeanInvocationHint;

/**
 * Marks an interface (typically a service) that is capable of being called as client proxy to the back-end server if
 * there is
 * no real implementation available for the service
 */
@BeanInvocationHint
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TunnelToServer {

}
