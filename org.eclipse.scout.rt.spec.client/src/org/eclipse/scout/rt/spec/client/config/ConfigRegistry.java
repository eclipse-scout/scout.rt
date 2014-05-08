/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client.config;

/**
 * Registry for config classes
 * <p>
 * All getters provide default-instances if no custom instances were set before.
 */
public final class ConfigRegistry {

  private static IDocConfig s_docConfigInstance;
  private static SpecFileConfig s_specFileConfigInstance;

  private ConfigRegistry() {
  }

  /**
   * @return the {@link IDocConfig} instance
   */
  public static IDocConfig getDocConfigInstance() {
    if (s_docConfigInstance == null) {
      s_docConfigInstance = new DefaultDocConfig();
    }
    return s_docConfigInstance;
  }

  public static void setDocConfig(IDocConfig specFileConfig) {
    s_docConfigInstance = specFileConfig;
  }

  /**
   * @return the {@link SpecFileConfig} instance
   */
  public static SpecFileConfig getSpecFileConfigInstance() {
    if (s_specFileConfigInstance == null) {
      s_specFileConfigInstance = new SpecFileConfig();
    }
    return s_specFileConfigInstance;
  }

  // TODO ASA The only property, that really can be customized in SpecFileConfig is the output dir. All source dirs need to be the in the same
  // structure in all bundles (rt and project) because of hierarchic copying.
  // --> Create a configuration possibility for the output dir and remove this setter.
  public static void setSpecFileConfig(SpecFileConfig specFileConfig) {
    s_specFileConfigInstance = specFileConfig;
  }

}
