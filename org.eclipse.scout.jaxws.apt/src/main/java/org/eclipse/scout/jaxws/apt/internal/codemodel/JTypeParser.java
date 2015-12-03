/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.apt.internal.codemodel;

import javax.lang.model.type.TypeMirror;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;

/**
 * This class is only necessary due to a bug of {@link JCodeModel#parseType(String)}, that '&lt;?&gt;' generic
 * declarations (short for '&lt;? extends Object&gt;') are not supported properly.
 *
 * @since 5.1
 */
public class JTypeParser {

  private JTypeParser() {
  }

  /**
   * This method is similar to {@link JCodeModel#parseType(String)}, but fixes an issue that JCodeModel does not support
   * '&lt;?&gt;' generic declaration (short for '&lt;? extends Object&gt;').
   */
  public static JType parseType(final JCodeModel model, final String type) throws ClassNotFoundException {
    return model.parseType(type.replaceAll("<\\?>", "<? extends Object>"));
  }

  /**
   * This method is similar to {@link JCodeModel#parseType(String)}, but fixes an issue that JCodeModel does not support
   * '&lt;?&gt;' generic declaration (short for '&lt;? extends Object&gt;').
   */
  public static JType parseType(final JCodeModel model, final TypeMirror _typeMirror) throws ClassNotFoundException {
    return model.parseType(_typeMirror.toString().replaceAll("<\\?>", "<? extends Object>")); // bug in JCode model that it does not accept
  }

}
