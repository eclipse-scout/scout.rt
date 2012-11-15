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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Class to write java instances into a compressed byte array. Use
 * CompressedObjectReader to unpack the byte array back into instances.
 */
public class CompressedObjectWriter {
  private ObjectOutputStream out;
  private DeflaterOutputStream zipOut;
  private ByteArrayOutputStream bytesOut;

  public CompressedObjectWriter(int outputBufferSize, int zipBufferSize) throws ProcessingException {
    try {
      Deflater d = new Deflater();
      d.setLevel(Deflater.BEST_COMPRESSION);

      bytesOut = new ByteArrayOutputStream(outputBufferSize);
      zipOut = new DeflaterOutputStream(bytesOut, d, zipBufferSize);
      out = new ObjectOutputStream(zipOut);
    }
    catch (IOException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  public void compress(Object o) throws ProcessingException {
    try {
      out.writeObject(o);
    }
    catch (IOException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  public byte[] getCompressedBytes() throws ProcessingException {
    try {
      out.flush();
      zipOut.finish();
      zipOut.flush();
      return bytesOut.toByteArray();
    }
    catch (IOException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  public void close() {
    try {
      out.close();
    }
    catch (IOException e) {
    }

    try {
      zipOut.close();
    }
    catch (IOException e) {
    }
  }

  /**
   * @see java.io.ObjectOutputStream#reset()
   * @throws ProcessingException
   */
  public void resetWrittenObjectCache() throws ProcessingException {
    try {
      out.reset();
    }
    catch (IOException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }
}
