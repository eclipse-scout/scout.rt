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

import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.eclipse.scout.rt.platform.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * "No frills" implementation for client-server communication with optional compression.
 */
@Order(5010)
public class BinaryServiceTunnelContentHandler extends AbstractServiceTunnelContentHandler {

  private static final Logger LOG = LoggerFactory.getLogger(BinaryServiceTunnelContentHandler.class);
  private static final String CONTENT_TYPE = "application/octet-stream";

  private Boolean m_receivedCompressed;

  @Override
  public String getContentType() {
    return CONTENT_TYPE;
  }

  @Override
  public void writeRequest(OutputStream out, ServiceTunnelRequest msg) throws Exception {
    writeData(out, msg);
  }

  @Override
  public ServiceTunnelRequest readRequest(InputStream in) throws Exception {
    return readData(in, ServiceTunnelRequest.class);
  }

  @Override
  public void writeResponse(OutputStream out, ServiceTunnelResponse msg) throws Exception {
    writeData(out, msg);
  }

  @Override
  public ServiceTunnelResponse readResponse(InputStream in) throws Exception {
    return readData(in, ServiceTunnelResponse.class);
  }

  private void writeData(OutputStream out, Object msg) throws IOException {
    Deflater deflater = null;
    boolean compressed = isUseCompression();
    try {
      // signal compression
      out.write(compressed ? 1 : 0);
      // build serialized data
      if (compressed) {
        deflater = new Deflater(Deflater.BEST_SPEED);
        out = new DeflaterOutputStream(out, deflater);
      }
      getObjectSerializer().serialize(out, msg);
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
        catch (Throwable fatal) {
        }
      }
    }
  }

  private <T> T readData(InputStream in, Class<T> clazz) throws IOException, ClassNotFoundException {
    Inflater inflater = null;
    try {
      // read compressed flag
      boolean compressed = in.read() == 1;
      m_receivedCompressed = compressed;
      // decode serial data
      if (compressed) {
        inflater = new Inflater();
        in = new InflaterInputStream(in, inflater);
      }
      return getObjectSerializer().deserialize(in, clazz);
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
    if (isSendCompressed() != null) {
      return isSendCompressed();
    }
    if (m_receivedCompressed != null) {
      return m_receivedCompressed;
    }
    return true;
  }

}
