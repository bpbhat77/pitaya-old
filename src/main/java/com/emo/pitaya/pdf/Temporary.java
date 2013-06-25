package com.emo.pitaya.pdf;

import java.io.File;
import java.util.UUID;

public class Temporary {

	private final File base;
	
	public Temporary(final String base) {
		this.base = new File(base);
	}
	
	public File newPdfFile() {
		return new File(base, UUID.randomUUID().toString() + ".pdf");
	}
}
