package org.eclipse.scout.rt.jackson.dataobject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;

/**
 * Jackson {@link AnnotationIntrospector} implementation adding type resolver for all {@link IDoEntity} data object
 * instances.
 */
@ApplicationScoped
public class DataObjectAnnotationIntrospector extends JacksonAnnotationIntrospector {
  private static final long serialVersionUID = 1L;

  @Override
  public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config, AnnotatedClass ac, JavaType baseType) {
    if (IDoEntity.class.isAssignableFrom(ac.getRawType())) {
      DataObjectTypeResolverBuilder doTypeResolverBuilder = BEANS.get(DataObjectTypeResolverBuilder.class);
      doTypeResolverBuilder.init(JsonTypeInfo.Id.NAME, BEANS.get(DataObjectTypeIdResolver.class));
      doTypeResolverBuilder.inclusion(JsonTypeInfo.As.PROPERTY);
      doTypeResolverBuilder.typeProperty(DataObjectTypeResolverBuilder.JSON_TYPE_PROPERTY);
      return doTypeResolverBuilder;
    }
    return super.findTypeResolver(config, ac, baseType);
  }
}
