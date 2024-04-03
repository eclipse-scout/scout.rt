/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.config;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.api.data.config.mapping.AbstractConfigPropertyToDoFunction;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.config.IConfigProperty;

/**
 * Do for {@link IConfigProperty}. Use {@link AbstractApiExposedConfigPropertyContributor#create(IConfigProperty)} to
 * convert a property to a DO.
 * <p>
 * The {@link #value()} passed to this DO must be a data type which can be serialized to json. This works fine e.g. for
 * base types like numbers, boolean and strings as well as collections like arrays, lists and maps consisting of such
 * base types. For other non-serializable custom data types create an {@link AbstractConfigPropertyToDoFunction} for the
 * property and convert the value to a serializable type (e.g. another DO) by using
 * {@link AbstractConfigPropertyToDoFunction#getPropertyValue(IConfigProperty)}.
 */
@TypeName("scout.ConfigProperty")
public class ConfigPropertyDo extends DoEntity {

  public DoValue<String> key() {
    return doValue("key");
  }

  /**
   * The value passed to this DO must be a data type which can be serialized to json. This works fine e.g.
   * for base types like numbers, boolean and strings as well as collections like arrays, lists and maps consisting of
   * such base types. For other non-serializable custom data types create an {@link AbstractConfigPropertyToDoFunction}
   * for the property and convert the value to a serializable type (e.g. another DO) by using
   * {@link AbstractConfigPropertyToDoFunction#getPropertyValue(IConfigProperty)}.
   *
   * @return this instance
   */
  public DoValue<Object> value() {
    return doValue("value");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ConfigPropertyDo withKey(String key) {
    key().set(key);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getKey() {
    return key().get();
  }

  /**
   * See {@link #value()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public ConfigPropertyDo withValue(Object value) {
    value().set(value);
    return this;
  }

  /**
   * See {@link #value()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Object getValue() {
    return value().get();
  }
}
