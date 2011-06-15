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
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
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
 * <SOAP-ENV:Envelope SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
 *   <SOAP-ENV:Body>
 *     <request version="3.0.0" format="de_CH" language="de_CH" service="org.eclipse.scout.rt.shared.services.common.ping.IPingService" operation="ping"/>
 *     <data>...</data>
 *     <info ts="20080715114301917" origin="192.168.1.105">For maximum performance, data is reduced, compressed and base64 encoded.</info>
 *   </SOAP-ENV:Body>
 * </SOAP-ENV:Envelope>
 * }
 * </pre>
 * 
 * Example response (success):
 * 
 * <pre>
 * @code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <SOAP-ENV:Envelope SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
 *   <SOAP-ENV:Body>
 *     <response status="OK" type="String"/>
 *     <data>...</data>
 *     <info ts="20080715114301917" origin="192.168.3.2">For maximum performance, data is reduced, compressed and base64 encoded.</info>
 *   </SOAP-ENV:Body>
 * </SOAP-ENV:Envelope>
 * }
 * </pre>
 * 
 * Example response (error):
 * 
 * <pre>
 * @code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <SOAP-ENV:Envelope SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
 *   <SOAP-ENV:Body>
 *     <response status="ERROR">
 *       <exception type="SecurityException">Access denied</exception>
 *     </response>
 *     <data>...</data>
 *     <info ts="20080715114301917" origin="192.168.3.2">For maximum performance, data is reduced, compressed and base64 encoded.</info>
 *   </SOAP-ENV:Body>
 * </SOAP-ENV:Envelope>
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
public class DefaultServiceTunnelContentHandler implements IServiceTunnelContentHandler, IServiceTunnelContentObserver {
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

  private Bundle[] m_bundleList;
  private String m_originAddress;
  private Boolean m_sendCompressed;
  private Boolean m_receivedCompressed;
  private final EventListenerList m_listeners;
  //cache
  private IInboundListener[] m_cachedInListeners;
  private IOutboundListener[] m_cachedOutListeners;

  public DefaultServiceTunnelContentHandler() {
    m_listeners = new EventListenerList();
  }

  public void initialize(Bundle[] classResolveBundles, ClassLoader rawClassLoader) {
    m_bundleList = classResolveBundles;
    try {
      m_originAddress = InetAddress.getLocalHost().getHostAddress();
    }
    catch (Throwable t) {
      // nop
    }
    m_sendCompressed = COMPRESS;
  }

  public void writeRequest(OutputStream out, ServiceTunnelRequest msg) throws Exception {
    // build soap message without sax (hi-speed)
    boolean compressed = isUseCompression();
    StringBuilder buf = new StringBuilder();
    buf.append("<SOAP-ENV:Envelope SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
    buf.append("<SOAP-ENV:Body>\n");
    buf.append("  <request version=\"");
    buf.append(msg.getVersion());
    buf.append("\" compressed=\"");
    buf.append(compressed);
    buf.append("\" format=\"");
    buf.append(msg.getLocale().toString());
    buf.append("\" language=\"");
    buf.append(msg.getNlsLocale().toString());
    buf.append("\" service=\"");
    buf.append(msg.getServiceInterfaceClassName());
    buf.append("\" operation=\"");
    buf.append(msg.getOperation());
    buf.append("\"/>\n");
    buf.append("  <data>");
    long y = System.nanoTime();
    setData(buf, msg, compressed);
    y = System.nanoTime() - y;
    if (LOG.isDebugEnabled()) LOG.debug("message encoding took " + y + " nanoseconds");
    buf.append("</data>\n");
    buf.append("  <info");
    buf.append(" origin=\"" + m_originAddress + "\"");
    buf.append("/>\n");
    buf.append("</SOAP-ENV:Body>");
    buf.append("</SOAP-ENV:Envelope>");
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

  public void writeResponse(OutputStream out, ServiceTunnelResponse msg) throws Exception {
    // build soap message without sax (hi-speed)
    boolean compressed = isUseCompression();
    StringBuilder buf = new StringBuilder();
    buf.append("<SOAP-ENV:Envelope SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
    buf.append("<SOAP-ENV:Body>\n");
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
      buf.append("  <response status=\"ERROR\">\n");
      buf.append("    <exception type=\"" + msg.getException().getClass().getSimpleName() + "\">");
      buf.append(msg.getException().getMessage());
      buf.append("</exception>\n");
      buf.append("  </response>\n");
    }
    buf.append("  <data>");
    long y = System.nanoTime();
    setData(buf, msg, compressed);
    y = System.nanoTime() - y;
    if (LOG.isDebugEnabled()) LOG.debug("message encoding took " + y + " nanoseconds");
    buf.append("</data>\n");
    buf.append("  <info");
    buf.append(" origin=\"" + m_originAddress + "\"");
    buf.append("/>\n");
    buf.append("</SOAP-ENV:Body>");
    buf.append("</SOAP-ENV:Envelope>");
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
    ServiceTunnelOutputStream serialout = null;
    try {
      // build serialized data
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      if (compressed) {
        deflater = new Deflater(Deflater.BEST_SPEED);
        DeflaterOutputStream deflaterStream = new DeflaterOutputStream(bos, deflater);
        serialout = new ServiceTunnelOutputStream(deflaterStream) {
          @Override
          protected Object replaceObject(Object obj) throws IOException {
            IOutboundListener[] listeners = m_cachedOutListeners;
            if (listeners != null) {
              try {
                for (IOutboundListener listener : listeners) {
                  listener.filterOutbound(obj);
                }
              }
              catch (IOException e) {
                throw e;
              }
              catch (Exception e) {
                throw new IOException(e.getMessage());
              }
            }
            obj = super.replaceObject(obj);
            return obj;
          }
        };
        serialout.writeObject(msg);
        serialout.flush();
        deflaterStream.finish();
        serialout.close();
        serialout = null;
      }
      else {
        serialout = new ServiceTunnelOutputStream(bos) {
          @Override
          protected Object replaceObject(Object obj) throws IOException {
            IOutboundListener[] listeners = m_cachedOutListeners;
            if (listeners != null) {
              try {
                for (IOutboundListener listener : listeners) {
                  listener.filterOutbound(obj);
                }
              }
              catch (IOException e) {
                throw e;
              }
              catch (Exception e) {
                throw new IOException(e.getMessage());
              }
            }
            obj = super.replaceObject(obj);
            return obj;
          }
        };
        serialout.writeObject(msg);
        serialout.flush();
        serialout.close();
        serialout = null;
      }
      String base64Data = StringUtility.wrapText(Base64Utility.encode(bos.toByteArray()), 10000);
      buf.append(base64Data);
    }
    finally {
      if (serialout != null) try {
        serialout.close();
      }
      catch (Throwable fatal) {
      }
      if (deflater != null) try {
        deflater.end();
      }
      catch (Throwable fatal) {
      }
    }
  }

  public ServiceTunnelRequest readRequest(InputStream in) throws Exception {
    return (ServiceTunnelRequest) read(in);
  }

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
    if (LOG.isDebugEnabled()) LOG.debug("message decoding took " + y + " nanoseconds");
    return res;
  }

  protected Object getData(String dataPart, boolean compressed) throws IOException, ClassNotFoundException {
    Inflater inflater = null;
    ServiceTunnelInputStream serialin = null;
    try {
      String base64Data = dataPart.replaceAll("[\\n\\r]", "");
      // decode serial data
      if (compressed) {
        inflater = new Inflater();
        InflaterInputStream inflaterStream = new InflaterInputStream(new ByteArrayInputStream(Base64Utility.decode(base64Data)), inflater);
        serialin = new ServiceTunnelInputStream(inflaterStream, m_bundleList) {
          @Override
          protected Object resolveObject(Object obj) throws IOException {
            obj = super.resolveObject(obj);
            IInboundListener[] listeners = m_cachedInListeners;
            if (listeners != null) {
              try {
                for (IInboundListener listener : listeners) {
                  listener.filterInbound(obj);
                }
              }
              catch (IOException e) {
                throw e;
              }
              catch (Exception e) {
                throw new IOException(e.getMessage());
              }
            }
            return obj;
          }
        };
        return serialin.readObject();
      }
      else {
        InputStream in = new ByteArrayInputStream(Base64Utility.decode(base64Data));
        serialin = new ServiceTunnelInputStream(in, m_bundleList) {
          @Override
          protected Object resolveObject(Object obj) throws IOException {
            obj = super.resolveObject(obj);
            IInboundListener[] listeners = m_cachedInListeners;
            if (listeners != null) {
              try {
                for (IInboundListener listener : listeners) {
                  listener.filterInbound(obj);
                }
              }
              catch (IOException e) {
                throw e;
              }
              catch (Exception e) {
                throw new IOException(e.getMessage());
              }
            }
            return obj;
          }
        };
        return serialin.readObject();
      }
    }
    finally {
      if (serialin != null) try {
        serialin.close();
      }
      catch (Throwable fatal) {
      }
      if (inflater != null) try {
        inflater.end();
      }
      catch (Throwable fatal) {
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

  @Override
  public void addInboundListener(IInboundListener listener) {
    m_listeners.add(IInboundListener.class, listener);
    updateCache();
  }

  @Override
  public void removeInboundListener(IInboundListener listener) {
    m_listeners.remove(IInboundListener.class, listener);
    updateCache();
  }

  @Override
  public void addOutboundListener(IOutboundListener listener) {
    m_listeners.add(IOutboundListener.class, listener);
    updateCache();
  }

  @Override
  public void removeOutboundListener(IOutboundListener listener) {
    m_listeners.remove(IOutboundListener.class, listener);
    updateCache();
  }

  private synchronized void updateCache() {
    m_cachedInListeners = m_listeners.getListeners(IInboundListener.class);
    if (m_cachedInListeners != null && m_cachedInListeners.length == 0) {
      m_cachedInListeners = null;
    }
    m_cachedOutListeners = m_listeners.getListeners(IOutboundListener.class);
    if (m_cachedOutListeners != null && m_cachedOutListeners.length == 0) {
      m_cachedOutListeners = null;
    }
  }

}
