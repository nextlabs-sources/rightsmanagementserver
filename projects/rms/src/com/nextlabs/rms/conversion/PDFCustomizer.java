package com.nextlabs.rms.conversion;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;

public class PDFCustomizer {

	private static final Logger logger = Logger.getLogger(PDFCustomizer.class);

	public static void addAutoPrintOption(File inputFile) {
		PDDocument document = null;
		try {
			document = PDDocument.load(inputFile, MemoryUsageSetting.setupTempFileOnly());
			PDActionJavaScript javascript = new PDActionJavaScript("this.print();");
			document.getDocumentCatalog().setOpenAction(javascript);
			if (document.isEncrypted()) {
				throw new IOException("Encrypted documents are not supported");
			}
			document.save(inputFile);
		} catch (Exception e) {
			logger.error("Error occurred while adding Auto Print option to PDF: " + e.getMessage(), e);
		} finally {
			if (document != null) {
				try {
					document.close();
				} catch (IOException e) {
					logger.error("Error occurred while closing document", e);
				}
			}
		}
	}
}
