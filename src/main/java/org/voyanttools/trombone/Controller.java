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
package org.voyanttools.trombone;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.voyanttools.trombone.storage.Storage;
import org.voyanttools.trombone.storage.file.FileStorage;
import org.voyanttools.trombone.storage.memory.MemoryStorage;
import org.voyanttools.trombone.tool.utils.ToolRunner;
import org.voyanttools.trombone.util.FlexibleParameters;

/**
 * @author sgs
 *
 */
public class Controller {

	private FlexibleParameters parameters;
	private Storage storage;
	private Writer writer = null;

	public Controller(FlexibleParameters parameters) throws IOException {
		this(parameters, getWriter(parameters));
	}

	public Controller(FlexibleParameters parameters, Writer writer) throws IOException {
		this(parameters.getParameterValue("storage","").equals("file") ? new FileStorage() : new MemoryStorage(), parameters, writer);
	}
	
	public Controller(Storage storage, FlexibleParameters parameters, Writer writer) throws IOException {
		this.storage = storage;
		this.parameters = parameters;
		this.writer = writer;
	}
	
	public Controller(Storage storage, FlexibleParameters parameters) throws IOException {
		this.storage = storage;
		this.parameters = parameters;
	}

	private static Writer getWriter(FlexibleParameters parameters) throws IOException {
		if (parameters.containsKey("outputFile")) {
			return new FileWriter(parameters.getParameterValue("outputFile"));
		}
		else {
			return new OutputStreamWriter(System.out);
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args == null) {
			throw new NullPointerException("illegal arguments");
		}

		final FlexibleParameters parameters = new FlexibleParameters(args);
		
		if (parameters.containsKey("outputFile")) {
			Writer writer = new FileWriter(parameters.getParameterValue("outputFile"));
			final Controller controller = new Controller(parameters, writer);		
			controller.run();
			writer.close();
		}
		else {
			final Controller controller = new Controller(parameters);		
			controller.run();
		}
	}

	public void run(OutputStream outputStream) throws IOException {
		ToolRunner toolRunner = new ToolRunner(storage, parameters, outputStream);
		toolRunner.run();
	}
	
	public void run(Writer writer) throws IOException {
		ToolRunner toolRunner = new ToolRunner(storage, parameters, writer);
		toolRunner.run();
	}
	
	public void run() throws IOException {
		
		ToolRunner toolRunner = new ToolRunner(storage, parameters, writer);
		toolRunner.run();
		
	}

}
