/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

/**
 * Mutable wrapper class for an underlying String, so that we do not need to copy/allocate Strings all the time if sub
 * strings are needed and CharSequence is sufficient. Not thread safe.
 */
public class SubStringView implements CharSequence {

  private final String m_string;
  private int m_startIndexInclusive;
  private int m_endIndexExclusive;

  public SubStringView(String string) {
    this(string, 0);
  }

  public SubStringView(String string, int startIndexInclusive) {
    this(string, startIndexInclusive, string.length());
  }

  public SubStringView(String string, int startIndexInclusive, int endIndexExclusive) {
    m_string = Assertions.assertNotNull(string);
    m_startIndexInclusive = startIndexInclusive;
    m_endIndexExclusive = endIndexExclusive;
    assertConsistent();
  }

  /* ****************************************************************************************** */

  @Override
  public int length() {
    return m_endIndexExclusive - m_startIndexInclusive;
  }

  @Override
  public char charAt(int index) {
    return m_string.charAt(m_startIndexInclusive + index);
  }

  @Override
  public SubStringView subSequence(int start, int end) {
    return new SubStringView(m_string, m_startIndexInclusive + start, m_startIndexInclusive + end);
  }

  /* ****************************************************************************************** */

  public int getStartIndexInclusive() {
    return m_startIndexInclusive;
  }

  public void setStartIndexInclusive(int startIndexInclusive) {
    m_startIndexInclusive = startIndexInclusive;
    assertConsistent();
  }

  public void incrementStartIndexInclusiveBy(int increment) {
    m_startIndexInclusive += increment;
    assertConsistent();
  }

  /* ****************************************************************************************** */

  public int getEndIndexExclusive() {
    return m_endIndexExclusive;
  }

  public void decrementEndIndexExclusiveBy(int decrement) {
    m_endIndexExclusive -= decrement;
    assertConsistent();
  }

  public void setEndIndexExclusive(int endIndexExclusive) {
    m_endIndexExclusive = endIndexExclusive;
    assertConsistent();
  }

  public boolean isEmpty() {
    return m_startIndexInclusive == m_endIndexExclusive;
  }

  /* ****************************************************************************************** */

  protected void assertConsistent() {
    Assertions.assertTrue(isConsistent(), "Inconsistent data provided. String = {}. startIndex = {}, endIndex = {}", m_string, m_startIndexInclusive, m_endIndexExclusive);
  }

  protected boolean isConsistent() {
    return m_startIndexInclusive >= 0
        && m_startIndexInclusive <= m_endIndexExclusive
        && m_endIndexExclusive <= m_string.length();
  }

  /* ****************************************************************************************** */

  /**
   * Similar to {@link String#trim()}, but modifies itself if needed.
   */
  public void trim() {
    int start = m_startIndexInclusive;
    int end = m_endIndexExclusive;
    while ((start < end) && (m_string.charAt(start) <= ' ')) {
      start++;
    }
    while ((start < end) && (m_string.charAt(end - 1) <= ' ')) {
      end--;
    }
    m_startIndexInclusive = start;
    m_endIndexExclusive = end;
  }

  @Override
  public String toString() {
    return m_string.substring(m_startIndexInclusive, m_endIndexExclusive); // DO NOT change this
  }
}
