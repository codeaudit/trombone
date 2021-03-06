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
package org.voyanttools.trombone.lucene;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.voyanttools.trombone.lucene.analysis.KitchenSinkPerFieldAnalyzerWrapper;
import org.voyanttools.trombone.storage.Storage;

/**
 * @author sgs
 *
 */
public class LuceneManager {
	
	private Directory directory;
	
	private DirectoryReader directoryReader = null;
	
	private IndexWriter indexWriter = null;
	
	private IndexSearcher indexSearcher = null;
	
	private Analyzer analyzer;
	
	private Storage storage;
	
	public LuceneManager(Storage storage, Directory directory) throws CorruptIndexException, IOException {
		this.storage = storage;
		this.directory = directory;
		analyzer = new KitchenSinkPerFieldAnalyzerWrapper(storage);
	}
	
//	public static Query getCorpusDocumentQuery(String corpusId, String documentId) {
//		BooleanQuery query = new BooleanQuery();
//		if (corpusId!=null) {query.add(new TermQuery(new Term("corpus", corpusId)), Occur.MUST);}
//		query.add(new TermQuery(new Term("id", documentId)), Occur.MUST);
//		query.add(new TermQuery(new Term("version", String.valueOf(VERSION))), Occur.MUST);
//		return query;
//	}
	

//	public static Query getDocumentQuery(String documentId) {
//		return getCorpusDocumentQuery(null, documentId);
//	}
	
//	private int getLuceneDocumentId(Query query) throws IOException {
//		if (DirectoryReader.indexExists(directory)==false) {return -1;}
//		TopDocs topDocs = getIndexSearcher().search(query, 1);
//		return topDocs.totalHits==1 ? topDocs.scoreDocs[0].doc : -1;
//	}
	
//	public Document getLuceneDocument(String corpusId, String documentId) throws IOException {
//		int id = getLuceneDocumentId(corpusId, documentId);
//		return id > -1 ? getIndexSearcher().doc(id) : null;
//	}
//
//	public Document getLuceneDocument(String documentId) throws IOException {
//		int id = getLuceneDocumentId(documentId);
//		return id > -1 ? getIndexSearcher().doc(id) : null;
//	}
	
//	public int getLuceneDocumentId(String corpusId, String documentId) throws IOException {
//		Query query = getCorpusDocumentQuery(corpusId, documentId);
//		return getLuceneDocumentId(query);
//	}
//	
//	public int getLuceneDocumentId(String documentId) throws IOException {
//		Query query = getCorpusDocumentQuery(null, documentId);
//		return getLuceneDocumentId(query);
//	}
	
//	public IndexSearcher getIndexSearcher() throws CorruptIndexException, IOException {
//		return getIndexSearcher(false);
//	}
//	
//	private IndexSearcher getIndexSearcher(boolean replace) throws CorruptIndexException, IOException {
//		if (indexSearcher == null || replace) {
//			indexSearcher = new IndexSearcher(getDirectoryReader());
//		}
//		return indexSearcher;
//	}
	
	public DirectoryReader getDirectoryReader() throws CorruptIndexException, IOException {
		return getDirectoryReader(false);
	}
	
	public DirectoryReader getDirectoryReader(boolean replace) throws CorruptIndexException, IOException {
		if (directoryReader == null || replace) {
			directoryReader = DirectoryReader.open(directory);
		}
		return directoryReader;
	}

	public void commit() throws CorruptIndexException, LockObtainFailedException, IOException {
		getIndexWriter().commit();
		
	}
	
	public void addDocument(Document document) throws CorruptIndexException, IOException {
		addDocuments(Arrays.asList(new Document[]{document}));
	}
	
	public void addDocuments(Collection<Document> documents) throws CorruptIndexException, IOException {
		IndexWriter writer = getIndexWriter();
		for (Document document : documents) {
			writer.addDocument(document);
		}
		writer.commit();
		setDirectoryReader(DirectoryReader.open(writer));
	}

//
//	public void updateDocument(Term term, Document document) throws CorruptIndexException, IOException {
//		document.add(new FloatField("version", luceneDocumentVersion, Field.Store.YES));
//		IndexWriter writer = getIndexWriter();
//		writer.addDocument(document);
//		writer.commit();
//		directoryReader = DirectoryReader.open(writer, false);
//		indexSearcher = new IndexSearcher(directoryReader);
//	}

	// TODO: make this block across threads so that only one writer can exist at a time
	public synchronized IndexWriter getIndexWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
		if (indexWriter==null) {
			indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));
		}
		return indexWriter;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}
	
	public boolean directoryExists() throws IOException {
		return DirectoryReader.indexExists(directory);
	}

	public void setDirectoryReader(DirectoryReader indexReader) {
		this.directoryReader = indexReader;
		this.indexSearcher = new IndexSearcher(directoryReader);
	}

}
