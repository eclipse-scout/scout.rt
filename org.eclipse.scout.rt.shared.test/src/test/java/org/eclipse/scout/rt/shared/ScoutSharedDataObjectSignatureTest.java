/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared;

import org.eclipse.scout.rt.dataobject.testing.signature.AbstractDataObjectSignatureTest;

public class ScoutSharedDataObjectSignatureTest extends AbstractDataObjectSignatureTest {

  @Override
  protected String getFilenamePrefix() {
    return "shout-shared";
  }

  @Override
  protected String getPackageNamePrefix() {
    return "org.eclipse.scout.rt.shared";
  }
}
