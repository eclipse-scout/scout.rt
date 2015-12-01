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
package org.eclipse.scout.rt.shared.servicetunnel.internal;

import org.eclipse.scout.rt.platform.holders.HolderUtility;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.holders.NVPair;

public abstract class AbstractHolderArgumentVisitor {

  public abstract void visitHolder(IHolder<?> input, IHolder<?> output);

  public abstract void visitOther(Object[] input, Object[] output, int index);

  public void startVisiting(Object[] input, Object[] output, int maxDepth, boolean autoCreateOutputStructure) {
    visitImpl(input, output, 0, maxDepth, autoCreateOutputStructure);
  }

  /**
   * equality to Object[] is explicitly needed here, do not change to "instanceof"
   */
  private void visitImpl(Object[] input, Object[] output, int depth, int maxDepth, boolean autoCreateOutputStructure) {
    for (int i = 0; i < input.length; i++) {
      if (isObjectArray(input[i]) && isObjectArray(output[i])) {
        if (depth < maxDepth) {
          visitImpl((Object[]) input[i], (Object[]) output[i], depth + 1, maxDepth, autoCreateOutputStructure);
        }
        else {
          visitOther(input, output, i);
        }
      }
      else if (isObjectArray(input[i])) {
        if (depth < maxDepth) {
          if (output[i] == null && autoCreateOutputStructure) {
            output[i] = new Object[((Object[]) input[i]).length];
            visitImpl((Object[]) input[i], (Object[]) output[i], depth + 1, maxDepth, autoCreateOutputStructure);
          }
        }
        else {
          visitOther(input, output, i);
        }
      }
      else if (input[i] instanceof IHolder<?>) {
        if (output[i] == null && autoCreateOutputStructure) {
          output[i] = HolderUtility.createSerializableHolder((IHolder<?>) input[i]);
        }
        if (output[i] instanceof IHolder<?>) {
          visitHolder((IHolder<?>) input[i], (IHolder<?>) output[i]);
        }
        else {
          visitOther(input, output, i);
        }
      }
      else if (input[i] instanceof NVPair && ((NVPair) input[i]).getValue() instanceof IHolder<?>) {
        NVPair nv = (NVPair) input[i];
        if (output[i] == null && autoCreateOutputStructure) {
          output[i] = new NVPair(nv.getName(), HolderUtility.createSerializableHolder((IHolder<?>) nv.getValue()));
        }
        if (output[i] instanceof NVPair && ((NVPair) output[i]).getValue() instanceof IHolder<?>) {
          visitHolder((IHolder<?>) ((NVPair) input[i]).getValue(), (IHolder<?>) ((NVPair) output[i]).getValue());
        }
        else {
          visitOther(input, output, i);
        }
      }
      else {
        visitOther(input, output, i);
      }
    }
  }

  private boolean isObjectArray(Object o) {
    return o != null && (o.getClass() == Object[].class);
  }

}
