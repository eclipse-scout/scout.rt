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
package org.eclipse.scout.rt.rest.client.multipart;

import static org.eclipse.scout.rt.platform.util.Assertions.assertFalse;

import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.MessageBodyWriter;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.uuid.IUuidProvider;
import org.eclipse.scout.rt.rest.client.IRestClientHelper;

/**
 * Multipart message for REST service invocations using a multipart request, e.g. by using a {@link IRestClientHelper}.
 * <p>
 * A multipart message must contain at least one part before being sent. Use {@link #toEntity()} to create an entity for
 * JAX-RS.
 * <p>
 * Only supports {@link StandardCharsets#UTF_8} encoding.
 * <p>
 * Example:
 *
 * <pre>
 * MultipartMessage multiPartMessage = BEANS.get(MultipartMessage.class)
 *     .addPart(MultipartPart.ofFile("lorem", "lorem.zip", new ByteArrayInputStream(loremZipBytes)))
 *     .addPart(MultipartPart.ofFile("ipsum", "ipsum.json", new ByteArrayInputStream(ipsumJsonBytes)))
 *     .addPart(MultipartPart.ofField("dolor", "one"))
 *     .addPart(MultipartPart.ofField("sid", "two"));
 *
 * return helper()
 *     .path("test")
 *     .request()
 *     .accept(MediaType.APPLICATION_JSON)
 *     .post(multiPartMessage.toEntity(), TestDo.class);
 * </pre>
 */
@Bean
public class MultipartMessage {

  public static final String BOUNDARY_PARAMETER = "boundary";

  protected List<MultipartPart> m_parts = new ArrayList<>();
  protected String m_boundary; // cached boundary in case #toEntity is called multiple times

  public MultipartMessage addPart(MultipartPart part) {
    m_parts.add(part);
    return this;
  }

  public List<MultipartPart> getParts() {
    return Collections.unmodifiableList(m_parts);
  }

  /**
   * To be used for multipart message instead of {@link Entity#entity(Object, String)} with
   * {@link MediaType#MULTIPART_FORM_DATA} because boundary must be added directly to the media type.
   * <p>
   * {@link MessageBodyWriter} cannot add HTTP headers when using a different HTTP client implementation than
   * {@link URLConnection} because headers are written to output stream before message body writer is called.
   */
  public Entity<MultipartMessage> toEntity() {
    assertFalse(m_parts.isEmpty(), "multipart message must contain at least one part");
    return Entity.entity(this, createMultipartMediaType());
  }

  protected MediaType createMultipartMediaType() {
    if (m_boundary == null) {
      // create a new unique boundary if it wasn't already cached before for this multipart message
      m_boundary = BEANS.get(IUuidProvider.class).createUuid().toString().replace("-", "");
    }
    return new MediaType(MediaType.MULTIPART_FORM_DATA_TYPE.getType(), MediaType.MULTIPART_FORM_DATA_TYPE.getSubtype(), CollectionUtility.hashMap(ImmutablePair.of(BOUNDARY_PARAMETER, m_boundary)));
  }

  @Override
  public String toString() {
    return MultipartMessage.class.getSimpleName() + "[m_parts=" + m_parts + "]";
  }
}
