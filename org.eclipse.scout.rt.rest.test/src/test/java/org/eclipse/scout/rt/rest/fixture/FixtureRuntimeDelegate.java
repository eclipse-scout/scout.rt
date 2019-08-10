/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.fixture;

import static org.mockito.Mockito.mock;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

import org.mockito.Mockito;

/**
 * Test fixture for JAX-RS {@link RuntimeDelegate} that is completely backed by {@link Mockito} mocks. Its only purpose
 * is that other JAX-RS API classes can be instantiated (e.g. {@code HEADER_DELEGATE} in {@link CacheControl}).
 */
public class FixtureRuntimeDelegate extends RuntimeDelegate {

  @Override
  public UriBuilder createUriBuilder() {
    return mock(UriBuilder.class);
  }

  @Override
  public ResponseBuilder createResponseBuilder() {
    return mock(ResponseBuilder.class);
  }

  @Override
  public VariantListBuilder createVariantListBuilder() {
    return mock(VariantListBuilder.class);
  }

  @Override
  public <T> T createEndpoint(Application application, Class<T> endpointType) throws IllegalArgumentException, UnsupportedOperationException {
    return mock(endpointType);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) throws IllegalArgumentException {
    return mock(HeaderDelegate.class);
  }

  @Override
  public Builder createLinkBuilder() {
    return mock(Builder.class);
  }
}
