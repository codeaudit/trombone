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

import java.text.Normalizer;
import java.util.Comparator;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * @author sgs
 *
 */
public class DocumentTerm {

	public enum Sort {
		rawFrequencyAsc, rawFrequencyDesc, relativeFrequencyAsc, relativeFrequencyDesc, termAsc, termDesc;
	}

	protected int docIndex;
	protected String docId;
	protected String term;
	@XStreamOmitField
	protected String normalizedString;
	protected int rawFreq;
	protected int totalTermsCount;
	protected float relativeFreq;
	protected int[] positions;
	protected int[] offsets;
	public DocumentTerm(int docIndex, String docId, String term, int rawFreq, int totalTokens, int[] positions, int[] offsets) {
		this.docIndex = docIndex;
		this.docId = docId;
		this.term = term;
		this.rawFreq = rawFreq;
		this.totalTermsCount = totalTokens;
		this.relativeFreq = totalTokens > 0 ? (float) rawFreq / totalTokens : 0;
		this.positions = positions;
		this.offsets = offsets;
		this.normalizedString = null;
	}
	public int getRawFrequency() {
		return rawFreq;
	}
	public String getNormalizedTerm() {
		if (normalizedString==null) {normalizedString = Normalizer.normalize(term, Normalizer.Form.NFD);}
		return normalizedString;
	}
	public String getTerm() {
		return term;
	}
	@Override
	public String toString() {
		return "("+docIndex+") "+term+": "+rawFreq+" ("+relativeFreq+")";
	}

	public float getRelativeFrequency() {
		return relativeFreq;
	}

	public int getDocumentIndex() {
		return docIndex;
	}

	public int[] getDistributions(int bins) {
		if (positions==null || bins ==0) return new int[0];
		int[] distributions = new int[bins];
		for(int position : positions) {
			distributions[(int) (position*bins/totalTermsCount)]++;
		}
		return distributions;
	}
	
	public static Comparator<DocumentTerm> getComparator(Sort sort) {
		switch (sort) {
		case rawFrequencyAsc:
			return RawFrequencyAscendingComparator;
		case termAsc:
			return TermAscendingComparator;
		case termDesc:
			return TermDescendingComparator;
		case rawFrequencyDesc:
			return RawFrequencyDescendingComparator;
		case relativeFrequencyAsc:
			return RelativeFrequencyAscendingComparator;
		default: // relativeDesc
			return RelativeFrequencyDescendingComparator;
		}
	}
	private static Comparator<DocumentTerm> TermAscendingComparator = new Comparator<DocumentTerm>() {
		@Override
		public int compare(DocumentTerm term1, DocumentTerm term2) {
			int i = term2.getNormalizedTerm().compareTo(term1.getNormalizedTerm());
			if (i==0) {
				return term1.rawFreq - term2.rawFreq;
			}
			return i;
		}
	};

	private static Comparator<DocumentTerm> TermDescendingComparator = new Comparator<DocumentTerm>() {
		@Override
		public int compare(DocumentTerm term1, DocumentTerm term2) {
			int i = term1.getNormalizedTerm().compareTo(term2.getNormalizedTerm());
			if (i==0) {
				return term1.rawFreq - term2.rawFreq;
			}
			return i;
		}
	};

	private static Comparator<DocumentTerm> RawFrequencyDescendingComparator = new Comparator<DocumentTerm>() {

		@Override
		public int compare(DocumentTerm term1, DocumentTerm term2) {
			if (term1.rawFreq==term2.rawFreq) {
				return term2.getNormalizedTerm().compareTo(term1.getNormalizedTerm());
			}
			else {
				return term1.rawFreq - term2.rawFreq;
			}
		}
		
	};
	
	private static Comparator<DocumentTerm> RawFrequencyAscendingComparator = new Comparator<DocumentTerm>() {

		@Override
		public int compare(DocumentTerm term1, DocumentTerm term2) {
			if (term1.rawFreq==term2.rawFreq) {
				return term2.getNormalizedTerm().compareTo(term1.getNormalizedTerm());
			}
			else {
				return term2.rawFreq - term1.rawFreq;
			}
		}
		
	};

	private static Comparator<DocumentTerm> RelativeFrequencyAscendingComparator = new Comparator<DocumentTerm>() {

		@Override
		public int compare(DocumentTerm term1, DocumentTerm term2) {
			if (term1.relativeFreq==term2.relativeFreq) {
				return term2.getNormalizedTerm().compareTo(term1.getNormalizedTerm());
			}
			else {
				return term2.rawFreq - term1.rawFreq;
			}
		}
		
	};

	private static Comparator<DocumentTerm> RelativeFrequencyDescendingComparator = new Comparator<DocumentTerm>() {

		@Override
		public int compare(DocumentTerm term1, DocumentTerm term2) {
			if (term1.relativeFreq==term2.relativeFreq) {
				return term2.getNormalizedTerm().compareTo(term1.getNormalizedTerm());
			}
			else {
				return term1.rawFreq - term2.rawFreq;
			}
		}
		
	};
	public int getTotalTermsCount() {
		return totalTermsCount;
	}
	
	public int getDocIndex() {
		return docIndex;
	}
	
	public String getDocId() {
		return docId;
	}
}
