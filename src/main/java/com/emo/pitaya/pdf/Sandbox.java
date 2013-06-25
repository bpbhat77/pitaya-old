package com.emo.pitaya.pdf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Sandbox {

	public final File base;
	
	public Sandbox(final String base) {
		this.base = new File(base);
	}
	
	public File relativeToDoc(final String docId, final String element) throws IOException {
		final File docPath = new File(base, docId);
		final File elementPath = new File(docPath, element);

		if (!fileBelongToDirectory(elementPath, base)) {
			throw new SecurityException(
					"security error : computing path from URL : "
							+ elementPath.getPath());
		}
		
		return elementPath;
	}
	
	private boolean fileBelongToDirectory(File file, File directory)
			throws IOException {
		final File parent = file.getParentFile();

		if (null == parent) {
			return false;
		}

		if (Files.isSameFile(parent.toPath(), directory.toPath())) {
			return true;
		} else {
			return fileBelongToDirectory(parent, directory);
		}
	}

}
