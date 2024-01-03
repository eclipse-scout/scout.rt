/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;
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
   * @see ScoutDataObjectModuleContext#getTypeAttributeName()
   */
  protected static final String DEFAULT_TYPE_ATTRIBUTE_NAME = "_type";

  /**
   * Default name of type version attribute used for serialization.
   *
   * @see ScoutDataObjectModuleContext#getTypeAttributeName()
   */
  protected static final String DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME = "_typeVersion";

  /**
   * Default name of contributions attribute used for serialization.
   *
   * @see ScoutDataObjectModuleContext#getContributionsAttributeName()
   */
  protected static final String DEFAULT_CONTRIBUTIONS_ATTRIBUTE_NAME = "_contributions";

  private ScoutDataObjectModuleContext m_moduleContext;

  @PostConstruct
  protected void init() {
    m_moduleContext = BEANS.get(ScoutDataObjectModuleContext.class)
        .withTypeAttributeName(DEFAULT_TYPE_ATTRIBUTE_NAME)
        .withTypeVersionAttributeName(DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME)
        .withContributionsAttributeName(DEFAULT_CONTRIBUTIONS_ATTRIBUTE_NAME);
  }

  public ScoutDataObjectModuleContext getModuleContext() {
    return m_moduleContext;
  }

  /**
   * Setup {@link ScoutDataObjectModule} to use given {@code typeAttributeName} as type attribute name.
   */
  public ScoutDataObjectModule withTypeAttributeName(String typeAttributeName) {
    m_moduleContext.withTypeAttributeName(typeAttributeName);
    return this;
  }

  /**
   * Setup {@link ScoutDataObjectModule} to use given {@code typeVersionAttributeName} as type version attribute name.
   */
  public ScoutDataObjectModule withTypeVersionAttributeName(String typeVersionAttributeName) {
    m_moduleContext.withTypeVersionAttributeName(typeVersionAttributeName);
    return this;
  }

  /**
   * Setup {@link ScoutDataObjectModule} to ignore type attributes when deserializing a JSON document structure. Forces
   * to create raw {@link DoEntity} instances for each deserialized JSON object instead.
   */
  public ScoutDataObjectModule withIgnoreTypeAttribute(boolean ignoreTypeAttribute) {
    m_moduleContext.withIgnoreTypeAttribute(ignoreTypeAttribute);
    return this;
  }

  /**
   * Setup {@link ScoutDataObjectModule} to suppress writing type attributes (e.g. '_type' attribute in JSON document)
   * when serializing a data object with {@link TypeName} annotation into a JSON document.
   * <p>
   * <b>NOTE:</b> A JSON document written without type information may not be deserialized correctly if any polymorphic
   * types are used within the data object structure, e.g. a {@link DoList} typed with {@link IDoEntity} containing
   * different data object subclasses.
   */
  public ScoutDataObjectModule withSuppressTypeAttribute(boolean suppressTypeAttribute) {
    m_moduleContext.withSuppressTypeAttribute(suppressTypeAttribute);
    return this;
  }

  /**
   * Setup {@link ScoutDataObjectModule} to use given {@code contributionsAttributeName} as contributions attribute
   * name.
   */
  public ScoutDataObjectModule withContributionsAttributeName(String contributionsAttributeName) {
    m_moduleContext.withContributionsAttributeName(contributionsAttributeName);
    return this;
  }

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
    context.addSerializers(BEANS.get(DataObjectSerializers.class).withModuleContext(m_moduleContext));
    context.addDeserializers(BEANS.get(DataObjectDeserializers.class).withModuleContext(m_moduleContext));

    context.addKeySerializers(BEANS.get(DataObjectMapKeySerializers.class).withModuleContext(m_moduleContext));
    context.addKeyDeserializers(BEANS.get(DataObjectMapKeyDeserializers.class).withModuleContext(m_moduleContext));

    context.addTypeModifier(BEANS.get(DataObjectTypeModifier.class).withModuleContext(m_moduleContext));
    context.insertAnnotationIntrospector(BEANS.get(DataObjectAnnotationIntrospector.class).withModuleContext(m_moduleContext));
  }
}
