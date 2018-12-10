/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.client.proxy.api;

import java.util.Collection;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("scout.ApiSignature")
public class ApiSignatureDo extends DoEntity {

  public DoList<ClassSignatureDo> classes() {
    return doList("classes");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ApiSignatureDo withClasses(Collection<? extends ClassSignatureDo> classes) {
    classes().updateAll(classes);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ApiSignatureDo withClasses(ClassSignatureDo... classes) {
    classes().updateAll(classes);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<ClassSignatureDo> getClasses() {
    return classes().get();
  }
}
