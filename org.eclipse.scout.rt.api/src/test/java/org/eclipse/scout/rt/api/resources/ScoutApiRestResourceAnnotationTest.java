/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.resources;

import org.eclipse.scout.rt.rest.resource.AbstractRestResourceAnnotationTest;

public class ScoutApiRestResourceAnnotationTest extends AbstractRestResourceAnnotationTest {

  @Override
  protected String getPackageNamePrefix() {
    return "org.eclipse.scout.rt.api";
  }
}
