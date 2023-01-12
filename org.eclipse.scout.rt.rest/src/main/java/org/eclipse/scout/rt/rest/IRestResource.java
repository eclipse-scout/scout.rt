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

import org.eclipse.scout.rt.platform.Bean;

/**
 * Marks a class as REST resource so that it can be found by {@link RestApplication} using jandex.
 */
@Bean
public interface IRestResource {
}
