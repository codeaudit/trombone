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
package org.voyanttools.trombone.storage.memory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.voyanttools.trombone.document.Metadata;
import org.voyanttools.trombone.document.StoredDocumentSource;
import org.voyanttools.trombone.input.source.InputSource;
import org.voyanttools.trombone.storage.StoredDocumentSourceStorage;

/**
 * @author sgs
 *
 */
public class MemoryStoredDocumentSourceStorage implements
		StoredDocumentSourceStorage {
	
	private Map<String, StoredDocumentSource> storedDocumentSourcesMap;
	private Map<String, byte[]> byteArraysMap;
	private Map<String, List<StoredDocumentSource>> multipleExpandedStoredDocumentSourcesMap;
	
	
	
	MemoryStoredDocumentSourceStorage() {
		this.storedDocumentSourcesMap = new HashMap<String, StoredDocumentSource>();
		this.byteArraysMap = new HashMap<String, byte[]>();
		this.multipleExpandedStoredDocumentSourcesMap = new HashMap<String, List<StoredDocumentSource>>();
	}

	/* (non-Javadoc)
	 * @see org.voyanttools.trombone.storage.StoredDocumentSourceStorage#getStoredDocumentSourceId(org.voyanttools.trombone.input.source.InputSource)
	 */
	public StoredDocumentSource getStoredDocumentSourceId(InputSource inputSource) throws IOException {
		String id = inputSource.getUniqueId();
		
		// check to see if it's already stored
		if (storedDocumentSourcesMap.containsKey(id)) {
			return storedDocumentSourcesMap.get(id);
		}
		
		// store the bytes
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = inputSource.getInputStream();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			IOUtils.copy(inputStream, outputStream);
			byteArraysMap.put(id, byteArrayOutputStream.toByteArray());
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
		}

		// create the stored document source and store it
		Metadata metadata = inputSource.getMetadata();
		ByteArrayOutputStream byteArrayInputStream = new ByteArrayOutputStream();
		StoredDocumentSource storedDocumentSource = new StoredDocumentSource(id, metadata);
		storedDocumentSourcesMap.put(id, storedDocumentSource);
		return storedDocumentSource;
	}

	/* (non-Javadoc)
	 * @see org.voyanttools.trombone.storage.StoredDocumentSourceStorage#getStoredDocumentSourceMetadata(java.lang.String)
	 */
	public Metadata getStoredDocumentSourceMetadata(String id)
			throws IOException {
		return storedDocumentSourcesMap.get(id).getMetadata();
	}

	/* (non-Javadoc)
	 * @see org.voyanttools.trombone.storage.StoredDocumentSourceStorage#getStoredDocumentSourceInputStream(java.lang.String)
	 */
	public InputStream getStoredDocumentSourceInputStream(String id) {
		return new ByteArrayInputStream(byteArraysMap.get(id));
	}

	/* (non-Javadoc)
	 * @see org.voyanttools.trombone.storage.StoredDocumentSourceStorage#getMultipleExpandedStoredDocumentSources(java.lang.String)
	 */
	public List<StoredDocumentSource> getMultipleExpandedStoredDocumentSources(
			String id) throws IOException {
		return getMultipleExpandedStoredDocumentSources(id, "");
	}

	/* (non-Javadoc)
	 * @see org.voyanttools.trombone.storage.StoredDocumentSourceStorage#getMultipleExpandedStoredDocumentSources(java.lang.String, java.lang.String)
	 */
	public List<StoredDocumentSource> getMultipleExpandedStoredDocumentSources(
			String id, String prefix) throws IOException {
		List<StoredDocumentSource> multipleExpandedStoredDocumentSources = new ArrayList<StoredDocumentSource>();
		if (multipleExpandedStoredDocumentSourcesMap.containsKey(id+prefix)) {
			multipleExpandedStoredDocumentSources.addAll(multipleExpandedStoredDocumentSourcesMap.get(id+prefix));
		}
		return multipleExpandedStoredDocumentSources;
	}

	/* (non-Javadoc)
	 * @see org.voyanttools.trombone.storage.StoredDocumentSourceStorage#setMultipleExpandedStoredDocumentSources(java.lang.String, java.util.List)
	 */
	public void setMultipleExpandedStoredDocumentSources(String id,
			List<StoredDocumentSource> archivedStoredDocumentSources)
			throws IOException {
		setMultipleExpandedStoredDocumentSources(id, archivedStoredDocumentSources, "");
	}

	/* (non-Javadoc)
	 * @see org.voyanttools.trombone.storage.StoredDocumentSourceStorage#setMultipleExpandedStoredDocumentSources(java.lang.String, java.util.List, java.lang.String)
	 */
	public void setMultipleExpandedStoredDocumentSources(String id,
			List<StoredDocumentSource> archivedStoredDocumentSources,
			String prefix) throws IOException {
		this.multipleExpandedStoredDocumentSourcesMap.put(id+prefix, archivedStoredDocumentSources);
	}

}