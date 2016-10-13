/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.ariesblueprint.webconsole;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.everit.expression.ExpressionCompiler;
import org.everit.expression.ParserConfiguration;
import org.everit.expression.jexl.JexlExpressionCompiler;
import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateCompiler;
import org.everit.templating.html.HTMLTemplateCompiler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.container.BlueprintEvent;
import org.osgi.service.blueprint.container.BlueprintListener;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * The Webconsole plugin that shows the blueprint containers and their components.
 */
public class AriesBlueprintWebconsolePlugin implements Servlet {

  /**
   * Tracks Aries {@link BlueprintContainer} OSGi services and adds them to the plugin internal map.
   */
  private class CustomBlueprintContainerServiceTrackerCustomizer
      implements ServiceTrackerCustomizer<BlueprintContainer, BlueprintContainer> {

    @Override
    public BlueprintContainer addingService(final ServiceReference<BlueprintContainer> reference) {
      BlueprintContainer blueprintContainer = bundleContext.getService(reference);
      blueprintContainerByBundleIdMap.put(reference.getBundle().getBundleId(), blueprintContainer);
      return blueprintContainer;
    }

    @Override
    public void modifiedService(final ServiceReference<BlueprintContainer> reference,
        final BlueprintContainer service) {
    }

    @Override
    public void removedService(final ServiceReference<BlueprintContainer> reference,
        final BlueprintContainer service) {
      blueprintContainerByBundleIdMap.remove(reference.getBundle().getBundleId());
      bundleContext.ungetService(reference);
    }

  }

  /**
   * Tracks Blueprint events and adds (or removes) them to the plugin internal Map.
   */
  private class CustomBlueprintListener implements BlueprintListener {

    @Override
    public void blueprintEvent(final BlueprintEvent event) {
      long bundleId = event.getBundle().getBundleId();
      if (event.getType() == BlueprintEvent.DESTROYED) {
        blueprintContainerUIByBundleIdMap.remove(bundleId);
        blueprintContainerByBundleIdMap.remove(bundleId);
        return;
      }

      blueprintContainerUIByBundleIdMap.put(bundleId, new BlueprintContainerUIData(event,
          blueprintContainerByBundleIdMap.get(bundleId)));
    }
  }

  private static final CompiledTemplate PAGE_TEMPLATE;

  static {
    ExpressionCompiler jexlExpressionCompiler = new JexlExpressionCompiler();
    TemplateCompiler templateCompiler = new HTMLTemplateCompiler(jexlExpressionCompiler);
    ClassLoader classLoader = AriesBlueprintWebconsolePlugin.class.getClassLoader();
    PAGE_TEMPLATE = templateCompiler.compile(
        AriesBlueprintWebconsolePlugin.readResource("META-INF/webcontent/blueprintcontainers.html",
            StandardCharsets.UTF_8,
            classLoader),
        new ParserConfiguration(classLoader));
  }

  private static String readResource(final String resourcePath, final Charset charset,
      final ClassLoader classLoader) {
    StringBuilder sb = new StringBuilder();
    try (
        Reader in = new InputStreamReader(classLoader.getResourceAsStream(resourcePath), charset)) {
      final int bufferSize = 1024;
      char[] buffer = new char[bufferSize];
      int r = in.read(buffer);
      while (r >= 0) {
        sb.append(buffer, 0, r);
        r = in.read(buffer);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return sb.toString();
  }

  private final Map<Long, BlueprintContainer> blueprintContainerByBundleIdMap =
      new ConcurrentHashMap<>();

  private final Map<Long, BlueprintContainerUIData> blueprintContainerUIByBundleIdMap =
      new ConcurrentHashMap<>();

  private BundleContext bundleContext;

  private ServletConfig config;

  private ServiceRegistration<BlueprintListener> listenerSR;

  private ServiceTracker<BlueprintContainer, BlueprintContainer> serviceTracker;

  public AriesBlueprintWebconsolePlugin(final BundleContext bundleContext) {
    this.bundleContext = bundleContext;

  }

  /**
   * Initializes the blueprint event and blueprint container trackers.
   */
  public void activate() {

    serviceTracker = new ServiceTracker<>(bundleContext, BlueprintContainer.class,
        new CustomBlueprintContainerServiceTrackerCustomizer());
    serviceTracker.open();

    CustomBlueprintListener blueprintListener = new CustomBlueprintListener();
    listenerSR = bundleContext.registerService(BlueprintListener.class, blueprintListener,
        new Hashtable<String, Object>());
  }

  /**
   * Closes the blueprint event and blueprint container trackers.
   */
  public void deactivate() {
    serviceTracker.close();
    listenerSR.unregister();
  }

  @Override
  public void destroy() {
  }

  @Override
  public ServletConfig getServletConfig() {
    return config;
  }

  @Override
  public String getServletInfo() {
    return "ariesblueprint";
  }

  @Override
  public void init(final ServletConfig config) throws ServletException {
    this.config = config;
  }

  @Override
  public void service(final ServletRequest req, final ServletResponse res)
      throws ServletException, IOException {
    PrintWriter writer = res.getWriter();

    Set<BlueprintContainerUIData> uiContainers =
        new TreeSet<>(blueprintContainerUIByBundleIdMap.values());

    Map<String, Object> vars = new HashMap<>();
    vars.put("blueprintContainers", uiContainers);
    PAGE_TEMPLATE.render(writer, vars, "body");
  }
}
