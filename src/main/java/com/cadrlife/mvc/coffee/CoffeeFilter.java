package com.cadrlife.mvc.coffee;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

/**
 * This plugin intercepts requests for static files ending in '.coffee', and
 * serves the compiled javascript instead.
 */
public class CoffeeFilter implements Filter {

	private static final class CompiledCoffee {
		public final Date dateCached; // Time this was put in the cache
		public final String output; // Compiled coffee

		public CompiledCoffee(Date date, String output) {
			this.dateCached = date;
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
	private FilterConfig filterConfig;

	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		cache = new HashMap<String, CompiledCoffee>();
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) request;
		String requestURI = httpReq.getRequestURI();
		ServletContext servletContext = filterConfig.getServletContext();
		String contextPath = servletContext.getContextPath();
		if (requestURI.startsWith(contextPath)) {
			requestURI = requestURI.substring(contextPath.length());
		}
		if (requestURI.endsWith(".js")) {
			requestURI = requestURI.substring(0, requestURI.length() - 3)
					+ ".coffee";
		}
		System.out.println("REQ URI" + requestURI);
		InputStream stream = servletContext.getResourceAsStream("/WEB-INF"
				+ requestURI);
		System.out.println(contextPath);

		try {
			if (requestURI.equals("/js/app.coffee")) {
				response.setContentType("text/javascript");
				String coffee = concatenateResourcesAsString(allAppCoffee(servletContext));
				String compiledCoffee = getCompiler().compile(coffee);
				response.getOutputStream().print(compiledCoffee);
				cache.put(requestURI, new CompiledCoffee(new Date(),
						compiledCoffee));
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
			String compiledCoffee = getCompiler().compile(coffee);
			response.getOutputStream().print(compiledCoffee);
			cache.put(requestURI,
					new CompiledCoffee(new Date(), compiledCoffee));
		} catch (JCoffeeScriptCompileException e) {
			e.printStackTrace();
			// Exception ex = new CompilationException(file, e.getMessage(),
			// getLineNumber(e), -1, -1);
		}
		// // Render a nice error page.
		// Template tmpl = TemplateLoader.load("errors/500.html");
		// Map<String, Object> args = new HashMap<String, Object>();
		// Exception ex = new CompilationException(file, e.getMessage(),
		// getLineNumber(e), -1, -1);
		// args.put("exception", ex);
		// play.Logger.error(ex, "Coffee compilation error");
		// response.contentType = "text/html";
		// response.status = 500;
		// response.print(tmpl.render(args));
		// }
		// return true;
	}

	private String concatenateResourcesAsString(Resource[] resources) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (Resource r : resources) {
			try {
				System.out.println(r);
				ByteStreams.copy(r.getInputStream(), out);
				out.write("\n".getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return out.toString();
	}

	private Resource[] allAppCoffee(ServletContext servletContext)
			throws IOException {
		ServletContextResourcePatternResolver resolver = new ServletContextResourcePatternResolver(
				servletContext);
		return resolver.getResources("/WEB-INF/js/app/**.coffee");
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