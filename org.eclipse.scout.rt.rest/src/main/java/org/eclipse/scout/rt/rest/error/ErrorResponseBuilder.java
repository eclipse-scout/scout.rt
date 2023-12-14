/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.error;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * Builder for {@link ErrorDo} and {@link ErrorResponse} objects.
 */
@Bean
public class ErrorResponseBuilder {

  private int m_httpStatus;
  private String m_errorCode;
  private String m_title;
  private String m_message;
  private String m_severity;

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

  public ErrorResponseBuilder withSeverity(int severity) {
    switch (severity) {
      case IStatus.INFO:
        withSeverity("info");
        break;
      case IStatus.WARNING:
        withSeverity("warning");
        break;
      case IStatus.ERROR:
        withSeverity("error");
    }
    return this;
  }

  public ErrorResponseBuilder withSeverity(String severity) {
    m_severity = severity;
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
    if (m_severity != null) {
      error.withSeverity(m_severity);
    }
    return error;
  }
}
