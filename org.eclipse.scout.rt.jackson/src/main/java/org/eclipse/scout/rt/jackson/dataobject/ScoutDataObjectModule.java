/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Jackson {@link Module} that adds {@link ObjectMapper} support for Scout data object types like ({@code DoEntity},
 * {@code DoValue} and {@code DoList}.
 */
@Bean
public class ScoutDataObjectModule extends Module {

  private static final String NAME = "ScoutDataObjectModule";

  /**
   * Default name of type attribute used for serialization.
   *
   * @see #getTypeAttributeName()
   */
  protected static final String DEFAULT_TYPE_ATTRIBUTE_NAME = "_type";

  @Override
  public String getModuleName() {
    return NAME;
  }

  @Override
  public Version version() {
    return PackageVersion.VERSION;
  }

  @Override
  public void setupModule(SetupContext context) {
    ScoutDataObjectModuleContext moduleContext = new ScoutDataObjectModuleContext();
    prepareScoutDataModuleContext(moduleContext);

    context.addSerializers(BEANS.get(DataObjectSerializers.class).withModuleContext(moduleContext));
    context.addDeserializers(BEANS.get(DataObjectDeserializers.class).withModuleContext(moduleContext));

    context.addKeySerializers(BEANS.get(DataObjectMapKeySerializers.class).withModuleContext(moduleContext));
    context.addKeyDeserializers(BEANS.get(DataObjectMapKeyDeserializers.class).withModuleContext(moduleContext));

    context.addTypeModifier(BEANS.get(DataObjectTypeModifier.class).withModuleContext(moduleContext));
    context.insertAnnotationIntrospector(BEANS.get(DataObjectAnnotationIntrospector.class).withModuleContext(moduleContext));
  }

  /**
   * Override this method to add custom properties to {@code moduleContext}.
   */
  protected void prepareScoutDataModuleContext(ScoutDataObjectModuleContext moduleContext) {
    moduleContext.setTypeAttributeName(DEFAULT_TYPE_ATTRIBUTE_NAME);
  }

  @Override
  public int hashCode() {
    return NAME.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return this == o;
  }
}
