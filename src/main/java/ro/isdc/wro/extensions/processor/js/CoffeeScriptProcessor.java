/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.coffeescript.CoffeeScript;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;



/**
 * Uses coffee script {@link http://jashkenas.github.com/coffee-script/} to compile to javascript code.
 *
 * @author Alex Objelean
 * @created 26 Mar 2011
 * @since 1.3.6
 */
@SupportedResourceType(ResourceType.JS)
public class CoffeeScriptProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CoffeeScriptProcessor.class);
  public static final String ALIAS = "coffeeScript";
  /**
   * Engine.
   */
  private CoffeeScript engine;

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      writer.write(getEngine().compile(content));
    } catch (final WroRuntimeException e) {
      onException(e);
      writer.write(content);
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
    } finally {
      reader.close();
      writer.close();
    }
  }

  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final WroRuntimeException e) {
  }


  /**
   * @return PackerJs engine.
   */
  private CoffeeScript getEngine() {
    if (engine == null) {
      engine = newCoffeeScript();
    }
    return engine;
  }

  /**
   * @return the {@link CoffeeScript} engine implementation. Override it to provide a different version of the coffeeScript.js
   *         library. Useful for upgrading the processor outside the wro4j release.
   */
  protected CoffeeScript newCoffeeScript() {
    return new CoffeeScript();
  }


  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }
}
