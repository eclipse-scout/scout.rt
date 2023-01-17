/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.chain.callable;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.platform.chain.callable.CallableChain.Chain;
import org.junit.Test;

public class CallableChainTest {

  @Test
  public void testEmptyChain() throws Exception {
    final List<String> protocol = new ArrayList<>();

    // run the test
    String result = new CallableChain<String>().call(() -> {
      protocol.add("command:call");
      return "command-result";
    });

    // assert
    assertEquals("command-result", result);
    assertEquals(Arrays.asList("command:call"), protocol);
  }

  @Test
  public void testDecoratorChain() throws Exception {
    final List<String> protocol = new ArrayList<>();

    String result = new CallableChain<String>()
        .add(() -> {
          protocol.add("decorator1:onBefore");
          return () -> protocol.add("decorator1:onAfter");
        })
        .add(() -> {
          protocol.add("decorator2:onBefore");
          return null;
        })
        .add(() -> {
          protocol.add("decorator3:onBefore");
          return () -> protocol.add("decorator3:onAfter");
        }).call(() -> {
          protocol.add("command:call");
          return "command-result";
        });

    // assert
    assertEquals("command-result", result);
    assertEquals(Arrays.asList(
        "decorator1:onBefore",
        "decorator2:onBefore",
        "decorator3:onBefore",
        "command:call",
        "decorator3:onAfter",
        "decorator1:onAfter"), protocol);
  }

  @Test
  public void testDecoratorChainWithException() {
    final List<String> protocol = new ArrayList<>();

    final Exception exception = new Exception("expected JUnit test exception");
    try {
      new CallableChain<String>()
          .add(() -> {
            protocol.add("decorator1:onBefore");
            return () -> {
              protocol.add("decorator1:onAfter");
              throw new RuntimeException("expected JUnit test exception");
            };
          }).add(() -> {
            protocol.add("decorator2:onBefore");
            throw exception;
          }).add(() -> {
            protocol.add("decorator3:onBefore");
            return () -> protocol.add("decorator3:onAfter");
          }).call(() -> {
            protocol.add("command:call");
            return "command-result";
          });
      fail();
    }
    catch (Exception e) {
      assertSame(exception, e);
    }

    // assert
    assertEquals(Arrays.asList(
        "decorator1:onBefore",
        "decorator2:onBefore",
        "decorator1:onAfter"), protocol);
  }

  @Test
  public void testInterceptorChain() throws Exception {
    final List<String> protocol = new ArrayList<>();

    String result = new CallableChain<String>()
        .add(new ICallableInterceptor<String>() {

          @Override
          public String intercept(Chain<String> chain) throws Exception {
            protocol.add("interceptor1:before");
            try {
              return chain.continueChain();
            }
            finally {
              protocol.add("interceptor1:after");
            }
          }

          @Override
          public boolean isEnabled() {
            return true;
          }
        }).add(new ICallableInterceptor<String>() {

          @Override
          public String intercept(Chain<String> chain) throws Exception {
            protocol.add("interceptor2:before");
            try {
              return chain.continueChain();
            }
            finally {
              protocol.add("interceptor2:after");
            }
          }

          @Override
          public boolean isEnabled() {
            return true;
          }
        }).add(new ICallableInterceptor<String>() {

          @Override
          public String intercept(Chain<String> chain) throws Exception {
            protocol.add("interceptor3:before");
            try {
              return chain.continueChain();
            }
            finally {
              protocol.add("interceptor3:after");
            }
          }

          @Override
          public boolean isEnabled() {
            return true;
          }
        }).add(new ICallableInterceptor<String>() {

          @Override
          public String intercept(Chain<String> chain) throws Exception {
            protocol.add("interceptor4:before");
            try {
              return chain.continueChain();
            }
            finally {
              protocol.add("interceptor4:after");
            }
          }

          @Override
          public boolean isEnabled() {
            return false;
          }
        }).call(() -> {
          protocol.add("command:call");
          return "command-result";
        });

    // assert
    assertEquals("command-result", result);
    assertEquals(Arrays.asList(
        "interceptor1:before",
        "interceptor2:before",
        "interceptor3:before",
        "command:call",
        "interceptor3:after",
        "interceptor2:after",
        "interceptor1:after"), protocol);
  }

  @Test
  public void testInterceptorChainWithException() {
    final List<String> protocol = new ArrayList<>();
    final Exception exception = new Exception("expected JUnit test exception");

    try {
      new CallableChain<String>()
          .add(new ICallableInterceptor<String>() {

            @Override
            public String intercept(Chain<String> chain) throws Exception {
              protocol.add("interceptor1:before");
              try {
                return chain.continueChain();
              }
              finally {
                protocol.add("interceptor1:after");
              }
            }

            @Override
            public boolean isEnabled() {
              return true;
            }
          }).add(new ICallableInterceptor<String>() {

            @Override
            public String intercept(Chain<String> chain) throws Exception {
              protocol.add("interceptor2:before");
              throw exception;
            }

            @Override
            public boolean isEnabled() {
              return true;
            }
          }).add(new ICallableInterceptor<String>() {

            @Override
            public String intercept(Chain<String> chain) throws Exception {
              protocol.add("interceptor3:before");
              try {
                return chain.continueChain();
              }
              finally {
                protocol.add("interceptor3:after");
              }
            }

            @Override
            public boolean isEnabled() {
              return true;
            }
          }).call(() -> {
            protocol.add("command:call");
            return "command-result";
          });
      fail();
    }
    catch (Exception e) {
      assertSame(exception, e);
    }

    // assert
    assertEquals(Arrays.asList(
        "interceptor1:before",
        "interceptor2:before",
        "interceptor1:after"), protocol);
  }

  @Test
  public void testInterceptorChainWithPreemtiveResult() throws Exception {
    final List<String> protocol = new ArrayList<>();

    String result = new CallableChain<String>()
        .add(new ICallableInterceptor<String>() {

          @Override
          public String intercept(Chain<String> chain) throws Exception {
            protocol.add("interceptor1:before");
            try {
              return chain.continueChain();
            }
            finally {
              protocol.add("interceptor1:after");
            }
          }

          @Override
          public boolean isEnabled() {
            return true;
          }
        }).add(new ICallableInterceptor<String>() {

          @Override
          public String intercept(Chain<String> chain) {
            protocol.add("interceptor2:before");
            return "interceptor2-result";
          }

          @Override
          public boolean isEnabled() {
            return true;
          }
        }).add(new ICallableInterceptor<String>() {

          @Override
          public String intercept(Chain<String> chain) throws Exception {
            protocol.add("interceptor3:before");
            try {
              return chain.continueChain();
            }
            finally {
              protocol.add("interceptor3:after");
            }
          }

          @Override
          public boolean isEnabled() {
            return true;
          }
        }).call(() -> {
          protocol.add("command:call");
          return "command-result";
        });

    // assert
    assertEquals("interceptor2-result", result);
    assertEquals(Arrays.asList(
        "interceptor1:before",
        "interceptor2:before",
        "interceptor1:after"), protocol);
  }

  @Test
  public void testInterceptorChainWithNoContinue() throws Exception {
    final List<String> protocol = new ArrayList<>();

    String result = new CallableChain<String>()
        .add(new ICallableInterceptor<String>() {

          @Override
          public String intercept(Chain<String> chain) throws Exception {
            protocol.add("interceptor1:before");
            try {
              return chain.continueChain();
            }
            finally {
              protocol.add("interceptor1:after");
            }
          }

          @Override
          public boolean isEnabled() {
            return true;
          }
        }).add(new ICallableInterceptor<String>() {

          @Override
          public String intercept(Chain<String> chain) {
            protocol.add("interceptor2:before");
            try {
              return "interceptor2-result"; // do not continue chain
            }
            finally {
              protocol.add("interceptor2:after");
            }
          }

          @Override
          public boolean isEnabled() {
            return true;
          }
        }).add(new ICallableInterceptor<String>() {

          @Override
          public String intercept(Chain<String> chain) throws Exception {
            protocol.add("interceptor3:before");
            try {
              return chain.continueChain();
            }
            finally {
              protocol.add("interceptor3:after");
            }
          }

          @Override
          public boolean isEnabled() {
            return true;
          }
        }).call(() -> {
          protocol.add("command:call");
          return "command-result";
        });

    // assert
    assertEquals("interceptor2-result", result);
    assertEquals(Arrays.asList(
        "interceptor1:before",
        "interceptor2:before",
        "interceptor2:after",
        "interceptor1:after"), protocol);
  }

  @Test
  public void testMixed() throws Exception {
    final List<String> protocol = new ArrayList<>();

    String result = new CallableChain<String>()
        .add(() -> {
          protocol.add("decorator1:onBefore");
          return () -> protocol.add("decorator1:onAfter");
        }).add(() -> {
          protocol.add("decorator2:onBefore");
          return () -> protocol.add("decorator2:onAfter");
        }).add(new ICallableInterceptor<String>() {

          @Override
          public String intercept(Chain<String> chain) throws Exception {
            protocol.add("interceptor1:before");
            try {
              return chain.continueChain();
            }
            finally {
              protocol.add("interceptor1:after");
            }
          }

          @Override
          public boolean isEnabled() {
            return true;
          }
        }).add(() -> {
          protocol.add("decorator3:onBefore");
          return () -> protocol.add("decorator3:onAfter");
        }).add(() -> {
          protocol.add("decorator4:onBefore");
          return () -> protocol.add("decorator4:onAfter");
        }).add(new ICallableInterceptor<String>() {

          @Override
          public String intercept(Chain<String> chain) throws Exception {
            protocol.add("interceptor2:before");
            try {
              return chain.continueChain();
            }
            finally {
              protocol.add("interceptor2:after");
            }
          }

          @Override
          public boolean isEnabled() {
            return true;
          }
        }).add(() -> {
          protocol.add("decorator5:onBefore");
          return () -> protocol.add("decorator5:onAfter");
        }).call(() -> {
          protocol.add("command:call");
          return "command-result";
        });

    // assert
    assertEquals("command-result", result);
    assertEquals(Arrays.asList(
        "decorator1:onBefore",
        "decorator2:onBefore",
        "interceptor1:before",
        "decorator3:onBefore",
        "decorator4:onBefore",
        "interceptor2:before",
        "decorator5:onBefore",
        "command:call",
        "decorator5:onAfter",
        "interceptor2:after",
        "decorator4:onAfter",
        "decorator3:onAfter",
        "interceptor1:after",
        "decorator2:onAfter",
        "decorator1:onAfter"), protocol);
  }

  @Test
  public void testMixedWithException() {
    final Exception exception = new Exception("expected JUnit test exception");
    final List<String> protocol = new ArrayList<>();

    try {
      new CallableChain<String>()
          .add(() -> {
            protocol.add("decorator1:onBefore");
            return () -> protocol.add("decorator1:onAfter");
          }).add(() -> {
            protocol.add("decorator2:onBefore");
            return () -> protocol.add("decorator2:onAfter");
          }).add(new ICallableInterceptor<String>() {

            @Override
            public String intercept(Chain<String> chain) throws Exception {
              protocol.add("interceptor1:before");
              try {
                return chain.continueChain();
              }
              finally {
                protocol.add("interceptor1:after");
              }
            }

            @Override
            public boolean isEnabled() {
              return true;
            }
          }).add(() -> {
            protocol.add("decorator3:onBefore");
            return () -> protocol.add("decorator3:onAfter");
          }).add(() -> {
            protocol.add("decorator4:onBefore");
            return () -> protocol.add("decorator4:onAfter");
          }).add(new ICallableInterceptor<String>() {

            @Override
            public String intercept(Chain<String> chain) throws Exception {
              protocol.add("interceptor2:before");
              try {
                return chain.continueChain();
              }
              finally {
                protocol.add("interceptor2:after");
              }
            }

            @Override
            public boolean isEnabled() {
              return true;
            }
          }).add(() -> {
            protocol.add("decorator5:onBefore");
            return () -> protocol.add("decorator5:onAfter");
          }).call(() -> {
            protocol.add("command:call");
            throw exception;
          });
      fail();
    }
    catch (Exception e) {
      assertSame(exception, e);
    }

    // assert
    assertEquals(Arrays.asList(
        "decorator1:onBefore",
        "decorator2:onBefore",
        "interceptor1:before",
        "decorator3:onBefore",
        "decorator4:onBefore",
        "interceptor2:before",
        "decorator5:onBefore",
        "command:call",
        "decorator5:onAfter",
        "interceptor2:after",
        "decorator4:onAfter",
        "decorator3:onAfter",
        "interceptor1:after",
        "decorator2:onAfter",
        "decorator1:onAfter"), protocol);
  }
}
