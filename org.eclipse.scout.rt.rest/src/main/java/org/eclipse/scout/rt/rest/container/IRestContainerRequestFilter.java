/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.container;

import javax.ws.rs.container.ContainerRequestFilter;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.rest.RestApplication;
import org.eclipse.scout.rt.rest.RestApplicationContributors;

/**
 * Global {@link ContainerRequestFilter} marked as {@link Bean}.
 * <p>
 * Implementations of this interface are automatically registered within the {@link RestApplication}.
 *
 * @see ContainerRequestFilter
 * @see RestApplicationContributors.RestContainerRequestFilterContributor
 */
@Bean
public interface IRestContainerRequestFilter extends ContainerRequestFilter {
}
