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
package org.eclipse.scout.rt.shared.ui;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Is able to create a string representation (identifier) of {@link UserAgent}. It is also able to parse a identifier
 * and create a {@link UserAgent}.
 *
 * @since 3.8.0
 */
@Bean
public interface IUserAgentParser {

  UserAgent parseIdentifier(String userAgent);

  String createIdentifier(UserAgent userAgent);
}
