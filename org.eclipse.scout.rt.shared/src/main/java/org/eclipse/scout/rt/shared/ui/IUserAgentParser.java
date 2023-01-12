/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
