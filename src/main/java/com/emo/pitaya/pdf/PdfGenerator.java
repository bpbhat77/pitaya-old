package com.emo.pitaya.pdf;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Joiner;


public class PdfGenerator implements Runnable {

	private final String url;
	private final String defaultOptions;
	private final String postOptions;
	private final File dest;
	private final String wkhtml2pdf;
	
	public PdfGenerator(final String wkhtml2pdf, final String url, final File dest) {
		this(wkhtml2pdf, url, dest, "--zoom 1.0 -B 0 -L 0 -R 0 -T 0 -s A4 --no-pdf-compression --disable-smart-shrinking", new HashMap<String, String>());
	}
	
	public PdfGenerator(final String wkhtml2pdf, final String url, final File dest, final String options, final Map<String, String> posts) {
		this.url = url;
		this.defaultOptions = options;
		this.dest = dest;
		this.wkhtml2pdf = wkhtml2pdf;
		
		final String[] postOptions = new String[posts.size()];
		int i = 0;
		for(final Entry<String, String> entry : posts.entrySet()) {
			postOptions[i++] = "--post " + entry.getKey() + " " + "\"" + entry.getValue().replace("\"", "\\\"") + "\"";
		}
		
		this.postOptions = Joiner.on(" ").join(postOptions);
	}
	
	@Override
	public void run() {
		final String dest = this.dest.getAbsolutePath();
		
		try {
			final Runtime rt = Runtime.getRuntime();
			final String cmd = Joiner.on(" ").join(wkhtml2pdf, defaultOptions, postOptions, url, dest);
			final Process pr = rt.exec(cmd);

			// exhaust input stream
			final BufferedInputStream in = new BufferedInputStream(
					pr.getInputStream());
			byte[] bytes = new byte[4096];
			while (in.read(bytes) != -1) {
			}

			// exhaust err stream
			final BufferedInputStream err = new BufferedInputStream(
					pr.getErrorStream());
			while (err.read(bytes) != -1) {
			}

			pr.waitFor();
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
