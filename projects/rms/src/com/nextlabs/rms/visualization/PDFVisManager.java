package com.nextlabs.rms.visualization;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.EvalResponse;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.util.StringUtils;

public class PDFVisManager implements IVisManager {
	private static final Logger logger = Logger.getLogger(PDFVisManager.class);
	private static final IVisManager[] PDF_HANDLERS = { new CADVisManager(), new GenericVisManager() };
	private static final String _3D = "3D";
	private static final String U3D = "U3D";
	private static final String[] _3D_ELEMENTS = { _3D, U3D };

	enum PDFType {
		PDF_2D, PDF_3D, UNKNOWN
	}

	@Override
	public String getVisURL(RMSUserPrincipal user, String sessionId, EvalResponse evalRes, File folderpath,
			String displayName, String cacheId) throws RMSException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getVisURL(RMSUserPrincipal user, String sessionId, EvalResponse evalRes, byte[] fileContent,
			String displayName, String cacheId) throws RMSException {
		long start = System.currentTimeMillis();
		PDFType pdfType = determinePDFType(fileContent, displayName);
		long finish = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug("Time taken to determine PDF type of '" + displayName + "' (type: " + pdfType + "): "
					+ (finish - start) + " ms");
		}
		String url = null;
		try {
			if (pdfType == PDFType.PDF_3D) {
				IVisManager[] managers = { PDF_HANDLERS[0], PDF_HANDLERS[1] };
				url = generateVisURL(user, sessionId, evalRes, fileContent, displayName, cacheId, managers);
			} else if (pdfType == PDFType.PDF_2D) {
				IVisManager[] managers = { PDF_HANDLERS[1], PDF_HANDLERS[0] };
				url = generateVisURL(user, sessionId, evalRes, fileContent, displayName, cacheId, managers);
			} else {
				GlobalConfigManager configManager = GlobalConfigManager.getInstance();
				boolean use2DPDF = configManager.getBooleanProperty(GlobalConfigManager.USE_2D_PDF_VIEWER);
				if (use2DPDF) {
					IVisManager[] managers = { PDF_HANDLERS[1], PDF_HANDLERS[0] };
					url = generateVisURL(user, sessionId, evalRes, fileContent, displayName, cacheId, managers);
				} else {
					IVisManager[] managers = { PDF_HANDLERS[0], PDF_HANDLERS[1] };
					url = generateVisURL(user, sessionId, evalRes, fileContent, displayName, cacheId, managers);
				}
			}
			return url;
		} catch (Exception e) {
			logger.error("Unable to process file '" + displayName + "': " + e.getMessage(), e);
			if (e instanceof RMSException) {
				throw e;
			}
			throw new RMSException("There was an error while processing the file.");
		}
	}

	private String generateVisURL(RMSUserPrincipal user, String sessionId, EvalResponse evalRes, byte[] fileContent,
			String displayName, String cacheId, IVisManager... handlers) throws RMSException {
		if (handlers != null) {
			int total = handlers.length;
			int idx = 0;
			for (IVisManager handler : handlers) {
				try {
					return handler.getVisURL(user, sessionId, evalRes, fileContent, displayName, cacheId);
				} catch (Exception e) {
					if (idx == total - 1) {
						if (e instanceof RMSException) {
							throw (RMSException) e;
						} else {
							throw new RMSException(e.getMessage());
						}
					}
				}
				++idx;
			}
		}
		throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
	}

	public static PDFType determinePDFType(byte[] contents, String displayName) {
		try (PDDocument doc = PDDocument.load(contents)) {
			boolean pdf3D = false;
			PDPageTree pages = doc.getPages();
			for (PDPage page : pages) {
				List<PDAnnotation> annotations = page.getAnnotations();
				for (PDAnnotation annotation : annotations) {
					if (StringUtils.containsElement(_3D_ELEMENTS, annotation.getSubtype(), false)) {
						pdf3D = true;
						break;
					}
				}
				if (pdf3D) {
					// if we already any of 3D element, we can break the loop
					break;
				}
			}
			return pdf3D ? PDFType.PDF_3D : PDFType.PDF_2D;
		} catch (Exception e) {
			logger.error("Unable to determine PDF type of '" + displayName + "': " + e.getMessage(), e);
		} finally {

		}
		return PDFType.UNKNOWN;
	}
}
