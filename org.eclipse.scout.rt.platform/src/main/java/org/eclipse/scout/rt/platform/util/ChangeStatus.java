/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

/**
 * @since 8.0
 */
public final class ChangeStatus {

  public static final int NOT_CHANGED = 0;
  public static final int INSERTED = 1;
  public static final int UPDATED = 2;
  public static final int DELETED = 3;

  private ChangeStatus() {
  }
}
