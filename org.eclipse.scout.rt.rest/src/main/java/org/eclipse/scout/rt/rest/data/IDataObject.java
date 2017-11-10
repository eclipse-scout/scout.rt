/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.data;

import java.io.Serializable;

import org.eclipse.scout.rt.jackson.databind.JandexTypeNameIdResolver;
import org.eclipse.scout.rt.platform.Bean;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = JsonConstants.JSON_TYPE_PROPERTY)
@JsonTypeIdResolver(JandexTypeNameIdResolver.class)
@Bean
public interface IDataObject extends Serializable {
}
