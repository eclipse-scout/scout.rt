/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.prefs;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Preferences store for a single node of a single user scope.
 *
 * @since 5.1
 * @see Preferences#get(ISession, String)
 * @see IUserPreferencesService
 */
public interface IPreferences extends Serializable {

  /**
   * Associates the specified <code>value</code> with the specified <code>key</code> in this node.
   *
   * @param key
   *          Key with which the specified value is to be associated. Must not be <code>null</code>.
   * @param value
   *          Value to be associated with the specified <code>key</code>. If the value is null, the <code>key</code> is
   *          removed from this node.
   * @return <code>true</code> if this preference node has been changed. <code>false</code> otherwise.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   */
  boolean put(String key, String value);

  /**
   * Returns the value associated with the specified <code>key</code> in this node. Returns the specified default if
   * there is no value associated with the <code>key</code>.
   *
   * @param key
   *          Key whose associated value is to be returned. Must not be <code>null</code>.
   * @param def
   *          The value to be returned in case that this node has no value associated with the given <code>key</code>.
   * @return The value associated with <code>key</code>, or <code>def</code> if no value is associated with
   *         <code>key</code>.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   */
  String get(String key, String def);

  /**
   * Associates the specified values with the specified <code>key</code> in this node. This method is intended for use
   * in conjunction with {@link #getList(String, List)} method.
   *
   * @param key
   *          Key with which the value is to be associated. Must not be <code>null</code>.
   * @param values
   *          Values that are to be associated with <code>key</code>.
   * @return <code>true</code> if this preference node has been changed. <code>false</code> otherwise.
   * @throws IllegalArgumentException
   *           If either the <code>key</code> or <code>values</code> is <code>null</code>.
   * @see #getList(String, List)
   * @since 5.2
   */
  boolean putList(String key, List<String> values);

  /**
   * Returns the values associated with the specified <code>key</code> in this node.<br>
   * Returns the specified default if there is no value associated with the <code>key</code>. This method is intended
   * for use in conjunction with the {@link #putList(String, List)} method.
   *
   * @param key
   *          Key whose associated value is to be returned as a <code>long</code>. Must not be <code>null</code>.
   * @param defaultValues
   *          The value to be returned in case that this node has no value associated with <code>key</code> or the
   *          associated value cannot be interpreted as a <code>long</code>.
   * @return The list with values associated with <code>key</code> in this node, or <code>def</code> if the associated
   *         value does not exist or cannot be interpreted as a <code>long</code> type.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   * @see #putList(String, List)
   * @since 5.2
   */
  List<String> getList(String key, List<String> defaultValues);

  /**
   * Removes the value associated with the specified <code>key</code> in this node, if any.
   *
   * @param key
   *          Key whose mapping is to be removed from this node. Must not be <code>null</code>.
   * @return <code>true</code> if this preference node has been changed. <code>false</code> otherwise.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   */
  boolean remove(String key);

  /**
   * Removes all of the properties (key-value associations) in this node.
   *
   * @return <code>true</code> if this preference node has been changed. <code>false</code> if it was already empty.
   */
  boolean clear();

  /**
   * Associates the specified <code>int</code> value with the specified <code>key</code> in this node. This method is
   * intended for use in conjunction with {@link #getInt(String, int)} method.
   *
   * @param key
   *          Key with which the value is to be associated. Must not be <code>null</code>.
   * @param value
   *          Value that is to be associated with <code>key</code>.
   * @return <code>true</code> if this preference node has been changed. <code>false</code> otherwise.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   * @see #getInt(String,int)
   */
  boolean putInt(String key, int value);

  /**
   * Returns the <code>int</code> value associated with the specified <code>key</code> in this node.<br>
   * Returns the specified default if there is no value associated with the <code>key</code> or if the value cannot be
   * converted to an <code>int</code>. This method is intended for use in conjunction with the
   * {@link #putInt(String, int)} method.
   *
   * @param key
   *          Key whose associated value is to be returned as an <code>int</code>. Must not be <code>null</code>.
   * @param def
   *          The value to be returned in case that this node has no value associated with <code>key</code> or the
   *          associated value cannot be interpreted as an <code>int</code>.
   * @return The <code>int</code> value associated with <code>key</code> in this node, or <code>def</code> if the
   *         associated value does not exist or cannot be interpreted as an <code>int</code> type.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   * @see #putInt(String, int)
   */
  int getInt(String key, int def);

  /**
   * Associates the specified <code>long</code> value with the specified <code>key</code> in this node. This method is
   * intended for use in conjunction with {@link #getLong(String, long)} method.
   *
   * @param key
   *          Key with which the value is to be associated. Must not be <code>null</code>.
   * @param value
   *          Value that is to be associated with <code>key</code>.
   * @return <code>true</code> if this preference node has been changed. <code>false</code> otherwise.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   * @see #getLong(String,long)
   */
  boolean putLong(String key, long value);

  /**
   * Returns the <code>long</code> value associated with the specified <code>key</code> in this node.<br>
   * Returns the specified default if there is no value associated with the <code>key</code> or if the value cannot be
   * converted to a <code>long</code>. This method is intended for use in conjunction with the
   * {@link #putLong(String, long)} method.
   *
   * @param key
   *          Key whose associated value is to be returned as a <code>long</code>. Must not be <code>null</code>.
   * @param def
   *          The value to be returned in case that this node has no value associated with <code>key</code> or the
   *          associated value cannot be interpreted as a <code>long</code>.
   * @return The <code>long</code> value associated with <code>key</code> in this node, or <code>def</code> if the
   *         associated value does not exist or cannot be interpreted as a <code>long</code> type.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   * @see #putLong(String, long)
   */
  long getLong(String key, long def);

  /**
   * Associates the specified <code>boolean</code> value with the specified <code>key</code> in this node. This method
   * is intended for use in conjunction with {@link #getBoolean(String, boolean)} method.
   *
   * @param key
   *          Key with which the value is to be associated. Must not be <code>null</code>.
   * @param value
   *          Value that is to be associated with <code>key</code>.
   * @return <code>true</code> if this preference node has been changed. <code>false</code> otherwise.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   * @see #getBoolean(String,boolean)
   */
  boolean putBoolean(String key, boolean value);

  /**
   * Returns the <code>boolean</code> value associated with the specified <code>key</code> in this node.<br>
   * Returns the specified default if there is no value associated with the <code>key</code> or if the value cannot be
   * converted to a <code>boolean</code>. This method is intended for use in conjunction with the
   * {@link #putBoolean(String, boolean)} method.
   *
   * @param key
   *          Key whose associated value is to be returned as a <code>boolean</code>. Must not be <code>null</code>.
   * @param def
   *          The value to be returned in case that this node has no value associated with <code>key</code> or the
   *          associated value cannot be interpreted as a <code>boolean</code>.
   * @return The <code>boolean</code> value associated with <code>key</code> in this node, or <code>def</code> if the
   *         associated value does not exist or cannot be interpreted as a <code>boolean</code> type.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   * @see #putBoolean(String, boolean)
   */
  boolean getBoolean(String key, boolean def);

  /**
   * Associates the specified <code>float</code> value with the specified <code>key</code> in this node. This method is
   * intended for use in conjunction with {@link #getFloat(String, float)} method.
   *
   * @param key
   *          Key with which the value is to be associated. Must not be <code>null</code>.
   * @param value
   *          Value that is to be associated with <code>key</code>.
   * @return <code>true</code> if this preference node has been changed. <code>false</code> otherwise.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   * @see #getFloat(String,float)
   */
  boolean putFloat(String key, float value);

  /**
   * Returns the <code>float</code> value associated with the specified <code>key</code> in this node.<br>
   * Returns the specified default if there is no value associated with the <code>key</code> or if the value cannot be
   * converted to a <code>float</code>. This method is intended for use in conjunction with the
   * {@link #putFloat(String, float)} method.
   *
   * @param key
   *          Key whose associated value is to be returned as a <code>float</code>. Must not be <code>null</code>.
   * @param def
   *          The value to be returned in case that this node has no value associated with <code>key</code> or the
   *          associated value cannot be interpreted as a <code>float</code>.
   * @return The <code>float</code> value associated with <code>key</code> in this node, or <code>def</code> if the
   *         associated value does not exist or cannot be interpreted as a <code>float</code> type.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   * @see #putFloat(String, float)
   */
  float getFloat(String key, float def);

  /**
   * Associates the specified <code>double</code> value with the specified <code>key</code> in this node. This method is
   * intended for use in conjunction with {@link #getDouble(String, double)} method.
   *
   * @param key
   *          Key with which the value is to be associated. Must not be <code>null</code>.
   * @param value
   *          Value that is to be associated with <code>key</code>.
   * @return <code>true</code> if this preference node has been changed. <code>false</code> otherwise.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   * @see #getDouble(String,double)
   */
  boolean putDouble(String key, double value);

  /**
   * Returns the <code>double</code> value associated with the specified <code>key</code> in this node.<br>
   * Returns the specified default if there is no value associated with the <code>key</code> or if the value cannot be
   * converted to a <code>double</code>. This method is intended for use in conjunction with the
   * {@link #putDouble(String, double)} method.
   *
   * @param key
   *          Key whose associated value is to be returned as a <code>double</code>. Must not be <code>null</code>.
   * @param def
   *          The value to be returned in case that this node has no value associated with <code>key</code> or the
   *          associated value cannot be interpreted as a <code>double</code>.
   * @return The <code>double</code> value associated with <code>key</code> in this node, or <code>def</code> if the
   *         associated value does not exist or cannot be interpreted as a <code>double</code> type.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   * @see #putDouble(String, double)
   */
  double getDouble(String key, double def);

  /**
   * Associates the specified <code>byte[]</code> value with the specified <code>key</code> in this node. This method is
   * intended for use in conjunction with {@link #getByteArray(String, byte[])} method.
   *
   * @param key
   *          Key with which the value is to be associated. Must not be <code>null</code>.
   * @param value
   *          Value that is to be associated with <code>key</code>.
   * @return <code>true</code> if this preference node has been changed. <code>false</code> otherwise.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   * @see #getByteArray(String,byte[])
   */
  boolean putByteArray(String key, byte[] value);

  /**
   * Returns the <code>byte[]</code> value associated with the specified <code>key</code> in this node.<br>
   * Returns the specified default if there is no value associated with the <code>key</code> or if the value cannot be
   * converted to a <code>byte[]</code>. This method is intended for use in conjunction with the
   * {@link #putByteArray(String, byte[])} method.
   *
   * @param key
   *          Key whose associated value is to be returned as a <code>byte[]</code>. Must not be <code>null</code>.
   * @param def
   *          The value to be returned in case that this node has no value associated with <code>key</code> or the
   *          associated value cannot be interpreted as a <code>byte[]</code>.
   * @return The <code>byte[]</code> value associated with <code>key</code> in this node, or <code>def</code> if the
   *         associated value does not exist or cannot be interpreted as a <code>byte[]</code> type.
   * @throws IllegalArgumentException
   *           If the <code>key</code> is <code>null</code>.
   * @see #putByteArray(String, byte[])
   */
  byte[] getByteArray(String key, byte[] def);

  /**
   * Gets the number of properties stored in this node.
   *
   * @return The number of properties in this node.
   */
  int size();

  /**
   * Returns all of the keys that have an associated value in this node.
   *
   * @return A {@link Set} containing all keys in this node.
   */
  Set<String> keys();

  /**
   * Gets the name (ID) of this node.
   *
   * @return The name of this node.
   */
  String name();

  /**
   * Gets the {@link ISession} that represents the user scope of this node.
   *
   * @return The {@link ISession} this node belongs to.
   */
  ISession userScope();

  /**
   * Forces any changes in the contents of this node and its descendants to the persistent store.
   * <p>
   * Once this method returns successfully, it is safe to assume that all changes made in the subtree rooted at this
   * node prior to the method invocation have become permanent.
   * <p>
   * Implementations are free to flush changes into the persistent store at any time. They do not need to wait for this
   * method to be called.
   * <p>
   * When a flush occurs on a newly created node, it is made persistent, as are any ancestors (and descendants) that
   * have yet to be made persistent. Note however that any properties value changes in ancestors are <i>not </i>
   * guaranteed to be made persistent.
   *
   * @throws BackingStoreException
   *           if this operation cannot be completed due to a failure in the backing store, or inability to communicate
   *           with it.
   * @throws IllegalStateException
   *           if this node (or an ancestor) has been removed with the {@link #removeNode()} method.
   * @see #sync()
   */

  /**
   * Flushes any changes in the contents of this node to the persistent store (if any).
   *
   * @return <code>true</code> if this node has been successfully flushed to the store. <code>false</code> if this node
   *         is not dirty (there are no changes since the last flush) or there is no persistent store configured.
   * @throws ProcessingException
   *           On an error while persisting this node.
   */
  boolean flush();

  /**
   * Adds the given {@link IPreferenceChangeListener} to the list of listeners to be notified about preference changes.
   *
   * @param listener
   *          The listener to add.
   */
  void addPreferenceChangeListener(IPreferenceChangeListener listener);

  /**
   * Removes the given {@link IPreferenceChangeListener} from the list of listeners to be notified about preference
   * changes.
   *
   * @param listener
   *          The listener to remove.
   */
  void removePreferenceChangeListener(IPreferenceChangeListener listener);
}
