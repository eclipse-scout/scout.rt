/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.internal.tube;

import com.sun.xml.internal.ws.api.BindingID;
import com.sun.xml.internal.ws.api.pipe.TubelineAssembler;
import com.sun.xml.internal.ws.api.pipe.TubelineAssemblerFactory;

/**
 * Factory to provide a custom tubeline assembler. This factory is registered in file
 * META-INF/services/com.sun.xml.ws.api.pipe.TubelineAssemblerFactory.
 */
public class ScoutTubelineAssemblerFactory extends TubelineAssemblerFactory {

  @Override
  public TubelineAssembler doCreate(BindingID bindingid) {
    return new ScoutTubelineAssembler();
  }
}
