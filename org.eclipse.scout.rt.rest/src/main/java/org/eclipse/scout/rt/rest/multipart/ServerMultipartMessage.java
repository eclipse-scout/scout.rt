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
package org.eclipse.scout.rt.rest.multipart;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Not using the implementation available on client side
 * ({@link org.eclipse.scout.rt.rest.client.multipart.MultipartMessage}) due to difference regarding part handling
 * (adding vs. reading via iterator).
 * <p>
 * Own implementation due to the lack of multipart support in JAX-RS. This implementation works stream-based, i.e.
 * doesn't require persisted files or memory allocation for the parts. It doesn't rely on any additional dependencies.
 * Using a {@link MessageBodyReader} doesn't work because the
 * {@link MessageBodyReader#readFrom(Class, Type, Annotation[], MediaType, MultivaluedMap, InputStream)} seems to expect
 * a fully consumed input stream after method is executed (Jersey closes the stream in InboundMessageContext#readEntity,
 * would work for Jetty based applications, but not when running on an application server like Tomcat).
 * <p>
 * Heavily inspired by <a href="https://guntherrotsch.github.io/blog_2021/jaxrs-multipart-server.html">JAX/RS Multipart
 * Server by Gunther Rotsch</a>. Scout uses a different approach via direct stream processing instead of working with
 * temporary files, thus {@link Iterator} with {@link AutoCloseable} is used. {@link StandardCharsets#UTF_8} encoding is
 * used instead of {@link StandardCharsets#US_ASCII}. The parts need to be processed in the given order (consumption of
 * input stream).
 * <p>
 * Keep package-private to avoid (accidental) outside access.
 */
class ServerMultipartMessage implements IMultipartMessage {

  protected static final int EOF = -1;
  protected static final int CR = 13;
  protected static final int LF = 10;

  /**
   * Pattern to remove surrounding quotes from qualifier value.
   */
  protected static final Pattern CONTENT_DISPOSITION_QUALIFIER_VALUE_PATTERN = Pattern.compile("^\"(.*)\"$");

  /**
   * HTTP line delimiter with boundary.
   */
  protected final byte[] m_newlineBoundaryBytes;

  /**
   * Main input stream as provided by
   * {@link MessageBodyReader#readFrom(Class, Type, Annotation[], MediaType, MultivaluedMap, InputStream)}, wrapped
   * within a {@link BufferedInputStream} if it didn't support mark yet.
   */
  protected final InputStream m_inputStream;

  /**
   * {@link PartInputStream} based on {@link #m_inputStream}.
   */
  protected PartInputStream m_partInputStream;

  public ServerMultipartMessage(MediaType mediaType, InputStream inputStream) {
    // boundary used within message is prefixed by -- (see https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html)
    String boundary = "--" + assertNotNull(mediaType.getParameters().get("boundary"), "boundary parameter in media type is required");
    // boundary must always be in 7-bit ASCII (https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html), still use UTF-8, same bytes then
    m_newlineBoundaryBytes = ("\r\n" + boundary).getBytes(StandardCharsets.UTF_8);

    // require an input stream with mark support
    m_inputStream = inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
    m_partInputStream = readStartBoundary(boundary, m_inputStream);
  }

  /**
   * PartInputStream is used to consume the start boundary leading the content headers/body. This works due to the way
   * PartInputStream is implemented, the start boundary is a part with empty part header/body.
   */
  protected PartInputStream readStartBoundary(String boundary, InputStream inputStream) {
    try {
      PartInputStream partInputStream = new PartInputStream(inputStream, boundary.getBytes(StandardCharsets.UTF_8));
      long skipped = partInputStream.skip(Long.MAX_VALUE);
      assertEqual(0L, skipped, "Multipart message must start with boundary: {}", boundary);
      return partInputStream;
    }
    catch (IOException e) {
      throw new PlatformException("Failed to read start boundary", e);
    }
  }

  @Override
  public boolean hasNext() {
    return !m_partInputStream.isLastPart();
  }

  /**
   * The input stream from {@link ServerMultipartPart#getInputStream()} must be fully consumed or otherwise be closed
   * ({@link AutoCloseable} reads the remaining bytes).
   */
  @Override
  public ServerMultipartPart next() {
    assertTrue(hasNext(), "No next part available");

    m_partInputStream = new PartInputStream(m_inputStream, m_newlineBoundaryBytes);
    Map<String, String> contentHeaders = readPartContentHeader(m_partInputStream);
    String contentDispositionHeaderValue = contentHeaders.get("content-disposition");

    Map<String, String> qualifiers = extractPartContentDispositionQualifiers(contentDispositionHeaderValue);
    return new ServerMultipartPart(qualifiers.get("name"), qualifiers.get("filename"), contentHeaders.get("content-type"), m_partInputStream);
  }

  /**
   * Extracts qualifiers from content disposition header value.
   *
   * @return Map key is always lowercase.
   */
  protected Map<String, String> extractPartContentDispositionQualifiers(String headerValue) {
    return Stream.of(headerValue.split(";"))
        .map(parameter -> parameter.split("=", 2))
        .filter(pair -> pair.length == 2) // ignore those not resulting in two parts
        .collect(Collectors.toMap(
            pair -> pair[0].trim().toLowerCase(), // key as lower case
            pair -> CONTENT_DISPOSITION_QUALIFIER_VALUE_PATTERN.matcher(pair[1].trim()).replaceFirst("$1"))); // remove surrounding quotes
  }

  /**
   * Reads the content header as a key-value map.
   *
   * @return Map key is always lowercase.
   */
  protected Map<String, String> readPartContentHeader(PartInputStream inputStream) {
    List<String> headers = readPartHeaderLines(inputStream);
    return headers.stream()
        .map(header -> header.split(":", 2))
        .collect(Collectors.toMap(headerPair -> headerPair[0].trim().toLowerCase(), headerPair -> headerPair[1].trim()));
  }

  /**
   * Read the header lines as string list.
   */
  protected List<String> readPartHeaderLines(PartInputStream is) {
    List<String> lines = new ArrayList<>();
    String line = readLine(is);
    while (!StringUtility.isNullOrEmpty(line)) {
      lines.add(line);
      line = readLine(is);
    }
    return lines;
  }

  /**
   * Reads a single line from the {@link PartInputStream}. A line ends with CRLF or if EOF is reached.
   */
  protected String readLine(PartInputStream is) {
    try {
      int c1 = is.read();
      if (c1 == EOF) {
        return null;
      }

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      int c2 = is.read();
      // searching for end of file (EOF) or sequence of carriage return (CR) and line feed (LF) sequence characters, whichever comes first
      while (c2 != EOF && !(c1 == CR && c2 == LF)) {
        os.write(c1);
        c1 = c2;
        c2 = is.read();
      }

      // Tested with Insomnia/curl and form submit in Firefox/Chrome, all using UTF-8 encoding by default
      return os.toString(StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new PlatformException("Failed to read line", e);
    }
  }

  /**
   * Not using the implementation available on client side
   * ({@link org.eclipse.scout.rt.rest.client.multipart.MultipartPart}) due to difference regarding
   * {@link AutoCloseable}.
   * <p>
   * Keep protected to avoid (accidental) outside access.
   */
  protected static class ServerMultipartPart implements IMultipartPart {

    private final String m_partName;
    private final String m_filename;

    private final String m_contentType;
    private final InputStream m_inputStream;

    public ServerMultipartPart(String partName, String filename, String contentType, InputStream inputStream) {
      m_partName = partName;
      m_filename = filename;
      m_contentType = contentType;
      m_inputStream = inputStream;
    }

    @Override
    public String getPartName() {
      return m_partName;
    }

    @Override
    public String getFilename() {
      return m_filename;
    }

    @Override
    public String getContentType() {
      return m_contentType;
    }

    @Override
    public InputStream getInputStream() {
      return m_inputStream;
    }

    @Override
    public void close() throws Exception {
      m_inputStream.close();
    }
  }

  /**
   * PartInputStream consumes a given input stream until the boundary is reached.
   * <p>
   * The provided input stream must support {@link InputStream#mark(int)} and {@link InputStream#reset()} methods in
   * order to be able to read ahead of characters. This allows the detection of the given boundary while still keep the
   * state of the input stream if only beginning parts of the boundary match.
   * <p>
   * Sources from <a href=
   * "https://github.com/GuntherRotsch/guntherrotsch.github.io/blob/code/jaxrs-multipart/portable-server/src/main/java/net/gunther/wildfly/demo/app/PartInputStream.java">Gunther
   * Rotsch</a>, changes made due to code styling and close handling.
   */
  protected static class PartInputStream extends InputStream {

    protected InputStream m_inputStream;
    protected byte[] m_boundary;

    protected boolean m_endOfPart = false;
    protected boolean m_lastPart = false;

    public PartInputStream(InputStream inputStream, byte[] boundary) {
      assertTrue(inputStream.markSupported(), "inputStream need to support mark and reset methods.");
      m_inputStream = inputStream;
      m_boundary = boundary;
    }

    public boolean isLastPart() {
      return m_lastPart;
    }

    @Override
    public int read() throws IOException {
      if (m_endOfPart) {
        return EOF;
      }

      int c = m_inputStream.read();
      int saved = c;

      m_inputStream.mark(m_boundary.length);

      // the following loop is about to match the boundary, whereby the pos variable is the current position in the boundary array
      int pos = 0;
      do {
        if (c == m_boundary[pos]) {
          // in sequence of boundary
          pos++;
          if (pos == m_boundary.length) {
            // the entire boundary sequence matched with input
            m_endOfPart = true;
            int c1 = checkForLastPart();
            // consume everything till next CR/LF sequence (including) or end of stream (whichever comes first)
            int c2 = m_inputStream.read();
            while (c2 > 0 && !(c1 == CR && c2 == LF)) {
              c1 = c2;
              c2 = m_inputStream.read();
            }
            return EOF;
          }

          // read the next byte to be matched with the boundary
          c = m_inputStream.read();
        }
        else {
          // the input does at this point not match with the boundary, i.e. we reset the
          // stream (and eventually return the initially saved byte)
          m_inputStream.reset();
          break;
        }
      }
      while (c != EOF);

      return saved;
    }

    /**
     * Checks for last part and returns the next byte of the input stream.
     */
    protected int checkForLastPart() throws IOException {
      int c = m_inputStream.read();
      if (c == '-') {
        c = m_inputStream.read();
        if (c == '-') {
          m_lastPart = true;
          c = m_inputStream.read();
        }
      }
      return c;
    }

    @Override
    public void close() throws IOException {
      // Finish reading until the end of the part, otherwise some bytes are left which will cause that the next part contains some bytes of this one,
      // resulting in invalid content header when read by ServerMultipartMessage#readContentHeader (java.lang.ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1).
      // Unfinished reading caused for example by ZipInputStream that doesn't read until the end of the stream, thus this byte skipping is necessary.
      if (!m_endOfPart) {
        //noinspection ResultOfMethodCallIgnored
        skip(Long.MAX_VALUE);
      }
    }
  }
}
