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
package org.eclipse.scout.rt.shared.data;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.DefaultFormDataValidator;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;

/**
 * Does input/output validation of arbitrary serializable data.
 * <p>
 * This default traverses all objects of the arguments map in the complete data structure by writing the object to a
 * void stream and calling {@link #validateObject()} on every traversed Object in the hierarchy.
 * <p>
 * This default delegates {@link AbstractFormData} to a {@link DefaultFormDataValidator} and does nothing otherwise.
 */
public class DefaultInboundValidator {
  private final ServiceTunnelRequest m_req;

  public DefaultInboundValidator(ServiceTunnelRequest req) {
    m_req = req;
  }

  public ServiceTunnelRequest getServiceTunnelRequest() {
    return m_req;
  }

  public void validate() throws Exception {
    Object[] args = getServiceTunnelRequest().getArgs();
    if (args == null || args.length == 0) {
      return;
    }
    new ObjectTreeVisitor() {
      @Override
      void visitObject(Object obj) throws Exception {
        validateObject(obj);
      }
    }.writeObject(args);
  }

  protected void validateObject(Object obj) throws Exception {
    if (obj instanceof AbstractFormData) {
      new DefaultFormDataValidator((AbstractFormData) obj).validate();
    }
  }

  public static abstract class ObjectTreeVisitor extends ObjectOutputStream {
    public ObjectTreeVisitor() throws IOException {
      super(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
          //nop
        }
      });
      enableReplaceObject(true);
    }

    @Override
    protected Object replaceObject(Object obj) throws IOException {
      try {
        visitObject(obj);
      }
      catch (IOException ioe) {
        throw ioe;
      }
      catch (Exception e) {
        throw new IOException(e.getMessage());
      }
      return obj;
    }

    abstract void visitObject(Object obj) throws Exception;

  }

}
