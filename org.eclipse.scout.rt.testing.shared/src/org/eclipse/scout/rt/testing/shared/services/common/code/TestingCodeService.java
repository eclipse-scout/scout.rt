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
package org.eclipse.scout.rt.testing.shared.services.common.code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleClassDescriptor;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICodeVisitor;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Dynamic code service for testing purposes. Arbitrary code types can be registered dynamically. Consumers must
 * register and remove the service themselves.
 * <p/>
 * <b>Example</b>:
 * 
 * <pre>
 * List<ServiceRegistration> reg = TestingUtility.registerServices(Activator.getDefault().getBundle(), 1000, new TestingCodeService(new MyCodeType()));
 * CODES.getCodeType(MyCodeType.class);
 * [...]
 * SERVICES.getService(TestingCodeService.class).addCodeTypes(new OtherCodeType());
 * [..]
 * TestingUtility.unregisterServices(reg);
 * </pre>
 * 
 * @since 3.8.0
 */
public class TestingCodeService extends AbstractService implements ICodeService {

  private final static IScoutLogger LOG = ScoutLogManager.getLogger(TestingCodeService.class);

  private final Map<Class<? extends ICodeType>, ICodeType<?>> m_codeTypes;
  private final Object m_codeTypeMapLock;

  public TestingCodeService(ICodeType<?>... codeTypes) {
    m_codeTypes = new HashMap<Class<? extends ICodeType>, ICodeType<?>>();
    m_codeTypeMapLock = new Object();
    addCodeTypes(codeTypes);
  }

  public void addCodeTypes(ICodeType<?>... codeTypes) {
    synchronized (m_codeTypeMapLock) {
      for (ICodeType<?> ct : codeTypes) {
        if (ct != null) {
          m_codeTypes.put(ct.getClass(), ct);
        }
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ICodeType> T getCodeType(Class<T> type) {
    synchronized (m_codeTypeMapLock) {
      return (T) m_codeTypes.get(type);
    }
  }

  @Override
  public <T extends ICodeType> T getCodeType(Long partitionId, Class<T> type) {
    synchronized (m_codeTypeMapLock) {
      return getCodeType(type);
    }
  }

  @Override
  public ICodeType findCodeTypeById(Object id) {
    synchronized (m_codeTypeMapLock) {
      for (ICodeType<?> ct : m_codeTypes.values()) {
        if (CompareUtility.equals(ct.getId(), id)) {
          return ct;
        }
      }
      return null;
    }
  }

  @Override
  public ICodeType findCodeTypeById(Long partitionId, Object id) {
    synchronized (m_codeTypeMapLock) {
      return findCodeTypeById(id);
    }
  }

  @Override
  public ICodeType[] getCodeTypes(Class... types) {
    synchronized (m_codeTypeMapLock) {
      List<ICodeType> result = new ArrayList<ICodeType>();
      for (Class type : types) {
        @SuppressWarnings("unchecked")
        ICodeType ct = getCodeType(type);
        if (ct != null) {
          result.add(ct);
        }
      }
      return result.toArray(new ICodeType[result.size()]);
    }
  }

  @Override
  public ICodeType[] getCodeTypes(Long partitionId, Class... types) {
    synchronized (m_codeTypeMapLock) {
      return getCodeTypes(types);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ICode> T getCode(final Class<T> type) {
    synchronized (m_codeTypeMapLock) {
      if (type == null) {
        return null;
      }
      Class declaringCodeTypeClass = null;
      if (type.getDeclaringClass() != null) {
        // code is inner type of code type or another code
        Class c = type.getDeclaringClass();
        while (c != null && !(ICodeType.class.isAssignableFrom(c))) {
          c = c.getDeclaringClass();
        }
        declaringCodeTypeClass = c;
      }
      if (declaringCodeTypeClass == null) {
        try {
          declaringCodeTypeClass = type.newInstance().getCodeType().getClass();
        }
        catch (Throwable t) {
          LOG.error("find code " + type, t);
        }
      }
      ICodeType codeType = getCodeType(declaringCodeTypeClass);
      final Holder<ICode> codeHolder = new Holder<ICode>(ICode.class);
      ICodeVisitor v = new ICodeVisitor() {
        @Override
        public boolean visit(ICode code, int treeLevel) {
          if (code.getClass() == type) {
            codeHolder.setValue(code);
            return false;
          }
          return true;
        }
      };
      codeType.visit(v);
      return (T) codeHolder.getValue();
    }
  }

  @Override
  public <T extends ICode> T getCode(Long partitionId, Class<T> type) {
    synchronized (m_codeTypeMapLock) {
      return getCode(type);
    }
  }

  @Override
  public <T extends ICodeType> T reloadCodeType(Class<T> type) {
    synchronized (m_codeTypeMapLock) {
      LOG.warn("reloading code types is not supported by this testing ICodeService");
      return getCodeType(type);
    }
  }

  @Override
  public ICodeType[] reloadCodeTypes(Class... types) {
    synchronized (m_codeTypeMapLock) {
      LOG.warn("reloading code types is not supported by this testing ICodeService");
      return getCodeTypes(types);
    }
  }

  @Override
  public BundleClassDescriptor[] getAllCodeTypeClasses(String classPrefix) {
    synchronized (m_codeTypeMapLock) {
      List<BundleClassDescriptor> result = new ArrayList<BundleClassDescriptor>();
      for (Class<? extends ICodeType> type : m_codeTypes.keySet()) {
        Bundle bundle = FrameworkUtil.getBundle(type);
        result.add(new BundleClassDescriptor(bundle.getSymbolicName(), type.getName()));
      }
      return result.toArray(new BundleClassDescriptor[result.size()]);
    }
  }

  @Override
  public ICodeType[] getAllCodeTypes(String classPrefix) {
    synchronized (m_codeTypeMapLock) {
      List<ICodeType> result = new ArrayList<ICodeType>();
      for (ICodeType ct : m_codeTypes.values()) {
        if (ct.getClass().getName().startsWith(classPrefix)) {
          result.add(ct);
        }
      }
      return result.toArray(new ICodeType[result.size()]);
    }
  }

  @Override
  public ICodeType[] getAllCodeTypes(String classPrefix, Long partitionId) {
    synchronized (m_codeTypeMapLock) {
      return getAllCodeTypes(classPrefix);
    }
  }
}
