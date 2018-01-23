package org.eclipse.scout.rt.jackson.dataobject;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

@Bean
public class DataObjectTypeResolverBuilder extends DefaultTypeResolverBuilder {
  private static final long serialVersionUID = 1L;

  /**
   * Used as constant value for to specify type of JSON object during serialization.
   */
  protected static final String JSON_TYPE_PROPERTY = "_type";

  public DataObjectTypeResolverBuilder() {
    super(DefaultTyping.NON_FINAL);
  }

  @Override
  public boolean useForType(JavaType t) {
    // do not write type information for "raw" DoEntity and DoTypedEntity instances
    return !DoEntity.class.equals(t.getRawClass()) && !DoTypedEntity.class.equals(t.getRawClass());
  }
}
