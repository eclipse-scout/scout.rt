/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.Principal;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.EncryptionUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.SoapHandlingUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.serialization.IObjectSerializer;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.shared.Activator;
import org.osgi.framework.Bundle;

/**
 * Creates SOAP envelopes for {@link ServiceTunnelRequest} and {@link ServiceTunnelResponse} objects.<br>
 * Use config.ini property org.eclipse.scout.rt.shared.servicetunnel.debug=true
 * to activate debug info
 * <p>
 * This fast hi-speed encoder/decoder ignores xml structure and reads content of first &lt;data&gt; tag directly.
 * <p>
 * Example request:
 * 
 * <pre>
 * @code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <soapenv:Envelope soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
 *   <soapenv:Body>
 *     <request version="3.0.0" format="de_CH" language="de_CH" service="org.eclipse.scout.rt.shared.services.common.ping.IPingService" operation="ping"/>
 *     <data>...</data>
 *     <info ts="20080715114301917" origin="192.168.1.105">For maximum performance, data is reduced, compressed and base64 encoded.</info>
 *   </soapenv:Body>
 * </soapenv:Envelope>
 * }
 * </pre>
 * 
 * Example response (success):
 * 
 * <pre>
 * @code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <soapenv:Envelope soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
 *   <soapenv:Body>
 *     <response status="OK" type="String"/>
 *     <data>...</data>
 *     <info ts="20080715114301917" origin="192.168.3.2">For maximum performance, data is reduced, compressed and base64 encoded.</info>
 *   </soapenv:Body>
 * </soapenv:Envelope>
 * }
 * </pre>
 * 
 * Example response (error):
 * 
 * <pre>
 * @code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <soapenv:Envelope soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
 *   <soapenv:Body>
 *     <response status="ERROR">
 *       <exception type="SecurityException">Access denied</exception>
 *     </response>
 *     <data>...</data>
 *     <info ts="20080715114301917" origin="192.168.3.2">For maximum performance, data is reduced, compressed and base64 encoded.</info>
 *   </soapenv:Body>
 * </soapenv:Envelope>
 * }
 * </pre>
 * 
 * In order to enable/disable content compression, use the system property or config.ini property:
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
 * To enable debug output only for this class, use logger specific parameters. When using the "simple" scout log manager
 * you may add the following property to the config.ini or as a system property:
 * <code>scout.log.level.org.eclipse.scout.rt.shared.servicetunnel.DefaultServiceTunnelContentHandler=4</code>
 * <p>
 */
public class DefaultServiceTunnelContentHandler implements IServiceTunnelContentHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultServiceTunnelContentHandler.class);
  private static final Pattern BEGIN_DATA_TAG = Pattern.compile("[<]([a-zA-Z0-9]+:)?data\\s*>");
  private static final Pattern END_DATA_TAG = Pattern.compile("[<][/]([a-zA-Z0-9]+:)?data\\s*>");
  private static final Pattern COMPRESSED_ATTRIBUTE = Pattern.compile("compressed\\s*=\\s*\"(true|false)\"");

  private static Boolean COMPRESS;

  static {
    String compressText = null;
    if (Activator.getDefault() != null) {
      compressText = Activator.getDefault().getBundle().getBundleContext().getProperty("org.eclipse.scout.serviceTunnel.compress");
    }
    if ("true".equals(compressText)) {
      COMPRESS = true;
    }
    else if ("false".equals(compressText)) {
      COMPRESS = false;
    }
    else {
      COMPRESS = null;
    }
  }

  private String m_originAddress;
  private Boolean m_sendCompressed;
  private Boolean m_receivedCompressed;
  private final EventListenerList m_listeners;
  private IObjectSerializer m_objectSerializer;

  public DefaultServiceTunnelContentHandler() {
    m_listeners = new EventListenerList();
  }

  @Override
  public void initialize(Bundle[] classResolveBundles, ClassLoader rawClassLoader) {
    try {
      m_originAddress = InetAddress.getLocalHost().getHostAddress();
    }
    catch (Throwable t) {
      // nop
    }
    m_sendCompressed = COMPRESS;
    m_objectSerializer = createObjectSerializer();
  }

  /**
   * @return Creates an {@link IObjectSerializer} instance used for serializing and deserializing data.
   * @since 3.8.2
   */
  protected IObjectSerializer createObjectSerializer() {
    return SerializationUtility.createObjectSerializer(new ServiceTunnelObjectReplacer());
  }

  @Override
  public void writeRequest(OutputStream out, ServiceTunnelRequest msg) throws Exception {
    // build soap message without sax (hi-speed)
    boolean compressed = isUseCompression();
    StringBuilder buf = new StringBuilder();
    String wsse = createWsSecurityElement(msg);
    if (wsse == null) {
      wsse = "";
    }
    buf.append("<soapenv:Envelope soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsse=\"http://schemas.xmlsoap.org/ws/2002/04/secext\">\n");
    buf.append("<soapenv:Header>");
    buf.append(wsse);
    buf.append("</soapenv:Header>\n");
    buf.append("<soapenv:Body>\n");
    buf.append("  <request version=\"");
    buf.append(msg.getVersion());
    buf.append("\" compressed=\"");
    buf.append(compressed);
    buf.append("\" locale=\"");
    buf.append(msg.getLocale().toString());
    buf.append("\" service=\"");
    buf.append(msg.getServiceInterfaceClassName());
    buf.append("\" operation=\"");
    buf.append(msg.getOperation());
    buf.append("\"/>\n");
    buf.append("  <data>");
    long y = System.nanoTime();
    setData(buf, msg, compressed);
    y = System.nanoTime() - y;
    if (LOG.isDebugEnabled()) {
      LOG.debug("message encoding took " + y + " nanoseconds");
    }
    buf.append("</data>\n");
    buf.append("  <info");
    buf.append(" origin=\"" + m_originAddress + "\"");
    buf.append("/>\n");
    buf.append("</soapenv:Body>");
    buf.append("</soapenv:Envelope>");
    //
    if (LOG.isDebugEnabled()) {
      out = new DebugOutputStream(out);
    }
    try {
      out.write(buf.toString().getBytes("UTF-8"));
    }
    finally {
      if (LOG.isDebugEnabled()) {
        String sentData = ((DebugOutputStream) out).getContent("UTF-8");
        int lastWrittenCharacter = ((DebugOutputStream) out).getLastWrittenCharacter();
        Throwable lastThrownException = ((DebugOutputStream) out).getLastThrownException();
        LOG.debug("lastWrittenCharacter=" + lastWrittenCharacter + ",lastThrownException=" + lastThrownException + ", sentData: " + sentData);
      }
    }
  }

  @Override
  public void writeResponse(OutputStream out, ServiceTunnelResponse msg) throws Exception {
    // build soap message without sax (hi-speed)
    boolean compressed = isUseCompression();
    StringBuilder buf = new StringBuilder();
    buf.append("<soapenv:Envelope soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
    buf.append("<soapenv:Body>\n");
    if (msg.getException() == null) {
      buf.append("  <response status=\"OK\"");
      Object x = msg.getData();
      if (x != null) {
        buf.append(" type=\"" + x.getClass().getSimpleName() + "\"");
      }
      else {
        buf.append(" type=\"\"");
      }
      buf.append(" compressed=\"" + compressed + "\"");
      buf.append("/>\n");
    }
    else {
      buf.append("  <response status=\"ERROR\"");
      buf.append(" compressed=\"" + compressed + "\"");
      buf.append(">\n");
      buf.append("    <exception type=\"" + msg.getException().getClass().getSimpleName() + "\">");
      buf.append(msg.getException().getMessage());
      buf.append("</exception>\n");
      buf.append("  </response>\n");
    }
    buf.append("  <data>");
    long y = System.nanoTime();
    setData(buf, msg, compressed);
    y = System.nanoTime() - y;
    if (LOG.isDebugEnabled()) {
      LOG.debug("message encoding took " + y + " nanoseconds");
    }
    buf.append("</data>\n");
    buf.append("  <info");
    buf.append(" origin=\"" + m_originAddress + "\"");
    buf.append("/>\n");
    buf.append("</soapenv:Body>");
    buf.append("</soapenv:Envelope>");
    //
    if (LOG.isDebugEnabled()) {
      out = new DebugOutputStream(out);
    }
    try {
      out.write(buf.toString().getBytes("UTF-8"));
    }
    finally {
      if (LOG.isDebugEnabled()) {
        String sentData = ((DebugOutputStream) out).getContent("UTF-8");
        int lastWrittenCharacter = ((DebugOutputStream) out).getLastWrittenCharacter();
        Throwable lastThrownException = ((DebugOutputStream) out).getLastThrownException();
        LOG.debug("lastWrittenCharacter=" + lastWrittenCharacter + ",lastThrownException=" + lastThrownException + ", sentData: " + sentData);
      }
    }
  }

  protected void setData(StringBuilder buf, Object msg, boolean compressed) throws IOException {
    Deflater deflater = null;
    try {
      // build serialized data
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      OutputStream out = bos;
      if (compressed) {
        deflater = new Deflater(Deflater.BEST_SPEED);
        out = new DeflaterOutputStream(bos, deflater);
      }
      m_objectSerializer.serialize(out, msg);
      String base64Data = StringUtility.wrapText(Base64Utility.encode(bos.toByteArray()), 10000);
      buf.append(base64Data);
    }
    finally {
      if (deflater != null) {
        try {
          deflater.end();
        }
        catch (Throwable fatal) {
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

  protected Object/* msg */read(InputStream in) throws Exception {
    if (LOG.isDebugEnabled()) {
      in = new DebugInputStream(in);
    }
    String dataPart = null;
    boolean compressed = true;
    try {
      Reader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
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
        compressed = mc.group(1).equals("true");
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
        String receivedData = ((DebugInputStream) in).getContent("UTF-8");
        int lastReadCharacter = ((DebugInputStream) in).getLastReadCharacter();
        Throwable lastThrownException = ((DebugInputStream) in).getLastThrownException();
        LOG.debug("lastReadCharacter=" + lastReadCharacter + ",lastThrownException=" + lastThrownException + ", receivedData:\n" + receivedData);
      }
    }
    long y = System.nanoTime();
    Object res = getData(dataPart, compressed);
    y = System.nanoTime() - y;
    if (LOG.isDebugEnabled()) {
      LOG.debug("message decoding took " + y + " nanoseconds");
    }
    return res;
  }

  protected Object getData(String dataPart, boolean compressed) throws IOException, ClassNotFoundException {
    Inflater inflater = null;
    try {
      String base64Data = dataPart.replaceAll("[\\n\\r]", "");
      // decode serial data
      InputStream in = new ByteArrayInputStream(Base64Utility.decode(base64Data));
      if (compressed) {
        inflater = new Inflater();
        in = new InflaterInputStream(in, inflater);
      }
      return m_objectSerializer.deserialize(in, null);
    }
    finally {
      if (inflater != null) {
        try {
          inflater.end();
        }
        catch (Throwable fatal) {
        }
      }
    }
  }

  protected boolean isUseCompression() {
    if (m_sendCompressed != null) {
      return m_sendCompressed;
    }
    if (m_receivedCompressed != null) {
      return m_receivedCompressed;
    }
    return true;
  }

  /**
   * @return the wsse:Security tag. The subject may be null and may contain no principals
   *         <p>
   *         Example WS-Security element for user/pass
   * 
   *         <pre>
   * <wsse:Security soapenv:mustUnderstand="1">
   *   <wsse:UsernameToken>
   *     <wsse:Username>user</wsse:Username>
   *     <wsse:Password Type="http://scout.eclipse.org/security#Base64">ertwtrwet3465t4</wsse:Password>
   *   </wsse:UsernameToken>
   * </wsse:Security>
   * </pre>
   *         <p>
   *         The default calls
   *         {@link DefaultServiceTunnelContentHandler#createDefaultWsSecurityElement(ServiceTunnelRequest)}
   */
  protected String createWsSecurityElement(ServiceTunnelRequest req) {
    return DefaultServiceTunnelContentHandler.createDefaultWsSecurityElement(req);
  }

  private static final byte[] tripleDesKey;
  static {
    String key = Activator.getDefault().getBundle().getBundleContext().getProperty("scout.ajax.token.key");
    if (key == null) {
      tripleDesKey = null;
    }
    else {
      tripleDesKey = new byte[24];
      byte[] keyBytes;
      try {
        keyBytes = key.getBytes("UTF-8");
        System.arraycopy(keyBytes, 0, tripleDesKey, 0, Math.min(keyBytes.length, tripleDesKey.length));
      }
      catch (UnsupportedEncodingException e) {
        LOG.error("reading property 'scout.ajax.token.key'", e);
      }
    }
  }

  /**
   * @return a soap wsse username token. The username is the principal name of the first pricnipal,
   *         the password is the triple-des encoding of "${timestamp}:${username}" using the config.ini parameter
   *         <code>scout.ajax.token.key</code>
   */
  public static final String createDefaultWsSecurityElement(ServiceTunnelRequest req) {
    if (tripleDesKey == null) {
      return null;
    }
    Subject subject = req.getClientSubject();
    if (subject == null || subject.getPrincipals().size() == 0) {
      return null;
    }
    ArrayList<Principal> list = new ArrayList<Principal>(subject.getPrincipals());
    String user = (list.size() > 0 ? list.get(0).getName() : null);
    String pass = (list.size() > 1 ? list.get(1).getName() : null);
    if (user == null) {
      user = "";
    }
    if (pass == null) {
      pass = "";
    }
    String msg = "" + System.currentTimeMillis() + ":" + user;
    try {
      byte[] token;
      token = EncryptionUtility.encrypt(msg.getBytes("UTF-8"), tripleDesKey);
      return SoapHandlingUtility.createWsSecurityUserNameToken(user, token);
    }
    catch (UnsupportedEncodingException e) {
      LOG.error("utf-8 decode failed", e);
    }
    return null;
  }
}
