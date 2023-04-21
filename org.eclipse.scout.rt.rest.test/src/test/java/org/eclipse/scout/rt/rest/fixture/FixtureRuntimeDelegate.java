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

import static org.eclipse.scout.rt.platform.util.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
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
    HeaderDelegate<T> delegate = mock(HeaderDelegate.class);
    doAnswer(a -> {
      if (a.getArgument(0) instanceof MediaType) {
        MediaType mediaType = a.getArgument(0);
        assertTrue(mediaType.getParameters().isEmpty(), "toString for media type parameters currently not mocked");
        return mediaType.getType() + "/" + mediaType.getSubtype();
      }
      throw new UnsupportedOperationException("not mocked");
    }).when(delegate).toString(any());
    return delegate;
  }

  @Override
  public Builder createLinkBuilder() {
    return mock(Builder.class);
  }
}
