/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.client;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.rest.chunked.IChunkedDataWriter;
import org.eclipse.scout.rt.rest.client.chunked.IChunkedDataReader;
import org.eclipse.scout.rt.rest.jersey.JerseyTestApplication;
import org.eclipse.scout.rt.rest.jersey.JerseyTestRestClientHelper;
import org.eclipse.scout.rt.rest.jersey.fixture.ChunkedDataResource;
import org.eclipse.scout.rt.rest.jersey.fixture.FixtureDo;
import org.glassfish.jersey.client.ChunkParser;
import org.glassfish.jersey.client.ChunkedInput;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test cases for REST using chunked data.
 */
public class ChunkedDataTest {

  private static final Logger LOG = LoggerFactory.getLogger(ChunkedDataTest.class);

  private WebTarget m_target;

  @BeforeClass
  public static void beforeClass() {
    BEANS.get(JerseyTestApplication.class).ensureStarted();
  }

  @Before
  public void before() {
    m_target = BEANS.get(JerseyTestRestClientHelper.class).target("api/chunked");
  }

  @Test
  public void testGetStringsScout() {
    Response response = m_target
        .path("string-scout")
        .request()
        .accept(MediaType.TEXT_PLAIN)
        .get();

    IChunkedDataReader<String> reader = IChunkedDataReader.create(response, String.class, "\n\n");
    assertFalse(reader.isClosed());

    String chunk;
    List<String> chunks = new ArrayList<>();
    while ((chunk = reader.read()) != null) {
      chunks.add(chunk);
      LOG.debug("Received chunk {}", chunk);
    }
    assertEquals(List.of("1", "2", "3"), chunks);
    assertTrue(reader.isClosed());
  }

  @Test
  public void testGetStringsJersey() {
    Response response = m_target
        .path("string-jersey")
        .request()
        .accept(MediaType.TEXT_PLAIN)
        .get();

    ChunkedInput<String> chunkedInput = response.readEntity(new GenericType<>() {
    });
    ChunkParser p = ChunkedInput.createParser("\n\n");
    chunkedInput.setParser(p);
    assertFalse(chunkedInput.isClosed());

    String chunk;
    List<String> chunks = new ArrayList<>();
    while ((chunk = chunkedInput.read()) != null) {
      chunks.add(chunk);
      LOG.debug("Received chunk {}", chunk);
    }
    assertEquals(List.of("1", "2", "3"), chunks);
    assertTrue(chunkedInput.isClosed());
  }

  @Test
  public void testGetDataObjectsScout() {
    Response response = m_target
        .path("dataobject-scout")
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get();

    IChunkedDataReader<FixtureDo> reader = IChunkedDataReader.create(response, FixtureDo.class, "\n\n");
    assertFalse(reader.isClosed());

    FixtureDo chunk;
    List<FixtureDo> chunks = new ArrayList<>();
    while ((chunk = reader.read()) != null) {
      chunks.add(chunk);
      LOG.debug("Received chunk {}", chunk);
    }
    assertChunks(chunks);
    assertTrue(reader.isClosed());
  }

  @Test
  public void testGetDataobjectsJersey() {
    Response response = m_target
        .path("dataobject-jersey")
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get();

    ChunkedInput<FixtureDo> chunkedInput = response.readEntity(new GenericType<>() {
    });
    ChunkParser p = ChunkedInput.createParser("\n\n");
    chunkedInput.setParser(p);
    assertFalse(chunkedInput.isClosed());

    FixtureDo chunk;
    List<FixtureDo> chunks = new ArrayList<>();
    while ((chunk = chunkedInput.read()) != null) {
      chunks.add(chunk);
      LOG.debug("Received chunk {}", chunk);
    }
    assertChunks(chunks);
    assertTrue(chunkedInput.isClosed());
  }

  @Test
  public void testGetDataObjectsScout_withHeader() {
    Response response = m_target
        .path("dataobject-scout/header")
        .queryParam("headerValue", "foo")
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get();

    assertEquals("foo", response.getHeaderString("X-Mock"));

    IChunkedDataReader<FixtureDo> reader = IChunkedDataReader.create(response, FixtureDo.class, "\n\n");
    assertFalse(reader.isClosed());

    FixtureDo chunk;
    List<FixtureDo> chunks = new ArrayList<>();
    while ((chunk = reader.read()) != null) {
      chunks.add(chunk);
      LOG.debug("Received chunk {}", chunk);
    }
    assertChunks(chunks);
    assertTrue(reader.isClosed());
  }

  @Test
  public void testGetDataObjectsScout_earlyAbort() throws IOException {
    Response response = m_target
        .path("dataobject-scout")
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get();

    IChunkedDataReader<FixtureDo> reader = IChunkedDataReader.create(response, FixtureDo.class, "\n\n");
    assertFalse(reader.isClosed());

    FixtureDo chunk = reader.read();
    reader.close();
    assertEquals(BEANS.get(ChunkedDataResource.class).createFixtureDo(1), chunk);
    assertTrue(reader.isClosed());
    assertThrows(IllegalStateException.class, () -> reader.read());
  }

  @Test
  public void testGetDataObjectsScout_defaultDelimiter() {
    testGetDataObjectsScout_defaultDelimiter("default-delimiter", response -> IChunkedDataReader.create(response, FixtureDo.class));
  }

  @Test
  public void testGetDataObjectsScout_defaultDelimiterEmptyString() {
    testGetDataObjectsScout_defaultDelimiter( "default-delimiter-empty-string", response -> IChunkedDataReader.create(response, FixtureDo.class, ""));
  }

  @Test
  public void testGetDataObjectsScout_defaultDelimiterNull() {
    testGetDataObjectsScout_defaultDelimiter( "default-delimiter-null", response -> IChunkedDataReader.create(response, FixtureDo.class, null));
  }

  protected void testGetDataObjectsScout_defaultDelimiter( String contextPath, Function<Response, IChunkedDataReader<FixtureDo>> chunkedReaderCreator) {
    Response response = m_target
        .path("dataobject-scout/" + contextPath)
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get();

    IChunkedDataReader<FixtureDo> reader = chunkedReaderCreator.apply(response);
    assertFalse(reader.isClosed());

    FixtureDo chunk;
    List<FixtureDo> chunks = new ArrayList<>();
    while ((chunk = reader.read()) != null) {
      chunks.add(chunk);
      LOG.debug("Received chunk {}", chunk);
    }
    assertChunks(chunks);
    assertTrue(reader.isClosed());
  }

  protected void assertChunks(List<FixtureDo> chunks) {
    ChunkedDataResource resource = BEANS.get(ChunkedDataResource.class);
    assertEquals(List.of(resource.createFixtureDo(1), resource.createFixtureDo(2), resource.createFixtureDo(3)), chunks);
  }

  protected static class FixtureDoChunkedInputMock extends ChunkedInput<FixtureDo> {
    protected FixtureDoChunkedInputMock() {
      super(FixtureDo.class, null, null, null, null, null, null);
    }
  }

  @Test
  public void testInitReaderAlreadyInitialized() {
    Response response = Mockito.mock(Response.class);
    //noinspection unchecked
    when(response.readEntity(any(GenericType.class))).thenReturn(new FixtureDoChunkedInputMock());
    IChunkedDataReader<FixtureDo> reader = IChunkedDataReader.create(response, FixtureDo.class, "");
    assertThrows(AssertionException.class, () -> reader.init(response, FixtureDo.class, ""));
  }

  @Test
  public void testInitWriterAlreadyInitialized() {
    @SuppressWarnings("resource")
    IChunkedDataWriter<FixtureDo> writer = IChunkedDataWriter.create(FixtureDo.class, "", -1);
    assertThrows(AssertionException.class, () -> writer.init(FixtureDo.class, "", -1));
  }
}
