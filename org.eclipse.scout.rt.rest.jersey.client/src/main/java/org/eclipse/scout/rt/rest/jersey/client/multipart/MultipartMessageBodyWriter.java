/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.jersey.client.multipart;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.rest.client.IGlobalRestClientConfigurator;
import org.eclipse.scout.rt.rest.client.multipart.MultipartMessage;
import org.eclipse.scout.rt.rest.client.multipart.MultipartPart;

/**
 * {@link MessageBodyWriter} for {@link MultipartMessage} used on the client side (sending).
 * <p>
 * Own implementation due to the lack of multipart support in JAX-RS. This implementation works stream-based, i.e.
 * doesn't require persisted files or memory allocation for the parts. It doesn't rely on any additional dependencies.
 * <p>
 * Expects that the media type contains already a boundary parameter, as added when {@link MultipartMessage#toEntity()}
 * is used.
 * <p>
 * Regarding imports, this class must not be part of the module <code>org.eclipse.scout.rt.rest.jersey.client</code> but
 * could reside in <code>org.eclipse.scout.rt.rest</code> too. Because there is no direct access to this class, this
 * module is used instead to hide implementation details.
 * <p>
 * Idea from <a href="https://guntherrotsch.github.io/blog_2021/jaxrs-multipart-client.html">JAX/RS Multipart Client by
 * Gunther Rotsch</a>. Scout uses a different approach via direct stream processing instead of working with temporary
 * files. {@link StandardCharsets#UTF_8} encoding is used instead of {@link StandardCharsets#US_ASCII}.
 */
public class MultipartMessageBodyWriter implements MessageBodyWriter<MultipartMessage> {

  private static final String HTTP_LINE_DELIMITER = "\r\n";

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return MultipartMessage.class.isAssignableFrom(type) && mediaType.isCompatible(MediaType.MULTIPART_FORM_DATA_TYPE);
  }

  @Override
  public void writeTo(MultipartMessage t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {

    String boundary = assertNotNull(mediaType.getParameters().get(MultipartMessage.BOUNDARY_PARAMETER),
        "boundary is missing, make sure to set media type via {}", MultipartMessage.class.getSimpleName() + "#toEntity()");

    // parts
    t.getParts().forEach(part -> writePart(boundary, entityStream, part));

    // end boundary
    write(entityStream, "--" + boundary + "--" + HTTP_LINE_DELIMITER);
  }

  /**
   * Writes a single part to the entity stream containing
   * <ol>
   * <li>Start boundary</li>
   * <li>Content headers</li>
   * <li>Content</li>
   * </ol>
   * each separated by the HTTP line delimiter and ending with the HTTP line delimiter.
   */
  protected void writePart(String boundary, OutputStream entityStream, MultipartPart part) {
    try {
      // start boundary
      write(entityStream, "--" + boundary + HTTP_LINE_DELIMITER);

      // headers
      for (String contentHeader : getContentHeaders(part)) {
        write(entityStream, contentHeader + HTTP_LINE_DELIMITER);
      }

      write(entityStream, HTTP_LINE_DELIMITER);

      // content
      try (InputStream contentStream = part.getInputStream()) {
        contentStream.transferTo(entityStream);
      }

      write(entityStream, HTTP_LINE_DELIMITER);
    }
    catch (IOException e) {
      throw new PlatformException("Failed to write part", e);
    }
  }

  /**
   * Writes the given string content with {@link StandardCharsets#UTF_8} encoding.
   * <p>
   * Use for content that only uses 7-bit ASCII anyway (boundary) or part headers including part and filenames. These
   * are encoded as UTF-8 and not by using US-ASCII.
   */
  protected void write(OutputStream entityStream, String content) throws IOException {
    entityStream.write(content.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Returns the content header for the given parts, which includes 'Content-Disposition' and if available the
   * 'Content-Type'.
   */
  protected List<String> getContentHeaders(MultipartPart part) {
    List<String> headers = new ArrayList<>(2);

    String contentDisposition = "Content-Disposition: form-data; name=\"" + part.getPartName() + "\"";
    if (part.getFilename() != null) {
      contentDisposition += "; filename=\"" + part.getFilename() + "\"";
    }

    headers.add(contentDisposition);

    if (part.getContentType() != null) {
      headers.add("Content-Type: " + part.getContentType());
    }

    return headers;
  }

  /**
   * {@link IGlobalRestClientConfigurator} implementation registering {@link MultipartMessageBodyWriter}.
   */
  public static class ScoutMultipartClientConfigurator implements IGlobalRestClientConfigurator {

    @Override
    public void configure(ClientBuilder clientBuilder) {
      clientBuilder.register(MultipartMessageBodyWriter.class);
    }
  }
}
