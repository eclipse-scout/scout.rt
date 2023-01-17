/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.scout.rt.mom.jms;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.util.LockFile;
import org.junit.rules.ExternalResource;

public class ArtemisJmsBrokerTestRule extends ExternalResource {

  private LockFile m_lockFile;
  private EmbeddedActiveMQ m_embeddedArtemisServer;

  @Override
  protected void before() throws Throwable {
    m_lockFile = new LockFile(Paths.get(System.getProperty("user.dir"), "target", "artemis-junit.lock").toFile());
    m_lockFile.lock(30, TimeUnit.MINUTES);
    startArtemisJmsServer();
  }

  @Override
  protected void after() {
    try {
      stopArtemisJmsServer();
    }
    finally {
      m_lockFile.unlock();
    }
  }

  protected void startArtemisJmsServer() throws Exception {
    prepareArtemisWorkDirectory();

    EmbeddedActiveMQ embeddedArtemisServer = new EmbeddedActiveMQ();
    embeddedArtemisServer.setConfiguration(createArtemisServerConfig());
    embeddedArtemisServer.start();
    m_embeddedArtemisServer = embeddedArtemisServer;
  }

  protected void prepareArtemisWorkDirectory() throws IOException {
    // base directory of artemis instance
    Path artemisPath = Paths.get(System.getProperty("user.dir"), "target", "artemis");

    // delete directory
    if (artemisPath.toFile().exists()) {
      Files.walkFileTree(artemisPath, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }
      });
    }

    System.setProperty("artemis.instance", artemisPath.toString());
  }

  protected Configuration createArtemisServerConfig() {
    Configuration config = new ConfigurationImpl();
    config.setSecurityEnabled(false);
    config.addAcceptorConfiguration(new TransportConfiguration(InVMAcceptorFactory.class.getName()));
    config.addAddressSetting("#", new AddressSettings()
        .setExpiryAddress(new SimpleString("jms.queue.ExpiryQueue"))
        .setDeadLetterAddress(new SimpleString("jms.queue.DLQ")));

    config.addQueueConfiguration(createQueue("jms.topic.scout.mom.requestreply.cancellation"));

    config.addQueueConfiguration(createQueue("jms.topic.test/mom/testTopicPublishFirst"));
    config.addQueueConfiguration(createQueue("jms.topic.test/mom/testProperties"));
    config.addQueueConfiguration(createQueue("jms.topic.test/mom/testTopicRequestReplyTimeout"));
    config.addQueueConfiguration(createQueue("jms.topic.test/mom/testTopicRequestReplyRequestFirst"));
    config.addQueueConfiguration(createQueue("jms.topic.test/mom/testTopicRequestReplyMultipleSubscriptions"));
    config.addQueueConfiguration(createQueue("jms.topic.test/mom/testTopicRequestReplyCorrelationId"));
    config.addQueueConfiguration(createQueue("jms.topic.test/mom/testTopicPublishSubscribeMultipleSubscriptions"));
    config.addQueueConfiguration(createQueue("jms.topic.test/mom/testQueueRequestReplyJsonObjectMarshaller"));
    config.addQueueConfiguration(createQueue("jms.topic.test/mom/testTopicRequestReplyCancellation"));
    config.addQueueConfiguration(createQueue("jms.topic.test/mom/testTopicRequestReply"));
    config.addQueueConfiguration(createQueue("jms.topic.test/mom/testTopicPublishSubscribeCorrelationId"));
    config.addQueueConfiguration(createQueue("jms.topic.test/mom/testMessageSelector"));
    config.addQueueConfiguration(createQueue("jms.topic.test/mom/testQueueRequestReplyCancellation"));
    config.addQueueConfiguration(createQueue("jms.topic.differentTopic"));
    config.addQueueConfiguration(createQueue("jms.topic.scout.physical.UnitTestTopic"));

    config.addQueueConfiguration(createQueue("Durable-Test-Subscription").setAddress("jms.topic.test/mom/testTopicPublishSubscribe"));

    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testPublishBytes"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testPublishText"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testPublishObject"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testConcurrentMessageConsumption"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testPublishAndConsumeInternal"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testSubscribeTransactional"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testSubscribeFailover"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testQueuePublishFirst"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testPublishJsonData"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testPublishJsonDataSecure"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testQueueRequestReplyRequestFirst"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testPublishTransactional"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testPublishStringData"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testPublishObjectData"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testCurrentMessagePubSub"));
    config.addQueueConfiguration(createQueue("jms.queue.test/mom/testSerialMessageConsumption"));
    return config;
  }

  protected QueueConfiguration createQueue(String name) {
    return new QueueConfiguration(name).setAddress(name);
  }

  protected void stopArtemisJmsServer() {
    try {
      m_embeddedArtemisServer.stop();
    }
    catch (Exception e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }
}
