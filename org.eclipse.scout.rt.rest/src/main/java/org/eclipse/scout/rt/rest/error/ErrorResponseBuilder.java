/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.error;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.CorrelationId;

/**
 * Builder for {@link ErrorDo} and {@link ErrorResponse} objects.
 */
@Bean
public class ErrorResponseBuilder {

  private int m_httpStatus;
  private String m_errorCode;
  private String m_title;
  private String m_message;

  public ErrorResponseBuilder withHttpStatus(int httpStatus) {
    m_httpStatus = httpStatus;
    return this;
  }

  public ErrorResponseBuilder withHttpStatus(Status httpStatus) {
    m_httpStatus = httpStatus.getStatusCode();
    return this;
  }

  public ErrorResponseBuilder withTitle(String title) {
    m_title = title;
    return this;
  }

  public ErrorResponseBuilder withMessage(String message) {
    m_message = message;
    return this;
  }

  public ErrorResponseBuilder withErrorCode(int errorCode) {
    m_errorCode = String.valueOf(errorCode);
    return this;
  }

  public ErrorResponseBuilder withErrorCode(String errorCode) {
    m_errorCode = errorCode;
    return this;
  }

  public Response build() {
    return Response.status(m_httpStatus)
        .entity(BEANS.get(ErrorResponse.class).withError(buildError()))
        .type(MediaType.APPLICATION_JSON)
        .build();
  }

  protected ErrorDo buildError() {
    ErrorDo error = BEANS.get(ErrorDo.class)
        .withHttpStatus(m_httpStatus)
        .withCorrelationId(CorrelationId.CURRENT.get());
    if (m_errorCode != null) {
      error.withErrorCode(m_errorCode);
    }
    if (m_title != null) {
      error.withTitle(m_title);
    }
    if (m_message != null) {
      error.withMessage(m_message);
    }
    return error;
  }
}
