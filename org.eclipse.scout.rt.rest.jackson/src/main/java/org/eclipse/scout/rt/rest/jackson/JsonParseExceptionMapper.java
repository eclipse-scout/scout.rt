package org.eclipse.scout.rt.rest.jackson;

import javax.annotation.Priority;
import javax.ws.rs.core.Response;

import org.eclipse.scout.rt.rest.exception.AbstractExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;

/**
 * Override Jackson JsonMappingExceptionMapper implementation registered in JacksonFeature
 * 
 * @see https://github.com/FasterXML/jackson-jaxrs-providers/issues/22
 */
@Priority(1)
public class JsonParseExceptionMapper extends AbstractExceptionMapper<JsonParseException> {

  private static final Logger LOG = LoggerFactory.getLogger(JsonParseExceptionMapper.class);

  @Override
  protected Response toResponseImpl(JsonParseException exception) {
    LOG.info("{}: {}", exception.getClass().getSimpleName(), exception.getMessage(), exception);
    return Response.status(Response.Status.BAD_REQUEST).build(); // do not return internal exception message
  }
}
