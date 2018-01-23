package org.eclipse.scout.rt.rest.jackson;

import javax.annotation.Priority;
import javax.ws.rs.core.Response;

import org.eclipse.scout.rt.rest.exception.AbstractExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;

@Priority(1) // override Jackson JsonMappingExceptionMapper implementation registered in JacksonFeature
public class JsonParseExceptionMapper extends AbstractExceptionMapper<JsonParseException> {

  private static final Logger LOG = LoggerFactory.getLogger(JsonParseExceptionMapper.class);

  @Override
  protected Response toResponseImpl(JsonParseException exception) {
    LOG.info("{}: {}", exception.getClass().getSimpleName(), exception.getMessage(), exception);
    return Response.status(Response.Status.BAD_REQUEST).build(); // do not return internal exception message
  }
}
