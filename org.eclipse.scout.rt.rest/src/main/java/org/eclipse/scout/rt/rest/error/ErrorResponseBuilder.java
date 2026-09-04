/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.error;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.rest.logger.LogLevel;

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
  private boolean m_correlationId = true;
  private LogLevel m_logLevel;
  private Map<String, String> m_headers = new LinkedHashMap<>();

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
      case IStatus.INFO -> withSeverity("info");
      case IStatus.WARNING -> withSeverity("warning");
      case IStatus.ERROR -> withSeverity("error");
    }
    return this;
  }

  public ErrorResponseBuilder withSeverity(String severity) {
    m_severity = severity;
    return this;
  }

  public ErrorResponseBuilder withCorrelationId(boolean correlationId) {
    m_correlationId = correlationId;
    return this;
  }

  public ErrorResponseBuilder withLogLevel(LogLevel logLevel) {
    m_logLevel = logLevel;
    return this;
  }

  public ErrorResponseBuilder addHeader(String key, String value) {
    m_headers.put(key, value);
    return this;
  }

  /**
   * Replaces all previously set headers
   */
  public ErrorResponseBuilder withHeaders(Map<String, String> headers) {
    m_headers = new HashMap<>(ObjectUtility.nvl(headers, Collections.emptyMap()));
    return this;
  }

  public Response build() {
    ResponseBuilder response = Response.status(m_httpStatus)
        .entity(BEANS.get(ErrorResponse.class).withError(buildError()))
        .type(MediaType.APPLICATION_JSON);
    m_headers.forEach((k, v) -> response.header(k, v));
    return response.build();
  }

  protected ErrorDo buildError() {
    ErrorDo error = BEANS.get(ErrorDo.class)
        .withHttpStatus(m_httpStatus);
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
    if (m_correlationId) {
      error.withCorrelationId(CorrelationId.CURRENT.get());
    }
    if (m_logLevel != null) {
      error.withLogLevel(m_logLevel);
    }
    return error;
  }
}
