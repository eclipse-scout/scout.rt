/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.container;

import jakarta.ws.rs.container.ContainerResponseFilter;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.rest.RestApplication;
import org.eclipse.scout.rt.rest.RestApplicationContributors;

/**
 * Global {@link ContainerResponseFilter} marked as {@link Bean}.
 * <p>
 * Implementations of this interface are automatically registered within the {@link RestApplication}.
 *
 * @see ContainerResponseFilter
 * @see RestApplicationContributors.RestContainerResponseFilterContributor
 */
@Bean
public interface IRestContainerResponseFilter extends ContainerResponseFilter {
}
