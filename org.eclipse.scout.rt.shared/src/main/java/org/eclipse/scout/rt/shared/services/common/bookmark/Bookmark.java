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
package org.eclipse.scout.rt.shared.services.common.bookmark;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bookmark implements Serializable, Cloneable, IOrdered {
  private static final Logger LOG = LoggerFactory.getLogger(Bookmark.class);
  private static final long serialVersionUID = 1L;

  /*
   * values for attribute "kind" (these values should not be refactored, since
   * they are also used in databases)
   */
  public static final int USER_BOOKMARK = 1;
  public static final int GLOBAL_BOOKMARK = 2;

  public static final String SPOOL_FOLDER_NAME = "[SPOOL]";
  public static final String INBOX_FOLDER_NAME = "[INBOX]";

  private long m_id;
  private int m_kind;
  private boolean m_new;
  private String m_title;
  private String m_text;
  private String m_keyStroke;
  private String m_outlineClassName;
  private List<AbstractPageState> m_path;
  private String m_iconId;
  private double m_order;
  /*
   * cache of serialized object
   */
  private transient byte[] m_serializedData;

  public Bookmark() {
  }

  public Bookmark(Bookmark bm) {
    importData(bm);
  }

  private void importData(Bookmark bm) {
    this.m_kind = bm.m_kind;
    this.m_title = bm.m_title;
    this.m_text = bm.m_text;
    this.m_iconId = bm.m_iconId;
    this.m_order = bm.m_order;
    this.m_keyStroke = bm.m_keyStroke;
    this.m_outlineClassName = bm.m_outlineClassName;
    if (bm.m_path != null) {
      this.m_path = new ArrayList<AbstractPageState>();
      for (AbstractPageState state : bm.m_path) {
        this.m_path.add((AbstractPageState) state.clone());
      }
    }
    m_serializedData = null;
  }

  public String getIconId() {
    return m_iconId;
  }

  public void setIconId(String iconid) {
    m_iconId = iconid;
    m_serializedData = null;
  }

  @Override
  public double getOrder() {
    return m_order;
  }

  @Override
  public void setOrder(double order) {
    m_order = order;
    m_serializedData = null;
  }

  public long getId() {
    return m_id;
  }

  public void setId(long id) {
    m_id = id;
  }

  public int getKind() {
    return m_kind;
  }

  public void setKind(int kind) {
    switch (kind) {
      case USER_BOOKMARK:
      case GLOBAL_BOOKMARK: {
        break;
      }
      default: {
        throw new IllegalArgumentException("invalid kind: " + kind);
      }
    }
    m_kind = kind;
    m_serializedData = null;
  }

  public String getTitle() {
    return m_title;
  }

  public void setTitle(String s) {
    m_title = s;
    m_serializedData = null;
  }

  public String getText() {
    return m_text;
  }

  public void setText(String s) {
    m_text = s;
    m_serializedData = null;
  }

  public String getKeyStroke() {
    return m_keyStroke;
  }

  public void setKeyStroke(String s) {
    m_keyStroke = s;
    m_serializedData = null;
  }

  public boolean isNew() {
    return m_new;
  }

  public void setNew(boolean bookmarkIsUnread) {
    m_new = bookmarkIsUnread;
  }

  public String getOutlineClassName() {
    return m_outlineClassName;
  }

  public void setOutlineClassName(String s) {
    m_outlineClassName = s;
    m_serializedData = null;
  }

  public List<AbstractPageState> getPath() {
    return CollectionUtility.arrayList(m_path);
  }

  public void addPathElement(AbstractPageState state) {
    if (m_path == null) {
      m_path = new ArrayList<AbstractPageState>();
    }
    m_path.add(state);
    m_serializedData = null;
  }

  public byte[] getSerializedData() {
    if (m_serializedData == null) {
      try {
        m_serializedData = SerializationUtility.createObjectSerializer().serialize(this);
      }
      catch (IOException e) {
        throw new ProcessingException("title: " + getTitle(), e);
      }
    }
    return m_serializedData;
  }

  public void setSerializedData(byte[] data) {
    try {
      Bookmark bm = SerializationUtility.createObjectSerializer().deserialize(data, Bookmark.class);
      importData(bm);
    }
    catch (IOException | ClassNotFoundException e) {
      throw new ProcessingException("title: " + getTitle(), e);
    }
    m_serializedData = data;
  }

  @Override
  public Object clone() {
    return new Bookmark(this);
  }

  private long getCRC() {
    CRC32 crc = new CRC32();
    try {
      byte[] a = getSerializedData();
      if (a != null) {
        crc.update(a);
      }
    }
    catch (RuntimeException e) {
      LOG.warn("CRC computation failed", e);
    }
    return crc.getValue();
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Bookmark) {
      Bookmark other = (Bookmark) o;
      return this.getCRC() == other.getCRC();
    }
    return false;
  }

  @Override
  public String toString() {
    return "Bookmark[title=" + getTitle() + ", id=" + getId() + ", kind=" + (getKind() == Bookmark.GLOBAL_BOOKMARK ? "Global" : "User") + "]";
  }
}
