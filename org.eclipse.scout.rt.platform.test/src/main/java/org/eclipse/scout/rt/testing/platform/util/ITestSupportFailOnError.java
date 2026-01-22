/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.util;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;

public interface ITestSupportFailOnError {

  default void failOnError() {
    List<String> err = getErrorMessages();
    if (err.isEmpty()) {
      return;
    }
    StringBuilder sb = new StringBuilder();
    Optional.ofNullable(getErrorTitle()).ifPresent(title -> sb.append(title).append("\n"));
    sb.append(err.get(0));
    for (int i = 1; i < err.size(); i++) {
      sb.append("\n").append(err.get(i));
    }
    String message = sb.toString();
    Assert.fail(message);
  }

  default String getErrorTitle() {
    return null;
  }

  List<String> getErrorMessages();
}
