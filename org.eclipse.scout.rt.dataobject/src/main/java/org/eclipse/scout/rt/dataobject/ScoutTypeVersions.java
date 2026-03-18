/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

public class ScoutTypeVersions {

  //  ___  _  _   ___
  // |__ \| || | |__ \
  //    ) | || |_   ) |
  //   / /|__   _| / /
  //  / /_   | |_ / /_
  // |____|  |_(_)____|

  /**
   * Baseline version for all new data objects created in 24.2.
   * <p>
   * As soon as the initial build of this major release was published, a new type version must always be created for
   * data objects requiring a migration handler. A new type version is created by incrementing the counter by 1 and
   * providing a suffix.
   */
  public static final class Scout_24_2_001 extends AbstractTypeVersion {
  }

  //  ___  _____ __
  // |__ \| ____/_ |
  //    ) | |__  | |
  //   / /|___ \ | |
  //  / /_ ___) || |
  // |____|____(_)_|

  /**
   * Baseline version for all new data objects created in 25.1.
   * <p>
   * As soon as the initial build of this major release was published, a new type version must always be created for
   * data objects requiring a migration handler. A new type version is created by incrementing the counter by 1 and
   * providing a suffix.
   */
  public static final class Scout_25_1_001 extends AbstractTypeVersion {
  }

  //  ___  _____  ___
  // |__ \| ____||__ \
  //    ) | |__     ) |
  //   / /|___ \   / /
  //  / /_ ___) | / /_
  // |____|____(_)____|

  /**
   * Baseline version for all new data objects created in 25.2.
   * <p>
   * As soon as the initial build of this major release was published, a new type version must always be created for
   * data objects requiring a migration handler. A new type version is created by incrementing the counter by 1 and
   * providing a suffix.
   */
  public static final class Scout_25_2_001 extends AbstractTypeVersion {
  }

  public static final class Scout_25_2_002__uiPreferences extends AbstractTypeVersion {
  }

  //   ___   __  __
  //  |__ \ / / /_ |
  //     ) / /_  | |
  //    / / '_ \ | |
  //   / /| (_) || |
  //  |____\___(_)_|

  /**
   * Baseline version for all new data objects created in 26.1.
   * <p>
   * As soon as the initial build of this major release was published, a new type version must always be created for
   * data objects requiring a migration handler. A new type version is created by incrementing the counter by 1 and
   * providing a suffix.
   */
  public static final class Scout_26_1_001 extends AbstractTypeVersion {
  }
}
