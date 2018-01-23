package org.eclipse.scout.rt.jackson.dataobject;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.AttributeName;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

/**
 * {@link DoEntity} implementation carrying a type name. This class is used for {@link DoEntity} instances if the input
 * provided a type attribute but the concrete {@link DoEntity} sub class is not available at runtime when deserializing
 * the content.
 * <p>
 * Example scenario: A (proxy) server parsing and forwarding serialized content which contains type information for
 * classes not available to the forwarding server. The type information should be preserved and forwarded as is.
 * <p>
 * <b>Caution</b> This class is not intended to be used as base type for custom entities with a type name, use
 * {@link DoEntity} with the {@link TypeName} annotation instead.
 */
public class DoTypedEntity extends DoEntity {

  // Imitate JSON type property using a standard DoValue attribute
  @AttributeName(DataObjectTypeResolverBuilder.JSON_TYPE_PROPERTY)
  public DoValue<String> type() {
    return doValue(DataObjectTypeResolverBuilder.JSON_TYPE_PROPERTY);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public DoTypedEntity withType(String type) {
    type().set(type);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getType() {
    return type().get();
  }
}
