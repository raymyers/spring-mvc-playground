package com.cadrlife.mvc.coffee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;

import com.cadrlife.mvc.coffee.jcoffeescript.JCoffeeScriptCompileException;
import com.cadrlife.mvc.coffee.jcoffeescript.JCoffeeScriptCompiler;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;

/**
 * Filter to compile coffeescript on the fly, with concatenation support. Does
 * do caching yet.
 * 
 * This filter takes 3 parameters:
 * 
 * coffeeFiles. Required. Ant-style path to all coffee files
 * ex. /WEB-INF/js/*.coffee
 * 
 * concatenateRoot. Optional. Path to the root file to resolve dependencies and concatenate results
 * ex. /WEB-INF/js/main.coffee
 * 
 * concatenateName. Optional. Path that maps to the concatenated source code.
 * ex. /js/app.js
 */
public class CoffeeFilter implements Filter {
	private String concatenateRoot = "";
	private String concatenateName = "";
	private String coffeeFiles = "";
	// Filter will diable itself if not configured to do anything useful.
	private boolean enabled = true;
	private boolean concatenationEnabled;

	private static final class CompiledCoffee {
		// public final Date dateCached; // Time this was put in the cache
		public final String output; // Compiled coffee

		public CompiledCoffee(Date date, String output) {
			// this.dateCached = date;
			this.output = output;
		}
	}

	// Regex to get the line number of the failure.
	private static final Pattern LINE_NUMBER = Pattern.compile("line ([0-9]+)");
	private static final ThreadLocal<JCoffeeScriptCompiler> compiler = new ThreadLocal<JCoffeeScriptCompiler>() {
		@Override
		protected JCoffeeScriptCompiler initialValue() {
			return new JCoffeeScriptCompiler();
		}
	};
	private Map<String, CompiledCoffee> cache; // Map of Relative Path ->
												// Compiled coffee
												// private Date lastCacheUpdate
												// = new Date(0l);
	private FilterConfig filterConfig;

	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		cache = new HashMap<String, CompiledCoffee>();
		coffeeFiles = filterConfig.getInitParameter("coffeeFiles");
		concatenateRoot = filterConfig.getInitParameter("concatenateRoot");
		concatenateName = filterConfig.getInitParameter("concatenateName");
		if (Strings.isNullOrEmpty(coffeeFiles)) {
			this.enabled = false;
			// TODO: Log this
		}
		concatenationEnabled = !(Strings.isNullOrEmpty(concatenateName) || Strings
				.isNullOrEmpty(concatenateRoot));
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (!enabled) {
			chain.doFilter(request, response);
			return;
		}
		HttpServletRequest httpReq = (HttpServletRequest) request;
		String requestURI = httpReq.getRequestURI();
		ServletContext servletContext = filterConfig.getServletContext();
		String contextPath = servletContext.getContextPath();
		if (requestURI.startsWith(contextPath)) {
			requestURI = requestURI.substring(contextPath.length());
		}
		String coffeeRequestURI = requestURI;
		if (requestURI.endsWith(".js")) {
			coffeeRequestURI = requestURI.substring(0, requestURI.length() - 3)
					+ ".coffee";
		}
		InputStream stream = servletContext.getResourceAsStream("/WEB-INF"
				+ coffeeRequestURI);
		if (concatenationEnabled && requestURI.equals(concatenateName)) {
			response.setContentType("text/javascript");
			String coffee = concatenateResourcesAsString(
					rootCoffee(servletContext), allAppCoffee(servletContext));
			String compiledCoffee = compileCoffeescript(coffeeRequestURI, coffee);
			response.getOutputStream().print(compiledCoffee);
			cache.put(requestURI,
					new CompiledCoffee(new Date(), compiledCoffee));
			return;
		}
		if (stream == null) {
			chain.doFilter(request, response);
			return;
		}
		response.setContentType("text/javascript");
		// Check the cache.
		CompiledCoffee cc = cache.get(requestURI);
		if (cc != null
		// && cc.sourceLastModified.equals(file.lastModified())
		) {
			response.getOutputStream().print(cc.output);
			return;
		}
		// Compile the coffee and return.
		String coffee = CharStreams.toString(new InputStreamReader(stream));
		String compiledCoffee = compileCoffeescript(coffeeRequestURI, coffee);
		response.getOutputStream().print(compiledCoffee);
		cache.put(requestURI, new CompiledCoffee(new Date(), compiledCoffee));
		// Render a nice error page?
	}

	private String compileCoffeescript(String requestURI, String coffee) {
		try {
			return getCompiler().compile(coffee);
		} catch (JCoffeeScriptCompileException e) {
			e.printStackTrace();
			throw new CompilationException(requestURI, coffee,
					e.getMessage(), getLineNumber(e), -1, -1);
		}
	}

	private Iterable<Resource> rootCoffee(ServletContext servletContext)
			throws IOException {
		ServletContextResourcePatternResolver resolver = new ServletContextResourcePatternResolver(
				servletContext);

		return Arrays.asList(resolver.getResources(concatenateRoot));
	}

	private Iterable<Resource> allAppCoffee(ServletContext servletContext)
			throws IOException {
		ServletContextResourcePatternResolver resolver = new ServletContextResourcePatternResolver(
				servletContext);
		Resource[] resources = resolver.getResources(coffeeFiles);
		return Arrays.asList(resources);
	}

	private String concatenateResourcesAsString(
			Iterable<Resource> rootResources,
			Iterable<Resource> includeResources) throws IOException {
		Iterable<File> rootFiles = resourcesToFiles(rootResources);
		Iterable<File> includeFiles = resourcesToFiles(includeResources);
		return concatenateFilesAsString(rootFiles, includeFiles);
	}

	private String concatenateFilesAsString(Iterable<File> rootFiles,
			Iterable<File> includeFiles) throws IOException {
		return new CoffeescriptConcat().concatenate(rootFiles, includeFiles);
	}

	private Iterable<File> resourcesToFiles(Iterable<Resource> rootResources) {
		Function<Resource, File> resourceToFile = new Function<Resource, File>() {

			public File apply(Resource r) {
				try {
					return r.getFile();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

		};
		Iterable<File> rootFiles = Iterables.transform(rootResources,
				resourceToFile);
		return rootFiles;
	}

	public void destroy() {

	}

	/**
	 * @return the line number that the exception happened on, or 0 if not found
	 *         in the message.
	 */
	public static int getLineNumber(JCoffeeScriptCompileException e) {
		Matcher m = LINE_NUMBER.matcher(e.getMessage());
		if (m.find()) {
			return Integer.parseInt(m.group(1));
		}
		return 0;
	}

	public static JCoffeeScriptCompiler getCompiler() {
		return compiler.get();
	}

}