/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.serialization;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.DefaultSerializerBlacklistAppendProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.DefaultSerializerBlacklistReplaceProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.DefaultSerializerWhitelistProperty;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.LongHolder;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.eclipse.scout.rt.testing.platform.serialization.SerializationTestUtility;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test if the blacklist correctly detects bad classes
 */
@RunWith(PlatformTestRunner.class)
@RunWithNewPlatform
public class OwaspDeserializationTest {

  @Before
  public void before() {
    BEANS.get(DefaultSerializerBlacklistAppendProperty.class).invalidate();
    BEANS.get(DefaultSerializerBlacklistReplaceProperty.class).invalidate();
    BEANS.get(DefaultSerializerBlacklist.class).reset();

    BEANS.get(DefaultSerializerWhitelistProperty.class).invalidate();
    BEANS.get(DefaultSerializerWhitelist.class).reset();
  }

  protected void setDefaultBlacklist(String replace, String append) {
    BEANS.get(DefaultSerializerBlacklistReplaceProperty.class).invalidate();
    BEANS.get(DefaultSerializerBlacklistReplaceProperty.class).setValue(replace);
    BEANS.get(DefaultSerializerBlacklistAppendProperty.class).invalidate();
    BEANS.get(DefaultSerializerBlacklistAppendProperty.class).setValue(append);
    BEANS.get(DefaultSerializerBlacklist.class).reset();
  }

  protected void setDefaultWhitelist(String regex) {
    BEANS.get(DefaultSerializerWhitelistProperty.class).invalidate();
    BEANS.get(DefaultSerializerWhitelistProperty.class).setValue(regex);
    BEANS.get(DefaultSerializerWhitelist.class).reset();
  }

  @Test
  public void testBlacklistBaseData() {
    Predicate<String> blacklist = SerializationUtility.createObjectSerializer().getBlacklist();
    for (String c : DefaultSerializerBlacklist.PROBLEMATIC_CLASSES) {
      assertTrue(blacklist.test(c));
      assertTrue(blacklist.test(c.substring(0, c.length() - 1)));
    }
    assertFalse(blacklist.test(String.class.getName()));
    assertFalse(blacklist.test(LongHolder.class.getName()));
  }

  @Test
  public void testLocalBlacklistEmpty() throws IOException, ClassNotFoundException {
    Predicate<String> blacklist = SerializationUtility.createBlacklistPolicy((String) null);
    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), Long.valueOf(123)));
    SerializationUtility
        .createObjectSerializer()
        .withBlacklist(blacklist)
        .deserialize(bytes, List.class);
  }

  @Test
  public void testLocalBlacklistOk() throws IOException, ClassNotFoundException {
    Predicate<String> blacklist = SerializationUtility.createBlacklistPolicy(Pattern.quote("java.util.concurrent.atomic.AtomicLong"));
    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), Long.valueOf(123)));
    SerializationUtility
        .createObjectSerializer()
        .withBlacklist(blacklist)
        .deserialize(bytes, List.class);
  }

  @Test(expected = ProcessingException.class)
  public void testLocalBlacklistNok() throws IOException, ClassNotFoundException {
    Predicate<String> blacklist = SerializationUtility.createBlacklistPolicy(Pattern.quote("java.util.concurrent.atomic.AtomicLong"));
    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), new AtomicLong(123)));
    SerializationUtility
        .createObjectSerializer()
        .withBlacklist(blacklist)
        .deserialize(bytes, List.class);
  }

  @Test
  public void testDefaultBlacklistOk() throws IOException, ClassNotFoundException {
    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), Long.valueOf(123)));
    SerializationUtility
        .createObjectSerializer()
        .deserialize(bytes, List.class);
  }

  @Test(expected = ProcessingException.class)
  public void testDefaultBlacklistNok() throws IOException, ClassNotFoundException {
    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), new javax.naming.Reference("foo")));
    SerializationUtility
        .createObjectSerializer()
        .deserialize(bytes, List.class);
  }

  @Test
  public void testDefaultBlacklistWithAppendOk() throws IOException, ClassNotFoundException {
    setDefaultBlacklist(null, "java\\.util\\.concurrent\\.atomic\\.AtomicLong");

    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), Long.valueOf(123)));
    SerializationUtility
        .createObjectSerializer()
        .deserialize(bytes, List.class);
  }

  @Test(expected = ProcessingException.class)
  public void testDefaultBlacklistWithAppendNok1() throws IOException, ClassNotFoundException {
    setDefaultBlacklist(null, "java\\.util\\.concurrent\\.atomic\\.AtomicLong");

    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), new AtomicLong(123)));
    SerializationUtility
        .createObjectSerializer()
        .deserialize(bytes, List.class);
  }

  @Test(expected = ProcessingException.class)
  public void testDefaultBlacklistWithAppendNok2() throws IOException, ClassNotFoundException {
    setDefaultBlacklist(null, "java\\.util\\.concurrent\\.atomic\\.AtomicLong");

    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), new javax.naming.Reference("foo")));
    SerializationUtility
        .createObjectSerializer()
        .deserialize(bytes, List.class);
  }

  @Test
  public void testDefaultBlacklistWithReplaceOk() throws IOException, ClassNotFoundException {
    setDefaultBlacklist("java\\.util\\.concurrent\\.atomic\\.AtomicLong", null);

    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), Long.valueOf(123)));
    SerializationUtility
        .createObjectSerializer()
        .deserialize(bytes, List.class);
  }

  @Test(expected = ProcessingException.class)
  public void testDefaultBlacklistWithReplaceNok1() throws IOException, ClassNotFoundException {
    setDefaultBlacklist("java\\.util\\.concurrent\\.atomic\\.AtomicLong", null);

    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), new AtomicLong(123)));
    SerializationUtility
        .createObjectSerializer()
        .deserialize(bytes, List.class);
  }

  @Test
  public void testDefaultBlacklistWithReplaceOk2() throws IOException, ClassNotFoundException {
    setDefaultBlacklist("java\\.util\\.concurrent\\.atomic\\.AtomicLong", null);

    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), new javax.naming.Reference("foo")));
    SerializationUtility
        .createObjectSerializer()
        .deserialize(bytes, List.class);
  }

  @Test
  public void testLocalWhitelistOk() throws IOException, ClassNotFoundException {
    Predicate<String> whitelist = SerializationUtility.createWhitelistPolicy(
        "\\[.*",
        "(byte|char|short|int|long|double|float|boolean)",
        Pattern.quote("java.util.Arrays$ArrayList"),
        Pattern.quote("java.lang.Number"),
        Pattern.quote("java.lang.Long"));
    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), Long.valueOf(123)));
    SerializationUtility
        .createObjectSerializer()
        .withWhitelist(whitelist)
        .deserialize(bytes, List.class);
  }

  @Test(expected = ProcessingException.class)
  public void testLocalWhitelistNok() throws IOException, ClassNotFoundException {
    Predicate<String> whitelist = SerializationUtility.createWhitelistPolicy(
        "\\[.*",
        "(byte|char|short|int|long|double|float|boolean)",
        Pattern.quote("java.util.Arrays$ArrayList"),
        Pattern.quote("java.lang.Number"),
        Pattern.quote("java.lang.Long"));
    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), Integer.valueOf(123)));
    SerializationUtility
        .createObjectSerializer()
        .withWhitelist(whitelist)
        .deserialize(bytes, List.class);
  }

  @Test
  public void testDefaultWhitelistOk() throws IOException, ClassNotFoundException {
    setDefaultWhitelist(String.join(", ",
        "\\[.*",
        "(byte|char|short|int|long|double|float|boolean)",
        Pattern.quote("java.util.Arrays$ArrayList"),
        Pattern.quote("java.lang.Number"),
        Pattern.quote("java.lang.Long")));

    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), Long.valueOf(123)));
    SerializationUtility
        .createObjectSerializer()
        .deserialize(bytes, List.class);
  }

  @Test(expected = ProcessingException.class)
  public void testDefaultWhitelistNok() throws IOException, ClassNotFoundException {
    setDefaultWhitelist(String.join(", ",
        "\\[.*",
        "(byte|char|short|int|long|double|float|boolean)",
        Pattern.quote("java.util.Arrays$ArrayList"),
        Pattern.quote("java.lang.Number"),
        Pattern.quote("java.lang.Long")));

    byte[] bytes = SerializationUtility
        .createObjectSerializer()
        .serialize(Arrays.asList(Long.valueOf(123), Integer.valueOf(123)));
    SerializationUtility
        .createObjectSerializer()
        .deserialize(bytes, List.class);
  }

  @Ignore
  @Test
  public void testReport() {
    Predicate<String> whitelist = SerializationUtility.createWhitelistPolicy(
        "\\[.*",
        "(byte|char|short|int|long|double|float|boolean)",
        "java\\..*",
        "org\\.eclipse\\.scout\\..*",
        "org\\.eclipsescout\\..*");
    List<String> classpath = SerializationTestUtility.collectAllSerializableClasses();
    String report = SerializationTestUtility.createVulnerabilityReport(classpath, whitelist);
    System.out.println(report);
  }
}
