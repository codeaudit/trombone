package org.voyanttools.trombone.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.search.vectorhighlight.FieldTermStack.TermInfo;
import org.voyanttools.trombone.lucene.StoredToLuceneDocumentsMapper;
import org.voyanttools.trombone.model.Corpus;
import org.voyanttools.trombone.model.Kwic;
import org.voyanttools.trombone.storage.Storage;
import org.voyanttools.trombone.tool.analysis.KwicsQueue;
import org.voyanttools.trombone.tool.utils.AbstractContextTerms;
import org.voyanttools.trombone.util.FlexibleParameters;

public class Kwics extends AbstractContextTerms {
	
	private List<Kwic> kwics = new ArrayList<Kwic>();
	
	private Kwic.Sort kwicsSort;
	

	public Kwics(Storage storage, FlexibleParameters parameters) {
		super(storage, parameters);
		kwicsSort = Kwic.Sort.valueOfForgivingly(parameters.getParameterValue("sortBy", ""));
	}

	public List<Kwic> getKwics(AtomicReader atomicReader, StoredToLuceneDocumentsMapper corpusMapper, Corpus corpus) throws IOException {
		
		Map<Integer, Collection<DocumentSpansData>> documentSpansDataMap = getDocumentSpansData(atomicReader, corpusMapper, queries);
		return getKwics(atomicReader, corpusMapper, corpus, documentSpansDataMap);

	}
	
	private List<Kwic> getKwics(AtomicReader atomicReader, StoredToLuceneDocumentsMapper corpusMapper, Corpus corpus, Map<Integer, Collection<DocumentSpansData>> documentSpansDataMap) throws IOException {
		
		int[] totalTokens = corpus.getLastTokenPositions(tokenType);
		KwicsQueue queue = new KwicsQueue(limit, kwicsSort);
		for (Map.Entry<Integer, Collection<DocumentSpansData>> dsd : documentSpansDataMap.entrySet()) {
			int luceneDoc = dsd.getKey();
			int corpusDocIndex = corpusMapper.getDocumentPositionFromLuceneDocumentIndex(luceneDoc);
			int lastToken = totalTokens[corpusDocIndex];
			KwicsQueue q = getKwics(atomicReader, dsd.getKey(), corpusDocIndex, lastToken, dsd.getValue());
			Kwic k;
			while ((k = q.poll()) != null) {
				queue.offer(k);
			}
		}
		
		
		List<Kwic> localKwics = new ArrayList<Kwic>();
		Kwic k;
		while ((k = queue.poll()) != null) {
			localKwics.add(k);
		}
		Collections.reverse(localKwics);
		return localKwics;
	}
	
	
	private KwicsQueue getKwics(AtomicReader atomicReader, int luceneDoc, int corpusDocumentIndex,
			int lastToken, Collection<DocumentSpansData> documentSpansData) throws IOException {

		Map<Integer, TermInfo> termsOfInterest = getTermsOfInterest(atomicReader, luceneDoc, lastToken, documentSpansData, false);

		// build kwics
		KwicsQueue queue = new KwicsQueue(limit, kwicsSort);
		String document = atomicReader.document(luceneDoc).get(tokenType.name());
		for (DocumentSpansData dsd : documentSpansData) {
			for (int[] data : dsd.spansData) {
				int keywordstart = data[0];
				int keywordend = data[1];
				
				String middle = StringUtils.substring(document, termsOfInterest.get(keywordstart).getStartOffset(), termsOfInterest.get(keywordend-1).getEndOffset());
				
				String[] parts = new String[keywordend-keywordstart];
				for (int i=0; i<keywordend-keywordstart; i++) {
					parts[i] = termsOfInterest.get(keywordstart+i).getText();
				}
				String analyzedMiddle = StringUtils.join(parts, " ");
				
				
				int leftstart = keywordstart - context;
				if (leftstart<0) {leftstart=0;}
				String left = StringUtils.substring(document, termsOfInterest.get(leftstart).getStartOffset(), termsOfInterest.get(keywordstart).getStartOffset()-1);

				int rightend = keywordend + context;
				if (rightend>lastToken) {rightend=lastToken;}
				
				String right = StringUtils.substring(document, termsOfInterest.get(keywordend-1).getEndOffset()+1, termsOfInterest.get(rightend).getEndOffset());
				
				queue.offer(new Kwic(corpusDocumentIndex, dsd.queryString, analyzedMiddle, keywordstart, left, middle, right));
			}
		}
		
		return queue;
		
	}

	@Override
	protected void runQueries(Corpus corpus,
			StoredToLuceneDocumentsMapper corpusMapper, String[] queries)
			throws IOException {
		this.queries = queries; // FIXME: this should be set by superclass
		AtomicReader reader = SlowCompositeReaderWrapper.wrap(storage.getLuceneManager().getIndexReader());
		this.kwics = getKwics(reader, corpusMapper, corpus);
	}

	@Override
	protected void runAllTerms(Corpus corpus,
			StoredToLuceneDocumentsMapper corpusMapper) throws IOException {
		AtomicReader reader = SlowCompositeReaderWrapper.wrap(storage.getLuceneManager().getIndexReader());
		this.kwics = getKwics(reader, corpusMapper, corpus);
	}




}
