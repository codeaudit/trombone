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
package org.voyanttools.trombone.storage;

import java.io.IOException;

/**
 * This interface defines methods for interacting with stored objects using a storage strategy defined by the
 * implementing class.
 * 
 * @author Stéfan Sinclair
 */
public interface Storage {
	
	/**
	 * Get the {@link StoredDocumentSourceStorage} for this type of Storage.
	 * 
	 * @return the {@link StoredDocumentSourceStorage}
	 */
	public StoredDocumentSourceStorage getStoredDocumentSourceStorage();
	
	/**
	 * Destroy (delete) this storage.
	 * 
	 * @throws IOException thrown if an exception occurs during deletion
	 */
	public void destroy() throws IOException;

}
