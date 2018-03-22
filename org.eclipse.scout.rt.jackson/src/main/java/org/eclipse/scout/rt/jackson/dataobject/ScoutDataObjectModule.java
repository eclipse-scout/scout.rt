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
    context.addSerializers(BEANS.get(DataObjectSerializers.class));
    context.addDeserializers(BEANS.get(DataObjectDeserializers.class));
    
    context.addKeySerializers(BEANS.get(DataObjectMapKeySerializers.class));
    context.addKeyDeserializers(BEANS.get(DataObjectMapKeyDeserializers.class));

    context.addTypeModifier(BEANS.get(DataObjectTypeModifier.class));
    context.insertAnnotationIntrospector(BEANS.get(DataObjectAnnotationIntrospector.class));
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
