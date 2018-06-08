package org.eclipse.scout.rt.jackson.dataobject;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

@Bean
public class DataObjectTypeResolverBuilder extends DefaultTypeResolverBuilder {
  private static final long serialVersionUID = 1L;

  public DataObjectTypeResolverBuilder() {
    super(DefaultTyping.NON_FINAL);
  }

  @Override
  public boolean useForType(JavaType t) {
    // do not write type information for "raw" DoEntity instances (only concrete instances, without IDoEntity marker interface)
	  return !DoEntity.class.equals(t.getRawClass());
  }
}
