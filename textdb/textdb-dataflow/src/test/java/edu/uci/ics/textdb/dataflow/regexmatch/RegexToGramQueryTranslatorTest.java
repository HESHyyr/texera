package edu.uci.ics.textdb.dataflow.regexmatch;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Shuying Lai
 * @author Zuozhi Wang
 */

public class RegexToGramQueryTranslatorTest {
	
	/*
	 * We need to check equivalence of two trees, but two equivalent trees could have many different forms.
	 * The equals function in GramBooleanQuery only compares two trees shallowly,
	 * it returns true if two trees' form (and content) are identical.
	 * 
	 * So we transform the tree to DNF form, and apply simplifications to remove redundant nodes.
	 * After transformation and simplification, two equivalent trees should have identical form.
	 * Then we can use the equals() function two check equivalence.
	 * 
	 */
	
	// Helper function to print query tree for debugging purposes.
	private void printTranslatorResult(String regex) {
		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translateUnsimplified(regex);
		
		GramBooleanQuery dnf = GramBooleanQuery.toDNF(exactQuery);
		GramBooleanQuery simplifiedDNF = GramBooleanQuery.simplifyDNF(dnf);
		
		System.out.println();
		System.out.println("----------------------------");
		
		System.out.println("regex: "+regex);
		System.out.println("boolean expression: "+simplifiedDNF.getLuceneQueryString());
		System.out.println();
		
//		System.out.println("original query tree: ");
//		System.out.println(exactQuery.printQueryTree());
//		
//		System.out.println("DNF: ");
//		System.out.println(dnf.printQueryTree());
		
		System.out.println("Simplified DNF: ");
		System.out.println(simplifiedDNF.printQueryTree());
		
		System.out.println("----------------------------");
		System.out.println();
	}
	
	// Helper function to transform a list of strings to a list of Leaf Node
	private List<GramBooleanQuery> getLeafNodeList(String... leafStringArray) {
		return 
			Arrays.asList(leafStringArray)
			.stream()
			.map(x -> GramBooleanQuery.newLeafNode(x))
			.collect(Collectors.toList());
	}
	
	@Test
	public void testEmptyRegex() {
		String regex = "";
		
		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
		
		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.OR);
		
		printTranslatorResult(regex);

		Assert.assertEquals(expectedQuery, exactQuery);
	}
	
	@Test
	public void testStarRegex() {
		String regex = "a*";
		
		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
		
		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.OR);
		
		printTranslatorResult(regex);

		Assert.assertEquals(expectedQuery, exactQuery);
	}
	
	@Test
	public void testLiteral1() {
		String regex = "abc";
		
		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
		
		GramBooleanQuery expectedQuery = GramBooleanQuery.newLeafNode("abc");
		
		printTranslatorResult(regex);

		Assert.assertEquals(expectedQuery, exactQuery);
	}
	
	// "ab" can't form a gram(default length 3), so the result is an empty OR node.
	@Test
	public void testLiteral2() {
		String regex = "ab";
		
		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
		
		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.OR);
		
		printTranslatorResult(regex);

		Assert.assertEquals(expectedQuery, exactQuery);
	}
	
	@Test
	public void testLiteral3() {
		String regex = "abcd";
		
		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
		
		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.AND);
		
		expectedQuery.subQuerySet.addAll(
				getLeafNodeList("abc", "bcd"));
		
		printTranslatorResult(regex);

		Assert.assertEquals(expectedQuery, exactQuery);
	}
	
	@Test
	public void testLiteral4() {
		String regex = "ucirvine";
		
		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);

		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.AND);
		
		expectedQuery.subQuerySet.addAll(
				getLeafNodeList("uci", "cir", "irv", "rvi", "vin", "ine"));
		
		printTranslatorResult(regex);

		Assert.assertEquals(expectedQuery, exactQuery);
	}
	
	@Test
	public void testLiteral5() {
		String regex = "textdb";
		
		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);

		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.AND);
		
		expectedQuery.subQuerySet.addAll(
				getLeafNodeList("tex", "ext", "xtd", "tdb"));
		
		printTranslatorResult(regex);

		Assert.assertEquals(expectedQuery, exactQuery);
	}

	
	@Test
	public void testCharClass1() {
		String regex = "[a-b][c-d][e-f]";
		
		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
		
		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.OR);
		
		expectedQuery.subQuerySet.addAll(
				getLeafNodeList("ace", "acf", "bce", "bcf", "ade", "adf", "bde", "bdf"));
		
		printTranslatorResult(regex);
		
		Assert.assertEquals(expectedQuery, exactQuery);
	}

	
	@Test
	public void testAlternate1() {
		String regex = "uci|ics";
		
		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
		
		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.OR);
		
		expectedQuery.subQuerySet.addAll(
				getLeafNodeList("uci", "ics"));

		printTranslatorResult(regex);
		
		Assert.assertEquals(expectedQuery, exactQuery);
	}
	
	@Test
	public void testAlternate2() {
		String regex = "data*(bcd|pqr)";
		
		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
		
		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.OR);
		
		GramBooleanQuery expectedAnd1 = new GramBooleanQuery(GramBooleanQuery.QueryOp.AND);		
		expectedQuery.subQuerySet.add(expectedAnd1);
		expectedAnd1.subQuerySet.addAll(
				getLeafNodeList("dat", "pqr"));
		
		GramBooleanQuery expectedAnd2 = new GramBooleanQuery(GramBooleanQuery.QueryOp.AND);
		expectedQuery.subQuerySet.add(expectedAnd2);
		expectedAnd2.subQuerySet.addAll(
				getLeafNodeList("dat", "bcd"));

		printTranslatorResult(regex);
			
		System.out.println(expectedQuery.equals(exactQuery));

		Assert.assertEquals(expectedQuery, exactQuery);
	}
//
//	@Test
//	public void testPlus1() {
//		String regex = "abc+";
//		
//		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
//		GramBooleanQuery dnf = GramBooleanQuery.toDNF(exactQuery);
//		GramBooleanQuery simplifiedDNF = GramBooleanQuery.simplifyDNF(dnf);
//		
//		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.OR);
//		GramBooleanQuery expectedAndNode = new GramBooleanQuery(GramBooleanQuery.QueryOp.AND);
//<<<<<<< HEAD
////		expectedAndNode.operandSet.addAll(Arrays.asList(new String[]{"abc"}));
//||||||| merged common ancestors
//		expectedAndNode.operandSet.addAll(Arrays.asList(new String[]{"abc"}));
//=======
//		expectedAndNode.operandSet.addAll(Arrays.asList("abc"));
//>>>>>>> e8794cc6f5fab13ca26535059b84a7aba4b8acec
//		expectedQuery.subQuerySet.add(expectedAndNode);
//		
//		printTranslatorResult(regex);
//
//		Assert.assertEquals(expectedQuery, simplifiedDNF);
//	}
//	
//	@Test
//	public void testPlus2() {
//		String regex = "abc+pqr+";
//		
//		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
//		GramBooleanQuery dnf = GramBooleanQuery.toDNF(exactQuery);
//		GramBooleanQuery simplifiedDNF = GramBooleanQuery.simplifyDNF(dnf);
//		
//		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.OR);
//		GramBooleanQuery expectedFirstAnd = new GramBooleanQuery(GramBooleanQuery.QueryOp.AND);
//<<<<<<< HEAD
////		expectedFirstAnd.operandSet.addAll(Arrays.asList(
////				new String[]{"abc", "cpq", "pqr"}));
//||||||| merged common ancestors
//		expectedFirstAnd.operandSet.addAll(Arrays.asList(
//				new String[]{"abc", "cpq", "pqr"}));
//=======
//		expectedFirstAnd.operandSet.addAll(Arrays.asList("abc", "cpq", "pqr"));
//>>>>>>> e8794cc6f5fab13ca26535059b84a7aba4b8acec
//		expectedQuery.subQuerySet.add(expectedFirstAnd);
//		
//		printTranslatorResult(regex);
//
//		Assert.assertEquals(expectedQuery, simplifiedDNF);		
//	}
//	
//	@Test
//	public void testQuest1() {
//		String regex = "abc?";
//		
//		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
//		GramBooleanQuery dnf = GramBooleanQuery.toDNF(exactQuery);
//		GramBooleanQuery simplifiedDNF = GramBooleanQuery.simplifyDNF(dnf);
//		
//		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.OR);
//		
//		printTranslatorResult(regex);
//
//		Assert.assertEquals(expectedQuery, simplifiedDNF);
//	}
//	
//	@Test
//	public void testQuest2() {
//		String regex = "abc?pqr?";
//		
//		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
//		GramBooleanQuery dnf = GramBooleanQuery.toDNF(exactQuery);
//		GramBooleanQuery simplifiedDNF = GramBooleanQuery.simplifyDNF(dnf);
//		
//		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.OR);
//		GramBooleanQuery expectedFirstAnd = new GramBooleanQuery(GramBooleanQuery.QueryOp.AND);
//<<<<<<< HEAD
////		expectedFirstAnd.operandSet.addAll(Arrays.asList(
////				new String[]{"abp", "bpq"}));
//||||||| merged common ancestors
//		expectedFirstAnd.operandSet.addAll(Arrays.asList(
//				new String[]{"abp", "bpq"}));
//=======
//		expectedFirstAnd.operandSet.addAll(Arrays.asList("abp", "bpq"));
//>>>>>>> e8794cc6f5fab13ca26535059b84a7aba4b8acec
//		expectedQuery.subQuerySet.add(expectedFirstAnd);
//		GramBooleanQuery expectedSecondAnd = new GramBooleanQuery(GramBooleanQuery.QueryOp.AND);
//<<<<<<< HEAD
////		expectedSecondAnd.operandSet.addAll(Arrays.asList(
////				new String[]{"abc", "bcp", "cpq"}));
//||||||| merged common ancestors
//		expectedSecondAnd.operandSet.addAll(Arrays.asList(
//				new String[]{"abc", "bcp", "cpq"}));
//=======
//		expectedSecondAnd.operandSet.addAll(Arrays.asList("abc", "bcp", "cpq"));
//>>>>>>> e8794cc6f5fab13ca26535059b84a7aba4b8acec
//		expectedQuery.subQuerySet.add(expectedSecondAnd);
//		
//		printTranslatorResult(regex);
//
//		Assert.assertEquals(expectedQuery, simplifiedDNF);		
//	}
//	
//	@Test
//	// RE2J will simplify REPEAT to equivalent form with QUEST.
//	// abc{1,3} will be simplified to abcc?c?
//	public void testRepeat1() {
//		String regex = "abc{1,3}";
//		
//		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
//		GramBooleanQuery dnf = GramBooleanQuery.toDNF(exactQuery);
//		GramBooleanQuery simplifiedDNF = GramBooleanQuery.simplifyDNF(dnf);
//		
//		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.OR);
//		GramBooleanQuery expectedAndNode = new GramBooleanQuery(GramBooleanQuery.QueryOp.AND);
//<<<<<<< HEAD
////		expectedAndNode.operandSet.addAll(Arrays.asList(new String[]{"abc"}));
//||||||| merged common ancestors
//		expectedAndNode.operandSet.addAll(Arrays.asList(new String[]{"abc"}));
//=======
//		expectedAndNode.operandSet.addAll(Arrays.asList("abc"));
//>>>>>>> e8794cc6f5fab13ca26535059b84a7aba4b8acec
//		expectedQuery.subQuerySet.add(expectedAndNode);
//		
////		printTranslatorResult(regex);
//
//		Assert.assertEquals(expectedQuery, simplifiedDNF);
//	}
//	
//	@Test
//	public void testCapture1() {
//		String regex = "(abc)(qwer)";
//		
//		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
//		GramBooleanQuery dnf = GramBooleanQuery.toDNF(exactQuery);
//		GramBooleanQuery simplifiedDNF = GramBooleanQuery.simplifyDNF(dnf);
//		
//		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.OR);
//		GramBooleanQuery expectedFirstAnd = new GramBooleanQuery(GramBooleanQuery.QueryOp.AND);
//<<<<<<< HEAD
////		expectedFirstAnd.operandSet.addAll(Arrays.asList(
////				new String[]{"abc", "bcq", "cqw", "qwe", "wer"}));
//||||||| merged common ancestors
//		expectedFirstAnd.operandSet.addAll(Arrays.asList(
//				new String[]{"abc", "bcq", "cqw", "qwe", "wer"}));
//=======
//		expectedFirstAnd.operandSet.addAll(Arrays.asList("abc", "bcq", "cqw", "qwe", "wer"));
//>>>>>>> e8794cc6f5fab13ca26535059b84a7aba4b8acec
//		expectedQuery.subQuerySet.add(expectedFirstAnd);
//		
//		printTranslatorResult(regex);
//
//		Assert.assertEquals(expectedQuery, simplifiedDNF);		
//	}
//	
//	@Test
//	public void testRegexCropUrl() {
//		String regex = "^(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?$";
//		
//		GramBooleanQuery exactQuery = RegexToGramQueryTranslator.translate(regex);
//		GramBooleanQuery dnf = GramBooleanQuery.toDNF(exactQuery);
//		GramBooleanQuery simplifiedDNF = GramBooleanQuery.simplifyDNF(dnf);
//		
//		GramBooleanQuery expectedQuery = new GramBooleanQuery(GramBooleanQuery.QueryOp.OR);
//		
//		printTranslatorResult(regex);
//
//		Assert.assertEquals(expectedQuery, simplifiedDNF);
//	}	
//	
}