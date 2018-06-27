/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_PREFIX;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_UNIQUE_PART_SIZE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.server.secure.SecureServerExposer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link KubernetesServerExposer}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesServerExposerTest {

  @Mock private ExternalServerExposerStrategy<KubernetesEnvironment> externalServerExposerStrategy;
  // TODO Add checking of invocations
  @Mock private SecureServerExposer<KubernetesEnvironment> secureServerExposer;

  private static final Map<String, String> ATTRIBUTES_MAP = singletonMap("key", "value");
  private static final Map<String, String> INTERNAL_SERVER_ATTRIBUTE_MAP =
      singletonMap(ServerConfig.INTERNAL_SERVER_ATTRIBUTE, Boolean.TRUE.toString());

  private static final Pattern SERVER_PREFIX_REGEX =
      Pattern.compile('^' + SERVER_PREFIX + "[A-z0-9]{" + SERVER_UNIQUE_PART_SIZE + "}-pod-main$");
  private static final String MACHINE_NAME = "pod/main";

  private KubernetesServerExposer<KubernetesEnvironment> serverExposer;
  private KubernetesEnvironment kubernetesEnvironment;
  private Container container;

  @BeforeMethod
  public void setUp() throws Exception {
    container = new ContainerBuilder().withName("main").build();
    Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName("pod")
            .endMetadata()
            .withNewSpec()
            .withContainers(container)
            .endSpec()
            .build();

    kubernetesEnvironment =
        KubernetesEnvironment.builder().setPods(ImmutableMap.of("pod", pod)).build();
    this.serverExposer =
        new KubernetesServerExposer<>(
            externalServerExposerStrategy,
            secureServerExposer,
            MACHINE_NAME,
            pod,
            container,
            kubernetesEnvironment);
  }

  @Test
  public void shouldExposeContainerPortAndCreateServiceForServer() {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", ATTRIBUTES_MAP);
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of("http-server", httpServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "http-server",
        "tcp",
        8080,
        new ServerConfigImpl(httpServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  @Test
  public void shouldExposeContainerPortAndCreateServiceAndForServerWhenTwoServersHasTheSamePort() {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", ATTRIBUTES_MAP);
    ServerConfigImpl wsServerConfig =
        new ServerConfigImpl("8080/tcp", "ws", "/connect", ATTRIBUTES_MAP);
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of(
            "http-server", httpServerConfig,
            "ws-server", wsServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertEquals(kubernetesEnvironment.getServices().size(), 1);

    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "http-server",
        "tcp",
        8080,
        new ServerConfigImpl(httpServerConfig).withAttributes(ATTRIBUTES_MAP));
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "ws-server",
        "tcp",
        8080,
        new ServerConfigImpl(wsServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  @Test
  public void shouldExposeContainerPortsAndCreateServiceForServerWhenTwoServersHasDifferentPorts() {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", ATTRIBUTES_MAP);
    ServerConfigImpl wsServerConfig =
        new ServerConfigImpl("8081/tcp", "ws", "/connect", ATTRIBUTES_MAP);
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of(
            "http-server", httpServerConfig,
            "ws-server", wsServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertEquals(kubernetesEnvironment.getServices().size(), 1);

    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "http-server",
        "tcp",
        8080,
        new ServerConfigImpl(httpServerConfig).withAttributes(ATTRIBUTES_MAP));
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "ws-server",
        "tcp",
        8081,
        new ServerConfigImpl(wsServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  @Test
  public void
      shouldExposeTcpContainerPortsAndCreateServiceAndForServerWhenProtocolIsMissedInPort() {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080", "http", "/api", ATTRIBUTES_MAP);
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of("http-server", httpServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertEquals(kubernetesEnvironment.getServices().size(), 1);

    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "http-server",
        "TCP",
        8080,
        new ServerConfigImpl(httpServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  @Test
  public void shouldNotAddAdditionalContainerPortWhenItIsAlreadyExposed() {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", ATTRIBUTES_MAP);
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of("http-server", httpServerConfig);
    container.setPorts(
        singletonList(
            new ContainerPortBuilder()
                .withName("port-8080")
                .withContainerPort(8080)
                .withProtocol("TCP")
                .build()));

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "http-server",
        "tcp",
        8080,
        new ServerConfigImpl(httpServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  @Test
  public void shouldAddAdditionalContainerPortWhenThereIsTheSameButWithDifferentProtocol() {
    // given
    ServerConfigImpl udpServerConfig =
        new ServerConfigImpl("8080/udp", "udp", "/api", ATTRIBUTES_MAP);
    Map<String, ServerConfigImpl> serversToExpose = ImmutableMap.of("server", udpServerConfig);
    container.setPorts(
        new ArrayList<>(
            singletonList(
                new ContainerPortBuilder()
                    .withName("port-8080")
                    .withContainerPort(8080)
                    .withProtocol("TCP")
                    .build())));

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertEquals(container.getPorts().size(), 2);
    assertEquals(container.getPorts().get(1).getContainerPort(), new Integer(8080));
    assertEquals(container.getPorts().get(1).getProtocol(), "UDP");
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "server",
        "udp",
        8080,
        new ServerConfigImpl(udpServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  @Test
  public void shouldExposeContainerPortAndCreateServiceForInternalServer() throws Exception {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", INTERNAL_SERVER_ATTRIBUTE_MAP);
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of("http-server", httpServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertThatInternalServerIsExposed(
        MACHINE_NAME,
        "http-server",
        "tcp",
        8080,
        new ServerConfigImpl(httpServerConfig).withAttributes(INTERNAL_SERVER_ATTRIBUTE_MAP));
  }

  @Test
  public void shouldExposeInternalAndExternalServers() throws Exception {
    // given
    ServerConfigImpl internalServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", INTERNAL_SERVER_ATTRIBUTE_MAP);
    ServerConfigImpl externalServerConfig =
        new ServerConfigImpl("9090/tcp", "http", "/api", ATTRIBUTES_MAP);
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of("int-server", internalServerConfig, "ext-server", externalServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertThatInternalServerIsExposed(
        MACHINE_NAME,
        "int-server",
        "tcp",
        8080,
        new ServerConfigImpl(internalServerConfig).withAttributes(INTERNAL_SERVER_ATTRIBUTE_MAP));
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "ext-server",
        "tcp",
        9090,
        new ServerConfigImpl(externalServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  private void assertThatExternalServerIsExposed(
      String machineName,
      String serverNameRegex,
      String portProtocol,
      Integer port,
      ServerConfigImpl expected) {
    // then
    assertTrue(
        container
            .getPorts()
            .stream()
            .anyMatch(
                p ->
                    p.getContainerPort().equals(port)
                        && p.getProtocol().equals(portProtocol.toUpperCase())));
    // ensure that service is created

    Service service = null;
    for (Entry<String, Service> entry : kubernetesEnvironment.getServices().entrySet()) {
      if (SERVER_PREFIX_REGEX.matcher(entry.getKey()).matches()) {
        service = entry.getValue();
        break;
      }
    }
    assertNotNull(service);

    // ensure that required service port is exposed
    Optional<ServicePort> servicePortOpt =
        service
            .getSpec()
            .getPorts()
            .stream()
            .filter(p -> p.getTargetPort().getIntVal().equals(port))
            .findAny();
    assertTrue(servicePortOpt.isPresent());
    ServicePort servicePort = servicePortOpt.get();
    assertEquals(servicePort.getTargetPort().getIntVal(), port);
    assertEquals(servicePort.getPort(), port);
    assertEquals(servicePort.getName(), SERVER_PREFIX + "-" + port);

    Annotations.Deserializer serviceAnnotations =
        Annotations.newDeserializer(service.getMetadata().getAnnotations());
    assertEquals(serviceAnnotations.machineName(), machineName);
  }

  private void assertThatInternalServerIsExposed(
      String machineName,
      String serverNameRegex,
      String portProtocol,
      Integer port,
      ServerConfigImpl expected) {
    // then
    assertTrue(
        container
            .getPorts()
            .stream()
            .anyMatch(
                p ->
                    p.getContainerPort().equals(port)
                        && p.getProtocol().equals(portProtocol.toUpperCase())));
    // ensure that service is created

    Service service = null;
    for (Entry<String, Service> entry : kubernetesEnvironment.getServices().entrySet()) {
      if (SERVER_PREFIX_REGEX.matcher(entry.getKey()).matches()) {
        service = entry.getValue();
        break;
      }
    }
    assertNotNull(service);

    // ensure that required service port is exposed
    Optional<ServicePort> servicePortOpt =
        service
            .getSpec()
            .getPorts()
            .stream()
            .filter(p -> p.getTargetPort().getIntVal().equals(port))
            .findAny();
    assertTrue(servicePortOpt.isPresent());
    ServicePort servicePort = servicePortOpt.get();
    assertEquals(servicePort.getTargetPort().getIntVal(), port);
    assertEquals(servicePort.getPort(), port);
    assertEquals(servicePort.getName(), SERVER_PREFIX + "-" + port);

    Annotations.Deserializer serviceAnnotations =
        Annotations.newDeserializer(service.getMetadata().getAnnotations());
    assertEquals(serviceAnnotations.machineName(), machineName);

    Map<String, ServerConfigImpl> servers = serviceAnnotations.servers();
    ServerConfig serverConfig = servers.get(serverNameRegex);
    assertEquals(serverConfig, expected);
  }
}
