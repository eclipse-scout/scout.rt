/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

/**
 * <b>NOTE</b> Serializing java exception is discouraged, since most concrete exception classes are not
 * JSON-serializable.
 * <p>
 * The Jackson library out of the box supports to serialize and deserialize an {@link Exception} or {@link Throwable}
 * using its message, cause and stacktrace elements as fields. This test data object ensures this basic functionality
 * when serializing {@link Exception} or {@link Throwable} within data objects.
 */
@TypeName("TestThrowable")
public class TestThrowableDo extends DoEntity {

  public DoValue<Throwable> throwable() {
    return doValue("throwable");
  }

  public DoValue<Exception> exception() {
    return doValue("exception");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestThrowableDo withThrowable(Throwable throwable) {
    throwable().set(throwable);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Throwable getThrowable() {
    return throwable().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestThrowableDo withException(Exception exception) {
    exception().set(exception);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Exception getException() {
    return exception().get();
  }
}
