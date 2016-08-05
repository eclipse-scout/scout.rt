/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates SOAP envelopes for {@link IServiceTunnelRequest} and {@link IServiceTunnelResponse} objects.<br>
 * This fast hi-speed encoder/decoder ignores xml structure and reads content of first &lt;data&gt; tag directly.
 * <p>
 * Example request:
 *
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <soapenv:Envelope soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:soapenv=
"http://schemas.xmlsoap.org/soap/envelope/">
 *   <soapenv:Body>
 *     <request version="3.0.0" format="de_CH" language="de_CH" service=
"org.eclipse.scout.rt.shared.services.common.ping.IPingService" operation="ping"/>
 *     <data>...</data>
 *     <info ts="20080715114301917" origin=
"192.168.1.105">For maximum performance, data is reduced, compressed and base64 encoded.</info>
 *   </soapenv:Body>
 * </soapenv:Envelope>
 * }
 * </pre>
 * <p>
 * Example response (success):
 *
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <soapenv:Envelope soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:soapenv=
"http://schemas.xmlsoap.org/soap/envelope/">
 *   <soapenv:Body>
 *     <response status="OK" type="String"/>
 *     <data>...</data>
 *     <info ts="20080715114301917" origin=
"192.168.3.2">For maximum performance, data is reduced, compressed and base64 encoded.</info>
 *   </soapenv:Body>
 * </soapenv:Envelope>
 * }
 * </pre>
 *
 * Example response (error):
 *
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <soapenv:Envelope soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:soapenv=
"http://schemas.xmlsoap.org/soap/envelope/">
 *   <soapenv:Body>
 *     <response status="ERROR">
 *       <exception type="SecurityException">Access denied</exception>
 *     </response>
 *     <data>...</data>
 *     <info ts="20080715114301917" origin=
"192.168.3.2">For maximum performance, data is reduced, compressed and base64 encoded.</info>
 *   </soapenv:Body>
 * </soapenv:Envelope>
 * }
 * </pre>
 *
 * In order to enable/disable content compression, use the system property or config.properties property:
 * <code>org.eclipse.scout.serviceTunnel.compress=true</code>
 * <p>
 * If the client side sets this property to true, all client data is sent to the server with compressed data (false
 * accordingly).<br>
 * If the client side does not set this property at all, data is sent to server in the same mode as it is received from
 * the server.<br>
 * Same is valid for the server side.
 * <p>
 * The default is true.
 * <p>
 * To enable debug output only for this class, use logger specific parameters.
 * <p>
 * This implementation can by used by an application by subclassing this class and adding the {@link Replace}
 * annotation.
 */
@Order(5100)
public class SoapServiceTunnelContentHandler extends AbstractServiceTunnelContentHandler {
  private static final Logger LOG = LoggerFactory.getLogger(SoapServiceTunnelContentHandler.class);
  private static final Pattern BEGIN_DATA_TAG = Pattern.compile("[<]([a-zA-Z0-9]+:)?data\\s*>");
  private static final Pattern END_DATA_TAG = Pattern.compile("[<][/]([a-zA-Z0-9]+:)?data\\s*>");
  private static final Pattern COMPRESSED_ATTRIBUTE = Pattern.compile("compressed\\s*=\\s*\"(true|false)\"");
  private static final String CONTENT_TYPE = "text/xml";

  private String m_originAddress;
  private Boolean m_receivedCompressed;

  @Override
  public void initialize() {
    super.initialize();
    try {
      m_originAddress = InetAddress.getLocalHost().getHostAddress();
    }
    catch (UnknownHostException | RuntimeException e) {
      LOG.warn("Could not determine local ip address", e);
    }
  }

  @Override
  public String getContentType() {
    return CONTENT_TYPE;
  }

  @Override
  public void writeRequest(OutputStream stream, ServiceTunnelRequest msg) throws Exception {
    boolean debugEnabled = LOG.isDebugEnabled();
    if (debugEnabled) {
      stream = new DebugOutputStream(stream);
    }
    try (OutputStreamWriter out = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {

      // build soap message without sax (hi-speed)
      boolean compressed = isUseCompression();
      out.write("<soapenv:Envelope soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsse=\"http://schemas.xmlsoap.org/ws/2002/04/secext\">\n");
      out.write("<soapenv:Body>\n");
      out.write("  <request compressed=\"");
      out.write(Boolean.toString(compressed));
      out.write("\" locale=\"");
      out.write(msg.getLocale().toString());
      out.write("\" service=\"");
      out.write(msg.getServiceInterfaceClassName());
      out.write("\" operation=\"");
      out.write(msg.getOperation());
      out.write("\"/>\n");
      out.write("  <data>");

      long y = System.nanoTime();
      setData(out, msg, compressed);
      y = System.nanoTime() - y;
      if (debugEnabled) {
        LOG.debug("message encoding took {} nanoseconds", y);
      }

      out.write("</data>\n");
      out.write("  <info");
      out.write(" origin=\"" + m_originAddress + "\"");
      out.write("/>\n");
      out.write("</soapenv:Body>");
      out.write("</soapenv:Envelope>");
    }
    finally {
      if (debugEnabled) {
        String sentData = ((DebugOutputStream) stream).getContent(StandardCharsets.UTF_8.name());
        int lastWrittenCharacter = ((DebugOutputStream) stream).getLastWrittenCharacter();
        Throwable lastThrownException = ((DebugOutputStream) stream).getLastThrownException();
        LOG.debug("lastWrittenCharacter={}, lastThrownException={}, sentData: {}", lastWrittenCharacter, lastThrownException, sentData);
      }
    }
  }

  @Override
  public void writeResponse(OutputStream stream, ServiceTunnelResponse msg) throws Exception {
    boolean debugEnabled = LOG.isDebugEnabled();
    if (debugEnabled) {
      stream = new DebugOutputStream(stream);
    }
    try (OutputStreamWriter out = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {

      // build soap message without sax (hi-speed)
      boolean compressed = isUseCompression();
      out.write("<soapenv:Envelope soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
      out.write("<soapenv:Body>\n");
      if (msg.getException() == null) {
        out.write("  <response status=\"OK\"");
        Object x = msg.getData();
        if (x != null) {
          out.write(" type=\"" + x.getClass().getSimpleName() + "\"");
        }
        else {
          out.write(" type=\"\"");
        }
        out.write(" compressed=\"" + compressed + "\"");
        out.write("/>\n");
      }
      else {
        out.write("  <response status=\"ERROR\"");
        out.write(" compressed=\"" + compressed + "\"");
        out.write(">\n");
        out.write("    <exception type=\"" + msg.getException().getClass().getSimpleName() + "\">");
        out.write(msg.getException().getMessage());
        out.write("</exception>\n");
        out.write("  </response>\n");
      }
      out.write("  <data>");

      long y = System.nanoTime();
      setData(out, msg, compressed);
      y = System.nanoTime() - y;
      if (debugEnabled) {
        LOG.debug("message encoding took {} nanoseconds", y);
      }

      out.write("</data>\n");
      out.write("  <info");
      out.write(" origin=\"" + m_originAddress + "\"");
      out.write("/>\n");
      out.write("</soapenv:Body>");
      out.write("</soapenv:Envelope>");
    }
    finally {
      if (debugEnabled) {
        String sentData = ((DebugOutputStream) stream).getContent(StandardCharsets.UTF_8.name());
        int lastWrittenCharacter = ((DebugOutputStream) stream).getLastWrittenCharacter();
        Throwable lastThrownException = ((DebugOutputStream) stream).getLastThrownException();
        LOG.debug("lastWrittenCharacter={}, lastThrownException={}, sentData: {}", lastWrittenCharacter, lastThrownException, sentData);
      }
    }
  }

  protected void setData(Writer writer, Object msg, boolean compressed) throws IOException {
    Deflater deflater = null;
    try {
      // build serialized data
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      OutputStream out = bos;
      if (compressed) {
        deflater = new Deflater(Deflater.BEST_SPEED);
        out = new DeflaterOutputStream(bos, deflater);
      }
      getObjectSerializer().serialize(out, msg);
      String base64Data = StringUtility.wrapText(Base64Utility.encode(bos.toByteArray()), 10000);
      writer.write(base64Data);
    }
    catch (NotSerializableException e) {
      LOG.error("Error serializing data '{}'", msg);
      throw e;
    }
    finally {
      if (deflater != null) {
        try {
          deflater.end();
        }
        catch (Throwable fatal) { // NOSONAR
        }
      }
    }
  }

  @Override
  public ServiceTunnelRequest readRequest(InputStream in) throws Exception {
    return (ServiceTunnelRequest) read(in);
  }

  @Override
  public ServiceTunnelResponse readResponse(InputStream in) throws Exception {
    return (ServiceTunnelResponse) read(in);
  }

  @SuppressWarnings("resource")
  protected Object/* msg */ read(InputStream in) throws Exception {
    if (LOG.isDebugEnabled()) {
      in = new DebugInputStream(in);
    }
    String dataPart = null;
    boolean compressed = true;
    try {
      Reader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
      StringBuilder buf = new StringBuilder();
      int ch;
      while ((ch = r.read()) >= 0) {
        buf.append((char) ch);
      }
      String xml = buf.toString();
      buf.setLength(0);
      //get the 'compressed' attribute
      Matcher mc = COMPRESSED_ATTRIBUTE.matcher(xml);
      if (mc.find()) {
        compressed = "true".equals(mc.group(1));
        m_receivedCompressed = compressed;
      }
      // simply get the content of <ns:data>{?}</ns:data> or <data>{?}</data>
      Matcher m1 = BEGIN_DATA_TAG.matcher(xml);
      Matcher m2 = END_DATA_TAG.matcher(xml);
      int lastMatchingIndex = 0;
      if (m1.find() && m2.find(m1.start())) {
        do { // we want to be sure that we match the last </data> tag of the
          // message
          lastMatchingIndex = m2.start();
        }
        while (m2.find());
        dataPart = xml.substring(m1.end(), lastMatchingIndex);
      }
      else {
        throw new IOException("missing a data tag");
      }
    }
    finally {
      if (LOG.isDebugEnabled()) {
        String receivedData = ((DebugInputStream) in).getContent(StandardCharsets.UTF_8.name());
        int lastReadCharacter = ((DebugInputStream) in).getLastReadCharacter();
        Throwable lastThrownException = ((DebugInputStream) in).getLastThrownException();
        LOG.debug("lastReadCharacter={}, lastThrownException={}, receivedData:\n{}", lastReadCharacter, lastThrownException, receivedData);
      }
    }
    long y = System.nanoTime();
    Object res = getData(dataPart, compressed);
    y = System.nanoTime() - y;
    LOG.debug("message decoding took {} nanoseconds", y);
    return res;
  }

  protected Object getData(String dataPart, boolean compressed) throws IOException, ClassNotFoundException {
    Inflater inflater = null;
    try {
      // decode serial data
      InputStream in = new ByteArrayInputStream(Base64Utility.decode(dataPart));
      if (compressed) {
        inflater = new Inflater();
        in = new InflaterInputStream(in, inflater);
      }
      return getObjectSerializer().deserialize(in, null);
    }
    finally {
      if (inflater != null) {
        try {
          inflater.end();
        }
        catch (Throwable fatal) { // NOSONAR
        }
      }
    }
  }

  protected boolean isUseCompression() {
    if (isSendCompressed() != null) {
      return isSendCompressed();
    }
    if (m_receivedCompressed != null) {
      return m_receivedCompressed;
    }
    return true;
  }
}
