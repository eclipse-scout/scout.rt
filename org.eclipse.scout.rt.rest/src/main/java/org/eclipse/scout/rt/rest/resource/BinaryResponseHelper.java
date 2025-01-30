/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.resource;

import java.io.InputStream;

import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.DownloadResponseHelper;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * Helper class used by REST resources.
 */
@ApplicationScoped
public class BinaryResponseHelper {

  public static final String IMAGE_TYPES = "image/jpeg, image/gif, image/png";
  public static final String ZIP_TYPE = "application/zip";

  /**
   * Creates a response with streaming binary data and no-cache headers. Use this method to return large data / files.
   */
  public Response binaryResponse(InputStream in, String filename, String mimeType) {
    return binaryResponse(Response.ok(in, mimeType), filename, null);
  }

  /**
   * Creates a response with streaming data and no-cache headers.
   */
  public Response binaryResponse(ResponseBuilder builder, String filename) {
    return binaryResponse(builder, filename, null);
  }

  /**
   * Creates a response with streaming data and cache-control headers.
   */
  public Response binaryResponse(ResponseBuilder builder, String filename, CacheControl cacheControl) {
    cacheControl = ObjectUtility.nvlOpt(cacheControl, this::createNoCacheControl);
    builder.cacheControl(cacheControl);

    BEANS.get(DownloadResponseHelper.class).getDownloadHeaders(filename).forEach((header, value) -> builder.header(header, value));

    return builder.build();
  }

  /**
   * Creates a response with binary data and no-cache headers.
   */
  public Response binaryResponse(byte[] data, String mimeType) {
    return binaryResponse(data, mimeType, null);
  }

  /**
   * Creates a response with binary data and cache-control headers.
   */
  public Response binaryResponse(byte[] data, String mimeType, CacheControl cacheControl) {
    cacheControl = ObjectUtility.nvlOpt(cacheControl, this::createNoCacheControl);
    return Response.ok(data, mimeType)
        .cacheControl(cacheControl)
        .build();
  }

  public CacheControl createNoCacheControl() {
    CacheControl cc = new CacheControl();
    cc.setNoCache(true);
    cc.setMustRevalidate(true);
    cc.setNoStore(true);
    return cc;
  }
}
