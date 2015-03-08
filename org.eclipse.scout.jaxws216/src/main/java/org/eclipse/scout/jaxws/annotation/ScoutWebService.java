/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.jaxws.internal.resolver.ScoutInstanceResolver;
import org.eclipse.scout.jaxws.security.provider.BasicAuthenticationHandler;
import org.eclipse.scout.jaxws.security.provider.ConfigIniAuthenticator;
import org.eclipse.scout.jaxws.security.provider.IAuthenticationHandler;
import org.eclipse.scout.jaxws.security.provider.IAuthenticator;

import com.sun.xml.internal.ws.api.server.InstanceResolverAnnotation;

/**
 * Annotation to specify authentication mechanism and authenticator on a port type. By default,
 * {@link BasicAuthenticationHandler} and {@link ConfigIniAuthenticator} are used.
 */
@SuppressWarnings("restriction")
@Target(ElementType.TYPE)
@InstanceResolverAnnotation(ScoutInstanceResolver.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ScoutWebService {
  Class<? extends IAuthenticationHandler> authenticationHandler() default BasicAuthenticationHandler.class;

  Class<? extends IAuthenticator> authenticator() default ConfigIniAuthenticator.class;
}
