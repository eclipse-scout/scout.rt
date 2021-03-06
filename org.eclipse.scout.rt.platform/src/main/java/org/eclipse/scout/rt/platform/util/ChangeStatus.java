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
