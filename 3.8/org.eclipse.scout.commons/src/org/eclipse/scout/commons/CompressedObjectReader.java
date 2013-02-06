/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.InflaterInputStream;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Class to get java instances from a compressed byte array created with
 * CompressedObjectWriter.
 */
public class CompressedObjectReader {

  private ObjectInputStream in;
  private Object currentObject;

  public CompressedObjectReader(byte[] data) throws ProcessingException {
    try {
      currentObject = null;
      InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(data));
      in = new ObjectInputStream(iis);
    }
    catch (IOException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  public boolean next() throws ProcessingException {
    try {
      currentObject = in.readObject();
      return true;
    }
    catch (EOFException e) {
      currentObject = null;
      return false;
    }
    catch (Exception e) {
      currentObject = null;
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  public Object getCurrentObject() {
    return currentObject;
  }

  public void close() {
    try {
      in.close();
    }
    catch (IOException e) {
    }
  }
}
