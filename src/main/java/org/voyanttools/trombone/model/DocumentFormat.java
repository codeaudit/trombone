/*******************************************************************************
 * Trombone is a flexible text processing and analysis library used
 * primarily by Voyant Tools (voyant-tools.org).
 * 
 * Copyright (©) 2007-2012 Stéfan Sinclair & Geoffrey Rockwell
 * 
 * This file is part of Trombone.
 * 
 * Trombone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Trombone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Trombone.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.voyanttools.trombone.model;

import java.io.File;

/**
 * An enumeration of the known document formats. The names are generic (like TEXT)
 * but can represent a number of file extensions (.txt) and content types (text/plain).
 * The Utilities for guessing file formats should be taken with a grain of salt
 * as most of them are based on simple heuristics to examine file extension or
 * content type (not by reading the actual file).
 * 
 * @author Stéfan Sinclair
 */
public enum DocumentFormat {

	/**
	 * A PDF document (.pdf)
	 */
	PDF("pdf"),
	
	/**
	 * An HTML document (.htm, .html, .xhtml).
	 */
	HTML("html", "htm", "xhtml"),

	/**
	 * An XML document (.xml).
	 */
	XML("xml"),

	/**
	 * An XML document (.xml).
	 */
	RSS("xml"),

	/**
	 * An XML document (.xml).
	 */
	RSS2("xml"),

	/**
	 * An XML document (.xml).
	 */
	ATOM("xml"),

	/**
	 * An XML document (.xml).
	 */
	TEI("xml"),

	/**
	 * An XML document (.xml).
	 */
	TEICORPUS("xml"),
	
	/**
	 * Specialized format for treating EEBO XML files
	 */
	EEBODREAM("xml"),
	
	/**
	 * An MS Word file (.doc).
	 */
	MSWORD("doc"),
	
	/**
	 * An MS Word XML file (.docx).
	 */
	MSWORDX("docx"),
	
	/**
	 * An RTF file (.rtf).
	 */
	RTF("rtf"),
	
	/**
	 * An Apple Pages file (.pages)
	 */
	PAGES("pages"),
	
	/**
	 * An Open Document file (.odt).
	 */
	ODT("odt"),
	
	/**
	 * An archive file ("ar", "cpio", "dump", "jar", "tar", "tgz", "tbz2", "zip")
	 */
	ARCHIVE("zip", "cpio", "dump", "jar", "tar.gz", "tar", "tgz", "ar"),
	
	/**
	 * A compressed file ("bzip2", "bz2", "gzip", "gz", "pack200", "xz")
	 */
	COMPRESSED("gz", "bz2", "gzip", "bzip2", "pack200", "xz"),
	
	/**
	 * A file that will be skipped ("png", "gif", "jpg", "jpeg", "bmp", "psd", "css", "js", "json")
	 */
	SKIPPABLE("png", "gif", "jpg", "jpeg", "bmp", "psd", "css", "js", "json") {
		@Override
		public boolean isSkippable() {
			return true;
		}
	},

	/**
	 * Test files. We'll put this last because of content types that declare things like text/html (we want HTML)
	 */
	TEXT("txt", "text"),

	/**
	 * An unknown file type.
	 */
	UNKNOWN;

	
	/**
	 * Determine if this format can be skipped (based on a list of known formats).
	 * 
	 * @return whether or not this format can be skipped
	 */
	public boolean isSkippable() {
		return false;
		
	}
	
	/**
	 * Determine if this is an XML-based format.
	 * 
	 * @return whether or not this is an XML-based format
	 */
	public boolean isXml() {
		for (String extension : extensions) {
			if (extension.equals("xml")) return true;
		}
		return false;
	}
	
	public String getDefaultExtension() {
		return extensions[0];
	}
	
	/**
	 * The valid extensions for this enum instance.
	 */
	private final String[] extensions;

	/**
	 * Constructs a new instance with the specified extensions.
	 * 
	 * @param extensions a list of extensions for this format
	 */
	private DocumentFormat(String... extensions) {
		this.extensions = extensions;
	}

	/**
	 * Get the format based on the file name (and in particular its extension).
	 * 
	 * @param filename the file name
	 * @return the format (UKNOWN if it's not recognized)
	 */
	public static DocumentFormat fromFilename(String filename) {

		String lowerCaseFileName = filename.toLowerCase();
		for (DocumentFormat format : DocumentFormat.values()) {
			for (String ext : format.extensions) {
				if (lowerCaseFileName.endsWith("."+ext)) {
					return format;
				}
			}
		}
		return UNKNOWN;
		
	}

	/**
	 * Get the format based on the file (and in particular the file name's extension).
	 * 
	 * @param file the file to examine
	 * @return the format (UKNOWN if it's not recognized)
	 */
	public static DocumentFormat fromFile(File file) {		
		return fromFilename(file.getName());
	}

	/**
	 * Get the format based on the specified content type (or MIME type). This
	 * is done by looking at known extensions and seeing if the content type
	 * contains any of those strings (e.g. application/xml is XML and text/html
	 * is HTML).
	 * 
	 * @param contentType the content type to examine
	 * @return the format (UKNOWN if it's not recognized)
	 */
	public static DocumentFormat fromContentType(String contentType) {
		contentType = contentType.toUpperCase();
		for (DocumentFormat format : DocumentFormat.values()) {
			for (String ext : format.extensions) {
				if (contentType.contains(ext.toUpperCase())) { // contains
					return format;
				}
			}
		}
		return UNKNOWN;
	}

	/**
	 * Determine if the file can be skipped (based on a list of known formats).
	 * 
	 * @param file the File to examine
	 * @return whether or not this format can be skipped
	 */
	public static boolean isSkippable(File file) {
		if (file.isHidden()) return true;
		if (file.getName().startsWith("__")) return true;
		return fromFile(file).isSkippable();
	}

}
