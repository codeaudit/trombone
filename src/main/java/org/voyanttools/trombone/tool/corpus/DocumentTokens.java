/**
 * 
 */
package org.voyanttools.trombone.tool.corpus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.vectorhighlight.FieldTermStack.TermInfo;
import org.apache.lucene.util.BytesRef;
import org.voyanttools.trombone.lucene.StoredToLuceneDocumentsMapper;
import org.voyanttools.trombone.model.Corpus;
import org.voyanttools.trombone.model.DocumentMetadata;
import org.voyanttools.trombone.model.DocumentToken;
import org.voyanttools.trombone.model.TokenType;
import org.voyanttools.trombone.storage.Storage;
import org.voyanttools.trombone.util.FlexibleParameters;
import org.voyanttools.trombone.util.Stripper;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * @author sgs
 *
 */
@XStreamAlias("documentTokens")
@XStreamConverter(DocumentTokensConverter.class)
public class DocumentTokens extends AbstractCorpusTool {

	private List<DocumentToken> documentTokens = new ArrayList<DocumentToken>();
	
	@XStreamOmitField
	private List<String> ids = new ArrayList<String>();

	@XStreamOmitField
	private int start;
	
	@XStreamOmitField
	private int limit;
	
	@XStreamOmitField
	private TokenType tokenType;

	/**
	 * @param storage
	 * @param parameters
	 */
	public DocumentTokens(Storage storage, FlexibleParameters parameters) {
		super(storage, parameters);
		start = parameters.getParameterIntValue("start", 0);
		limit = parameters.getParameterIntValue("limit", 50);
		tokenType = TokenType.lexical;
	}


	@Override
	protected void run(Corpus corpus, StoredToLuceneDocumentsMapper corpusMapper)
			throws IOException {
		IndexReader reader = SlowCompositeReaderWrapper.wrap(storage.getLuceneManager().getIndexReader());
		ids = this.getCorpusStoredDocumentIdsFromParameters(corpus);
		List<TermInfo> termInfos = new ArrayList<TermInfo>();
		TermInfo termInfo;
		int documentStart = start;
		Stripper stripper = new Stripper(parameters.getParameterValue("stripTags"));
		String skipToDocId = parameters.getParameterValue("skipToDocId", "");
		boolean isSkipping = true;
		for (String id : ids) {
			if (skipToDocId.isEmpty()==false) {
				if (isSkipping && skipToDocId.equals(id)) {
					isSkipping=false;
				}
				else {
					continue;
				}
			}
			int maxPos = documentStart+limit;

			int luceneDoc = corpusMapper.getLuceneIdFromDocumentId(id);
			Terms terms = reader.getTermVector(luceneDoc, tokenType.name());
			TermsEnum termsEnum = terms.iterator(null);
			Map<String, Integer> docFreqs = new HashMap<String, Integer>();
			while(true) {
				BytesRef term = termsEnum.next();
				if (term!=null) {
					String termString = term.utf8ToString();
					DocsAndPositionsEnum docsAndPositionsEnum = termsEnum.docsAndPositions(null, null, DocsAndPositionsEnum.FLAG_OFFSETS);
					for (int i=0, len = docsAndPositionsEnum.freq(); i<len; i++) {
						int pos = docsAndPositionsEnum.nextPosition();
						if (pos >= documentStart && pos<maxPos) { // out of range
							if (!docFreqs.containsKey(termString)) {
								docFreqs.put(termString, len);
							}
							termInfos.add(new TermInfo(termString, docsAndPositionsEnum.startOffset(), docsAndPositionsEnum.endOffset(), pos, 1));
						}
					}
				}
				else {break;}
			}
			Collections.sort(termInfos);
			List<DocumentToken> tokens = new ArrayList<DocumentToken>();
			String document = reader.document(luceneDoc).get(tokenType.name());
			String string;
			int lastEndOffset = 0;
			int corpusDocumentIndexPosition = corpus.getDocumentPosition(id);
			for (int i=0, len=termInfos.size(); i<len; i++) {
				termInfo = termInfos.get(i);
				if (i>0) { // add filler between tokens
					string = StringUtils.substring(document, lastEndOffset, termInfo.getStartOffset());
					documentTokens.add(new DocumentToken(id, corpusDocumentIndexPosition, stripper.strip(string), TokenType.other, -1, lastEndOffset, termInfo.getStartOffset(), -1)); // -1 position
					if (len+1<termInfos.size()) {len++;} // extend loop by one
				}
				string = StringUtils.substring(document, termInfo.getStartOffset(), termInfo.getEndOffset());
				documentTokens.add(new DocumentToken(id, corpusDocumentIndexPosition, string, tokenType, termInfo.getPosition(), termInfo.getStartOffset(), termInfo.getEndOffset(), docFreqs.get(termInfo.getText())));
				lastEndOffset = termInfo.getEndOffset();
			}
			termInfos.clear();
			documentStart = 0; // reset to the start of next document
		}
	}


	public List<DocumentToken> getDocumentTokens() {
		return documentTokens;
	}

}