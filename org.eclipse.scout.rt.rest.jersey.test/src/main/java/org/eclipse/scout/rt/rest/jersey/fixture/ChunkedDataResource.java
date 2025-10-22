/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.fixture;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.dataobject.fixture.FixtureDateId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.rest.IRestResource;
import org.eclipse.scout.rt.rest.chunked.IChunkedDataWriter;
import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("chunked")
public class ChunkedDataResource implements IRestResource {

  private static final Logger LOG = LoggerFactory.getLogger(ChunkedDataResource.class);

  /**
   * Implementation example based on JAX-RS API and Scout API
   *
   * @see org.eclipse.scout.rt.rest.chunked.IChunkedDataWriter
   */
  @GET
  @Path("string-scout")
  @Produces(MediaType.TEXT_PLAIN)
  public GenericType<String> getStringsScout() {
    IChunkedDataWriter<String> writer = IChunkedDataWriter.create(String.class, "\n\n", 100);
    writeAsyncDataScout(writer, Integer::toString);
    return writer.toEntity();
  }

  /**
   * Implementation example based on Jersey API
   *
   * @see org.glassfish.jersey.server.ChunkedOutput
   */
  @GET
  @Path("string-jersey")
  @Produces(MediaType.TEXT_PLAIN)
  public ChunkedOutput<String> getStringsJersey() {
    ChunkedOutput<String> output = new ChunkedOutput<>(String.class, "\n\n");
    writeAsyncDataJersey(output, Integer::toString);
    return output;
  }

  /**
   * Implementation example based on JAX-RS API and Scout API
   *
   * @see org.eclipse.scout.rt.rest.chunked.IChunkedDataWriter
   */
  @GET
  @Path("dataobject-scout")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericType<FixtureDo> getDataObjectsScout() {
    IChunkedDataWriter<FixtureDo> writer = IChunkedDataWriter.create(FixtureDo.class, "\n\n", 100);
    writeAsyncDataScout(writer, this::createFixtureDo);
    return writer.toEntity();
  }

  /**
   * Implementation example based on Jersey API
   *
   * @see org.glassfish.jersey.server.ChunkedOutput
   */
  @GET
  @Path("dataobject-jersey")
  @Produces(MediaType.APPLICATION_JSON)
  public ChunkedOutput<FixtureDo> getDataobjectsJersey() {
    ChunkedOutput<FixtureDo> output = new ChunkedOutput<>(FixtureDo.class, "\n\n");
    writeAsyncDataJersey(output, this::createFixtureDo);
    return output;
  }

  /**
   * Implementation example based on JAX-RS API and Scout API.<br>
   * Returning {@link Response} including header 'X-Mock' with value {@code headerValue}.
   */
  @GET
  @Path("dataobject-scout/header")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDataObjectsScout(@QueryParam("headerValue") String headerValue) {
    IChunkedDataWriter<FixtureDo> writer = IChunkedDataWriter.create(FixtureDo.class, "\n\n", 100);
    writeAsyncDataScout(writer, this::createFixtureDo);
    return Response.ok(writer.toEntity())
        .header("X-Mock", headerValue)
        .build();
  }

  /**
   * Implementation example based on JAX-RS API and Scout API and using default jersey-defined delimiter.
   */
  @GET
  @Path("dataobject-scout/default-delimiter")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDataObjectsScoutDefaultDelimiter() {
    IChunkedDataWriter<FixtureDo> writer = IChunkedDataWriter.create(FixtureDo.class);
    writeAsyncDataScout(writer, this::createFixtureDo);
    return Response.ok(writer.toEntity()).build();
  }

  /**
   * Implementation example based on JAX-RS API and Scout API and using "" as delimiter (resulting in default jersey-defined delimiter).
   */
  @GET
  @Path("dataobject-scout/default-delimiter-empty-string")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDataObjectsScoutDefaultDelimiterEmptyString() {
    IChunkedDataWriter<FixtureDo> writer = IChunkedDataWriter.create(FixtureDo.class, "", 100);
    writeAsyncDataScout(writer, this::createFixtureDo);
    return Response.ok(writer.toEntity()).build();
  }

  /**
   * Implementation example based on JAX-RS API and Scout API and using null as delimiter (resulting in default jersey-defined delimiter).
   */
  @GET
  @Path("dataobject-scout/default-delimiter-null")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDataObjectsScoutDefaultDelimiterNull() {
    IChunkedDataWriter<FixtureDo> writer = IChunkedDataWriter.create(FixtureDo.class, null, 100);
    writeAsyncDataScout(writer, this::createFixtureDo);
    return Response.ok(writer.toEntity()).build();
  }

  public FixtureDo createFixtureDo(int id) {
    return BEANS.get(FixtureDo.class)
        .withId(FixtureStringId.of(Integer.toString(id)))
        .withUuid(FixtureUuId.of(UUID.fromString("00000000-0000-0000-0000-000000000000")))
        .withDates(FixtureDateId.of(new Date(id)), FixtureDateId.of(new Date(id + 1)));
  }

  protected <W extends IChunkedDataWriter<T>, T> void writeAsyncDataScout(W writer, IntFunction<T> writeSupplier) {
    Jobs.schedule(() -> {
      try (writer) {
        for (int i = 1; i <= 3; i++) {
          writer.write(writeSupplier.apply(i));
          LOG.debug("Wrote chunk '{}'", i);
          SleepUtil.sleepSafe(10, TimeUnit.MILLISECONDS);
          assertFalse(writer.isClosed());
        }
      }
      assertTrue(writer.isClosed());
    }, Jobs.newInput());
  }

  protected <W extends ChunkedOutput<T>, T> void writeAsyncDataJersey(W writer, IntFunction<T> writeSupplier) {
    Jobs.schedule(() -> {
      try (writer) {
        for (int i = 1; i <= 3; i++) {
          writer.write(writeSupplier.apply(i));
          LOG.debug("Wrote chunk '{}'", i);
          SleepUtil.sleepSafe(10, TimeUnit.MILLISECONDS);
          assertFalse(writer.isClosed());
        }
      }
      assertTrue(writer.isClosed());
    }, Jobs.newInput());
  }
}
