package org.eclipse.scout.rt.jackson.dataobject;

import java.util.Date;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.IDataObject;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.ReferenceType;

/**
 * Deserializer provider for data object deserializer for ({@code DoEntity}, {@code DoValue} and {@code DoList}.
 */
@ApplicationScoped
public class DataObjectDeserializers extends Deserializers.Base {

  @Override
  public JsonDeserializer<?> findReferenceDeserializer(ReferenceType refType, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer contentTypeDeserializer, JsonDeserializer<?> contentDeserializer)
      throws JsonMappingException {
    if (refType.hasRawClass(DoValue.class)) {
      return new DoValueDeserializer(refType, null, contentTypeDeserializer, contentDeserializer);
    }
    return super.findReferenceDeserializer(refType, config, beanDesc, contentTypeDeserializer, contentDeserializer);
  }

  @Override
  public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
    if (IDoEntity.class.isAssignableFrom(type.getRawClass())) {
      return new DoEntityDeserializer(type.getRawClass().asSubclass(IDoEntity.class));
    }
    else if (DoList.class.isAssignableFrom(type.getRawClass())) {
      return new DoListDeserializer(type);
    }
    else if (Date.class.isAssignableFrom(type.getRawClass())) {
      return new DoDateDeserializer();
    }
    else if (IDataObject.class.isAssignableFrom(type.getRawClass())) {
      return new DataObjectDeserializer(type.getRawClass());
    }
    return super.findBeanDeserializer(type, config, beanDesc);
  }
}
