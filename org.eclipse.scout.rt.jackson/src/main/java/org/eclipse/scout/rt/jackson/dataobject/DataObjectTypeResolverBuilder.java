/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
