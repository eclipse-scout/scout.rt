/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.DefaultSerializerBlacklistAppendProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.DefaultSerializerBlacklistReplaceProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.DefaultSerializerWhitelistProperty;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

/**
 * Base implementation of {@link IObjectSerializer}. Subclasses must provide a suitable {@link ObjectInputStream}.
 * <p>
 * Since 11.0 uses {@link DefaultSerializerBlacklist}. WARNING: The default whitelist is '.*'. Verify and ensure to use
 * a proper local whitelist by calling {@link #withWhitelist(Predicate)} or setting the global default
 * {@link DefaultSerializerWhitelistProperty} in the config.properties.
 *
 * @see DefaultSerializerWhitelist
 * @since 3.8.2
 */
public abstract class AbstractObjectSerializer implements IObjectSerializer {

  protected final IObjectReplacer m_objectReplacer;
  protected Predicate<String> m_blacklist = BEANS.get(DefaultSerializerBlacklist.class);
  protected Predicate<String> m_whitelist = BEANS.get(DefaultSerializerWhitelist.class);

  public AbstractObjectSerializer(IObjectReplacer objectReplacer) {
    m_objectReplacer = objectReplacer;
  }

  public IObjectReplacer getObjectReplacer() {
    return m_objectReplacer;
  }

  @Override
  public Predicate<String> getBlacklist() {
    return m_blacklist;
  }

  @Override
  public IObjectSerializer withBlacklist(Predicate<String> blacklist) {
    m_blacklist = blacklist;
    return this;
  }

  @Override
  public Predicate<String> getWhitelist() {
    return m_whitelist;
  }

  @Override
  public IObjectSerializer withWhitelist(Predicate<String> whitelist) {
    m_whitelist = whitelist;
    return this;
  }

  @Override
  public byte[] serialize(Object o) throws IOException {
    if (o == null) {
      return null;
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    serialize(bos, o);
    return bos.toByteArray();
  }

  @Override
  public void serialize(OutputStream out, Object o) throws IOException {
    if (out == null) {
      return;
    }
    try (ObjectOutputStream oos = createObjectOutputStream(out)) {
      oos.writeObject(o);
      oos.flush();
    }
  }

  @Override
  public <T> T deserialize(byte[] buf, Class<T> expectedType) throws IOException, ClassNotFoundException {
    if (buf == null) {
      return null;
    }
    return deserialize(new ByteArrayInputStream(buf), expectedType);
  }

  @Override
  public <T> T deserialize(InputStream in, Class<T> expectedType) throws IOException, ClassNotFoundException {
    if (in == null) {
      return null;
    }
    try (ObjectInputStream ois = createObjectInputStream(in)) {
      Object o = ois.readObject();
      if (expectedType != null && !expectedType.isInstance(o)) {
        throw new IOException("deserialized object has unexpected type: expected '" + expectedType + "', actual '" + o.getClass() + "'.");
      }
      @SuppressWarnings("unchecked")
      T castedObject = (T) o;
      return castedObject;
    }
  }

  @Override
  public ObjectOutputStream createObjectOutputStream(OutputStream out) throws IOException {
    return new ReplacingObjectOutputStream(out, getObjectReplacer());
  }

  @Override
  public ObjectInputStream createObjectInputStream(InputStream in) throws IOException {
    return new ResolvingObjectInputStream(in, getObjectReplacer(), getBlacklist(), getWhitelist());
  }

  public static class ReplacingObjectOutputStream extends ObjectOutputStream {
    protected final IObjectReplacer m_objectReplacer;

    public ReplacingObjectOutputStream(OutputStream out, IObjectReplacer objectReplacer) throws IOException {
      super(out);
      m_objectReplacer = objectReplacer;
      if (m_objectReplacer != null) {
        enableReplaceObject(true);
      }
    }

    @Override
    protected Object replaceObject(Object obj) throws IOException {
      if (m_objectReplacer == null) {
        return obj;
      }
      return m_objectReplacer.replaceObject(obj);
    }
  }

  public static class ResolvingObjectInputStream extends ObjectInputStream {
    protected final IObjectReplacer m_objectReplacer;
    protected Predicate<String> m_blacklist;
    protected Predicate<String> m_whitelist;

    public ResolvingObjectInputStream(InputStream in, IObjectReplacer objectReplacer, Predicate<String> blacklist, Predicate<String> whitelist) throws IOException {
      super(in);
      m_objectReplacer = objectReplacer;
      m_blacklist = blacklist;
      m_whitelist = whitelist;
      if (m_objectReplacer != null) {
        enableResolveObject(true);
      }
    }

    /**
     * OWASP: check for unsecure deserialization classes using a blacklist and a whitelist
     */
    @Override
    protected Class<?> resolveClass(ObjectStreamClass osc) throws IOException, ClassNotFoundException {
      if (m_blacklist.test(osc.getName())) {
        throw new ProcessingException("Security check: deseserialization of class '{}'. This class is blacklisted. To change the blacklist use config property '{}' or '{}' and consider {}",
            osc.getName(),
            BEANS.get(DefaultSerializerBlacklistAppendProperty.class).getKey(),
            BEANS.get(DefaultSerializerBlacklistReplaceProperty.class).getKey(),
            DefaultSerializerBlacklist.class);
      }
      if (!m_whitelist.test(osc.getName())) {
        throw new ProcessingException("Security check: deseserialization of class '{}'. This class is not whitelisted. To change the blacklist use config property '{}' and consider {}",
            osc.getName(),
            BEANS.get(DefaultSerializerWhitelistProperty.class).getKey(),
            DefaultSerializerWhitelist.class);
      }
      return super.resolveClass(osc);
    }

    @Override
    protected Object resolveObject(Object obj) throws IOException {
      if (m_objectReplacer == null) {
        return obj;
      }
      return m_objectReplacer.resolveObject(obj);
    }
  }
}
