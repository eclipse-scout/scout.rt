/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.server;

import static java.util.Collections.singleton;
import static org.eclipse.scout.rt.rest.RestApplicationContributors.lookupBeanClasses;

import java.util.Set;

import jakarta.inject.Singleton;

import org.eclipse.scout.rt.rest.IRestResource;
import org.eclipse.scout.rt.rest.RestApplication.IRestApplicationSingletonsContributor;
import org.glassfish.jersey.inject.hk2.AbstractBinder;

/**
 * Registers all {@link IRestResource} implementations in {@link Singleton} scope,
 * allowing Jersey/HK2 to reuse a single instance per resource.
 */
public class ResourceBinderContributor implements IRestApplicationSingletonsContributor {
  @Override
  public Set<Object> contribute() {
    return singleton(new AbstractBinder() {
      @Override
      protected void configure() {
        lookupBeanClasses(IRestResource.class)
            .forEach(resourceClass -> bindAsContract(resourceClass)
                .in(Singleton.class));
      }
    });
  }
}
