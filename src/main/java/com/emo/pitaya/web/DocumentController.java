package com.emo.pitaya.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.emo.mango.config.MangoConfig;
import com.emo.pitaya.pdf.PdfConfig;
import com.emo.pitaya.pdf.PdfGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

@Controller
@RequestMapping("/document")
public class DocumentController {

	private final PdfConfig config;

	private final VelocityEngine ve;

	@Inject
	public DocumentController(final MangoConfig config) {
		this.ve = new VelocityEngine();
		this.config = new PdfConfig(config.config());

		final Properties props = new Properties();

		props.setProperty("resource.loader", "file");
		props.setProperty("file.resource.loader.description",
				"Velocity File Resource Loader");
		props.setProperty("file.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.FileResourceLoader");
		props.setProperty("file.resource.loader.path", this.config.sandbox.base.getPath());
		props.setProperty("file.resource.loader.cache", "false");
		props.setProperty("file.resource.loader.modificationCheckInterval", "0");

		ve.init(props);
	}


	private void uncheckedServe(final File file,
			final HttpServletResponse response) throws IllegalAccessException,
			IOException {
		final String contentType = Files.probeContentType(file.toPath());
		response.setContentType(contentType);

		final InputStream is = Files.newInputStream(file.toPath());
		if (is == null) {
			throw new IllegalAccessException("no such file "
					+ file.getCanonicalPath());
		}

		ByteStreams.copy(is, response.getOutputStream());

		is.close();

		response.flushBuffer();
	}

	private void serve(final String docId, final String elementRelativePath,
			final HttpServletResponse response) throws IllegalAccessException,
			IOException {
		serve(docId, elementRelativePath, null, response);
	}

	private void serve(final String docId, final String elementRelativePath,
			final VelocityContext vctx, final HttpServletResponse response)
			throws IllegalAccessException, IOException {

		final File elementPath = config.sandbox.relativeToDoc(docId, elementRelativePath);

		final String contentType = Files.probeContentType(elementPath.toPath());

		response.setContentType(contentType);

		final InputStream is;

		if (vctx != null) {
			final File docRelativePath = new File(docId, elementRelativePath);

			final Template tpl = ve.getTemplate(docRelativePath.getPath());

			final StringWriter writer = new StringWriter();
			tpl.merge(vctx, writer);

			is = new ByteArrayInputStream(writer.getBuffer().toString()
					.getBytes()); // TODO: this is rather ugly, find a better
									// way.

		} else {
			is = Files.newInputStream(elementPath.toPath());

			if (is == null) {
				throw new IllegalAccessException("no such file "
						+ elementPath.getCanonicalPath());
			}
		}

		ByteStreams.copy(is, response.getOutputStream());

		is.close();

		response.flushBuffer();
	}

	@RequestMapping(value = "/{id}/**", method = RequestMethod.GET)
	@ResponseBody
	public final void getAny(final @PathVariable("id") String docId,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, IllegalAccessException {

		final String uri = request.getRequestURI();
		final String relPath = uri.substring(uri.indexOf("/" + docId + "/") + 2
				+ docId.length());

		serve(docId, relPath, response);
	}

	@RequestMapping(value = "/{id}/content")
	@ResponseBody
	public final void getContent(final @PathVariable("id") String docId,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, IllegalAccessException {

		final Enumeration<String> paramNames = request.getParameterNames();
		final VelocityContext context;

		final ObjectMapper mapper = new ObjectMapper();

		if (paramNames.hasMoreElements()) {
			context = new VelocityContext();
			while (paramNames.hasMoreElements()) {
				final String paramName = paramNames.nextElement();
				final String value = request.getParameter(paramName);
				final boolean isJson = (value.startsWith("[") || value
						.startsWith("{"))
						&& (value.endsWith("]") || value.endsWith("}"));

				context.put(paramName, (!isJson) ? value : new NodeWrapper(
						mapper.readTree(value)));
			}
		} else {
			context = null;
		}

		serve(docId, "content.html", context, response);
	}

	private Map<String, String> entriesFromRequest(
			final HttpServletRequest request) {
		final Map<String, String> entries = new HashMap<String, String>();

		for (final Entry<String, String[]> entry : request.getParameterMap()
				.entrySet()) {
			entries.put(entry.getKey(), entry.getValue()[0]);
		}

		return entries;
	}

	@RequestMapping(value = "/{id}/pdf/generate")
	@ResponseBody
	public void generatePdf(final @PathVariable("id") String docId,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, IllegalAccessException {

		final File dest = config.temporary.newPdfFile();
		
		final PdfGenerator pdf = new PdfGenerator(config.binary, config.webSourceFor(docId), dest, config.options,
				entriesFromRequest(request));

		pdf.run();
	}

	@RequestMapping(value = "/{id}/pdf/view")
	@ResponseBody
	public void viewPdf(final @PathVariable("id") String docId,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, IllegalAccessException {

		final File dest = config.temporary.newPdfFile();
		
		final PdfGenerator pdf = new PdfGenerator(config.binary, config.webSourceFor(docId), dest, config.options,
				entriesFromRequest(request));

		pdf.run();

		uncheckedServe(dest, response);
	}

}
