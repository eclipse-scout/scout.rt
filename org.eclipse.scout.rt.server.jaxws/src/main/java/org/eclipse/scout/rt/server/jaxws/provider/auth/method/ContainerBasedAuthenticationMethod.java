/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.provider.auth.method;

/**
 * Use this authentication method when the request has already been reliably authenticated by some external system prior
 * to accessing the application, like authenticated by the application server, or a Servlet filter.
 *
 * @deprecated use {@link PreAuthenticationMethod}; will be removed in 7.1.x
 * @since 5.2
 */
@Deprecated
public class ContainerBasedAuthenticationMethod extends PreAuthenticationMethod {
}
