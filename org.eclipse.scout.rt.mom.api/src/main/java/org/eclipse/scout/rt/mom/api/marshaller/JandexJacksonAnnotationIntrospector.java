/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.mom.api.marshaller;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.IBean;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;

/**
 * Default {@link JacksonAnnotationIntrospector} implementation for BSI applications.
 */
//TODO [7.0] pbz: Move class to own scout json/jackson module
@Bean
public class JandexJacksonAnnotationIntrospector extends JacksonAnnotationIntrospector {
  private static final long serialVersionUID = 1L;

  protected BeanManagerUtility m_beanManagerUtility;

  @PostConstruct
  protected void init() {
    m_beanManagerUtility = BEANS.get(BeanManagerUtility.class);
  }

  /**
   * Method {@link #_findTypeResolver(MapperConfig, Annotated, JavaType)} is used by jackson to build the
   * {@link TypeResolverBuilder} according to a set of annotations on the object to serialize/deserialize.
   * <p>
   * The implementation in this method checks the provided {@code baseType} and computes the class to use for
   * deserialization considering that the {@code baseType} could be a {@code Bean} and therefore be replaced.
   *
   * @see https://github.com/FasterXML/jackson-databind/issues/955
   */
  @Override
  public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config, AnnotatedClass ac, JavaType baseType) {
    TypeResolverBuilder<?> builder = super.findTypeResolver(config, ac, baseType);
    if (builder != null && builder.getDefaultImpl() == null) {
      // if a type resolver builder is available and does not yet have a default implementation set, try to find correct default java class type

      // CASE 1: base type is a Scout bean, lookup most specific class to use
      if (m_beanManagerUtility.isBeanClass(baseType.getRawClass())) {
        IBean<?> uniqueBean = BEANS.getBeanManager().uniqueBean(baseType.getRawClass());
        if (uniqueBean != null) {
          builder.defaultImpl(uniqueBean.getBeanClazz());
        }
        // Note: if beanClazz is null, no unique bean implementation could be found, do not set a default implementation is this case!
      }
      else {
        // CASE 2: base type is not a Scout bean, use base type as default implementation
        builder.defaultImpl(baseType.getRawClass());
      }
    }
    return builder;
  }
}
