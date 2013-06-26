package com.emo.pitaya.pdf;

import com.emo.pitaya.web.Repository;
import com.typesafe.config.Config;

public class PdfConfig {
	
	public final String binary;
	public final String options;
	
	private final String webSource;
	
	public final Temporary temporary;
	public final Repository sandbox;

	public PdfConfig(final Config config) {
		this.binary = config.getString("wkhtmltopdf.path");
		this.options = config.getString("wkhtmltopdf.options");
		this.webSource = config.getString("web.source");
		this.temporary = new Temporary(config.getString("repo.temporary"));
		this.sandbox = new Repository(config.getString("repo.sandbox"));
	}
	
	public String webSourceFor(final String docId) {
		return this.webSource.replace("{id}", docId);
	}
	
}
