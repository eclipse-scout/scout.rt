/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.OutputStream;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.serialization.IObjectSerializer;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * "No frills" implementation for client-server communication
 */
@ApplicationScoped
public class BinaryServiceTunnelContentHandler {

  private static final Logger LOG = LoggerFactory.getLogger(BinaryServiceTunnelContentHandler.class);

  protected IObjectSerializer m_objectSerializer;

  @PostConstruct
  protected void initialize() {
    m_objectSerializer = createObjectSerializer();
  }

  /**
   * @return Creates an {@link IObjectSerializer} instance used for serializing and deserializing data.
   * @since 3.8.2
   */
  protected IObjectSerializer createObjectSerializer() {
    return SerializationUtility.createObjectSerializer(new ServiceTunnelObjectReplacer());
  }

  protected IObjectSerializer getObjectSerializer() {
    return m_objectSerializer;
  }

  public void writeRequest(OutputStream out, ServiceTunnelRequest msg) throws IOException {
    writeData(out, msg);
  }

  public ServiceTunnelRequest readRequest(InputStream in) throws IOException, ClassNotFoundException {
    return readData(in, ServiceTunnelRequest.class);
  }

  public void writeResponse(OutputStream out, ServiceTunnelResponse msg) throws IOException {
    writeData(out, msg);
  }

  public ServiceTunnelResponse readResponse(InputStream in) throws IOException, ClassNotFoundException {
    return readData(in, ServiceTunnelResponse.class);
  }

  private void writeData(OutputStream out, Object msg) throws IOException {
    try {
      // build serialized data
      getObjectSerializer().serialize(out, msg);
    }
    catch (NotSerializableException e) {
      LOG.error("Error serializing data '{}'", msg);
      throw e;
    }
  }

  private <T> T readData(InputStream in, Class<T> clazz) throws IOException, ClassNotFoundException {
    // decode serial data
    return getObjectSerializer().deserialize(in, clazz);
  }
}
