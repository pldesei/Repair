/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.localization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cofix.common.config.Constant;
import cofix.common.util.Pair;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @date Jul 4, 2017
 */
public class ManualLocator extends AbstractFaultlocalization {

	private Map<String, Pair<String, Integer>> _faultLocMap = new HashMap<>();
	private Map<String, String> _failedTestMap = new HashMap<>();

	private void init() {
		/**
		 * ================================================================================================================
		 */
		/*--------------------------------CHART----------------------------------------*/
		_faultLocMap.put("chart_4", new Pair<String, Integer>("org.jfree.chart.plot.XYPlot", 4493));
		_failedTestMap.put("chart_4", "org.jfree.chart.axis.junit.LogAxisTests::testXYAutoRange1");

		_faultLocMap.put("chart_6", new Pair<String, Integer>("org.jfree.chart.util.ShapeList", 111));
		_failedTestMap.put("chart_6", "org.jfree.chart.util.junit.ShapeListTests::testSerialization");

		_faultLocMap.put("chart_10",
				new Pair<String, Integer>("org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator", 65));
		_failedTestMap.put("chart_10",
				"org.jfree.chart.imagemap.junit.StandardToolTipTagFragmentGeneratorTests::testGenerateURLFragment");

		_faultLocMap.put("chart_13", new Pair<String, Integer>("org.jfree.chart.block.BorderArrangement", 454));
		_failedTestMap.put("chart_13",
				"org.jfree.chart.block.junit.BorderArrangementTests::testSizingWithWidthConstraint");

		_faultLocMap.put("chart_15", new Pair<String, Integer>("org.jfree.chart.plot.PiePlot", 2051));
		_failedTestMap.put("chart_15", "org.jfree.chart.plot.junit.PiePlot3DTests::testDrawWithNullDataset");

		_faultLocMap.put("chart_16",
				new Pair<String, Integer>("org.jfree.data.category.DefaultIntervalCategoryDataset", 338));
		_failedTestMap.put("chart_16",
				"org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testGetCategoryIndex");

		_faultLocMap.put("chart_17", new Pair<String, Integer>("org.jfree.data.time.TimeSeries", 857));
		_failedTestMap.put("chart_17", "org.jfree.data.time.junit.TimeSeriesTests::testBug1832432");

		_faultLocMap.put("chart_18", new Pair<String, Integer>("org.jfree.data.DefaultKeyedValues", 318));
		_failedTestMap.put("chart_18", "org.jfree.data.category.junit.DefaultCategoryDatasetTests::testBug1835955");

		_faultLocMap.put("chart_21",
				new Pair<String, Integer>("org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset", 152));
		_failedTestMap.put("chart_21",
				"org.jfree.data.statistics.junit.DefaultBoxAndWhiskerCategoryDatasetTests::testGetRangeBounds");

		_faultLocMap.put("chart_22", new Pair<String, Integer>("org.jfree.data.KeyedObjects2D", 231));
		_failedTestMap.put("chart_22", "org.jfree.data.junit.KeyedObjects2DTests::testRemoveColumnByKey");

		_faultLocMap.put("chart_23",
				new Pair<String, Integer>("org.jfree.chart.renderer.category.AbstractCategoryItemRenderer", 1291));
		_failedTestMap.put("chart_23",
				"org.jfree.chart.renderer.category.junit.MinMaxCategoryRendererTests::testEquals");

		_faultLocMap.put("chart_1",
				new Pair<String, Integer>("org.jfree.chart.renderer.category.AbstractCategoryItemRenderer", 1797));
		_failedTestMap.put("chart_1",
				"org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests::test2947660");

		_faultLocMap.put("chart_2", new Pair<String, Integer>("org.jfree.data.general.DatasetUtilities", 752));
		_failedTestMap.put("chart_2", "org.jfree.data.general.junit.DatasetUtilitiesTests::testBug2849731_2");

		_faultLocMap.put("chart_3", new Pair<String, Integer>("org.jfree.data.time.TimeSeries", 1057));
		_failedTestMap.put("chart_3", "org.jfree.data.time.junit.TimeSeriesTests::testCreateCopy3");

		_faultLocMap.put("chart_7", new Pair<String, Integer>("org.jfree.data.time.TimePeriodValues", 299));
		_failedTestMap.put("chart_7", "org.jfree.data.time.junit.TimePeriodValuesTests::testGetMaxMiddleIndex");

		_faultLocMap.put("chart_11", new Pair<String, Integer>("org.jfree.chart.util.ShapeUtilities", 275));
		_failedTestMap.put("chart_11", "org.jfree.chart.util.junit.ShapeUtilitiesTests::testEqualGeneralPaths");

		_faultLocMap.put("chart_12", new Pair<String, Integer>("org.jfree.chart.plot.MultiplePiePlot", 145));
		_failedTestMap.put("chart_12", "org.jfree.chart.plot.junit.MultiplePiePlotTests::testConstructor");

		_faultLocMap.put("chart_20", new Pair<String, Integer>("org.jfree.chart.plot.ValueMarker", 95));
		_failedTestMap.put("chart_20", "org.jfree.chart.plot.junit.ValueMarkerTests::test1808376");

		/**
		 * ================================================================================================================
		 */
		/*--------------------------------CLOSURE----------------------------------------*/
		_faultLocMap.put("closure_14",
				new Pair<String, Integer>("com.google.javascript.jscomp.ControlFlowAnalysis", 767));
		_failedTestMap.put("closure_14", "com.google.javascript.jscomp.CheckMissingReturnTest::testIssue779");

		_faultLocMap.put("closure_57",
				new Pair<String, Integer>("com.google.javascript.jscomp.ClosureCodingConvention", 197));
		_failedTestMap.put("closure_57", "com.google.javascript.jscomp.ClosureCodingConventionTest::testRequire");

		_faultLocMap.put("closure_73", new Pair<String, Integer>("com.google.javascript.jscomp.CodeGenerator", 1045));
		_failedTestMap.put("closure_73", "com.google.javascript.jscomp.CodePrinterTest::testUnicode");

		_faultLocMap.put("closure_77", new Pair<String, Integer>("com.google.javascript.jscomp.CodeGenerator", 966));
		_failedTestMap.put("closure_77", "com.google.javascript.jscomp.CodePrinterTest::testZero");

		/**
		 * ================================================================================================================
		 */
		/*--------------------------------LANG----------------------------------------*/
		_faultLocMap.put("lang_16", new Pair<String, Integer>("org.apache.commons.lang3.math.NumberUtils", 458));
		_failedTestMap.put("lang_16", "org.apache.commons.lang3.math.NumberUtilsTest::testCreateNumber");

		_faultLocMap.put("lang_33", new Pair<String, Integer>("org.apache.commons.lang3.ClassUtils", 909));
		_failedTestMap.put("lang_33", "org.apache.commons.lang3.ClassUtilsTest::testToClass_object");

		// need to split test case
		// first one : triggered by
		// String[] sa = ArrayUtils.add(stringArray, aString);
		_faultLocMap.put("lang_35", new Pair<String, Integer>("org.apache.commons.lang3.ArrayUtils", 3292));
		// second one : triggered by
		// String[] sa = ArrayUtils.add(stringArray, 0, aString);
		// _faultLocMap.put("lang_35", new Pair<String,
		// Integer>("org.apache.commons.lang3.ArrayUtils", 3575));
		_failedTestMap.put("lang_35", "org.apache.commons.lang3.ArrayUtilsAddTest::testLANG571");

		_faultLocMap.put("lang_39", new Pair<String, Integer>("org.apache.commons.lang3.StringUtils", 3675));
		_failedTestMap.put("lang_39",
				"org.apache.commons.lang3.StringUtilsTest::testReplace_StringStringArrayStringArray");

		_faultLocMap.put("lang_43",
				new Pair<String, Integer>("org.apache.commons.lang.text.ExtendedMessageFormat", 421));
		_failedTestMap.put("lang_43",
				"org.apache.commons.lang.text.ExtendedMessageFormatTest::testEscapedQuote_LANG_477");

		_faultLocMap.put("lang_58", new Pair<String, Integer>("org.apache.commons.lang.math.NumberUtils", 452));
		_failedTestMap.put("lang_58", "org.apache.commons.lang.math.NumberUtilsTest::testLang300");

		_faultLocMap.put("lang_59", new Pair<String, Integer>("org.apache.commons.lang.text.StrBuilder", 884));
		_failedTestMap.put("lang_59", "org.apache.commons.lang.text.StrBuilderAppendInsertTest::testLang299");

		// need to split test case
		// first one: triggered by
		// assertFalse( "The contains(char) method is looking beyond the end of the
		// string", sb.contains('h'));
		_faultLocMap.put("lang_60", new Pair<String, Integer>("org.apache.commons.lang.text.StrBuilder", 1673));
		// second one: triggered by
		// assertEquals( "The indexOf(char) method is looking beyond the end of the
		// string", -1, sb.indexOf('h'));
		// _faultLocMap.put("lang_60", new Pair<String,
		// Integer>("org.apache.commons.lang.text.StrBuilder", 1730));
		_failedTestMap.put("lang_60", "org.apache.commons.lang.text.StrBuilderTest::testLang295");

		/**
		 * ================================================================================================================
		 */
		/*--------------------------------MATH----------------------------------------*/
		_faultLocMap.put("math_5", new Pair<String, Integer>("org.apache.commons.math3.complex.Complex", 304));
		_failedTestMap.put("math_5", "org.apache.commons.math3.complex.ComplexTest::testReciprocalZero");

		_faultLocMap.put("math_33",
				new Pair<String, Integer>("org.apache.commons.math3.optimization.linear.SimplexTableau", 338));
		_failedTestMap.put("math_33", "org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath781");

		// need to avoid other failed test cases (comment others)
		// triggered by test cases:
		// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testChromosomeListConstructorTooLow
		// &
		// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testChromosomeListConstructorTooHigh
		_faultLocMap.put("math_35",
				new Pair<String, Integer>("org.apache.commons.math3.genetics.ElitisticListPopulation", 51));
		_failedTestMap.put("math_35",
				"org.apache.commons.math3.genetics.ElitisticListPopulationTest::testChromosomeListConstructorTooLow");
		// triggered by test cases:
		// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testConstructorTooLow
		// &
		// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testConstructorTooHigh
		// _faultLocMap.put("math_35", new Pair<String,
		// Integer>("org.apache.commons.math3.genetics.ElitisticListPopulation", 65));
		// _failedTest.put("math_35",
		// "org.apache.commons.math3.genetics.ElitisticListPopulationTest::testConstructorTooHigh");

		_faultLocMap.put("math_41",
				new Pair<String, Integer>("org.apache.commons.math.stat.descriptive.moment.Variance", 520));
		_failedTestMap.put("math_41",
				"org.apache.commons.math.stat.descriptive.moment.VarianceTest::testEvaluateArraySegmentWeighted");

		// need to spit test case
		// first one: for
		// [org.apache.commons.math.linear.SparseRealVectorTest::testConcurrentModification
		// && w = u.ebeDivide(v1);]
		_faultLocMap.put("math_49", new Pair<String, Integer>("org.apache.commons.math.linear.OpenMapRealVector", 345));
		// // second one: for
		// [org.apache.commons.math.linear.SparseRealVectorTest::testConcurrentModification
		// && w = u.ebeDivide(v2);]
		// _faultLocMap.put("math_49", new Pair<String,
		// Integer>("org.apache.commons.math.linear.OpenMapRealVector", 358));
		// // third one: for
		// [org.apache.commons.math.linear.SparseRealVectorTest::testConcurrentModification
		// && w = u.ebeMultiply(v1);]
		// _faultLocMap.put("math_49", new Pair<String,
		// Integer>("org.apache.commons.math.linear.OpenMapRealVector", 370));
		// // forth one: for
		// [org.apache.commons.math.linear.SparseRealVectorTest::testConcurrentModification
		// && w = u.ebeMultiply(v2);]
		// _faultLocMap.put("math_49", new Pair<String,
		// Integer>("org.apache.commons.math.linear.OpenMapRealVector", 383));
		_failedTestMap.put("math_49",
				"org.apache.commons.math.linear.SparseRealVectorTest::testConcurrentModification");

		_faultLocMap.put("math_53", new Pair<String, Integer>("org.apache.commons.math.complex.Complex", 153));
		_failedTestMap.put("math_53", "org.apache.commons.math.complex.ComplexTest::testAddNaN");

		_faultLocMap.put("math_59", new Pair<String, Integer>("org.apache.commons.math.util.FastMath", 3482));
		_failedTestMap.put("math_59", "org.apache.commons.math.util.FastMathTest::testMinMaxFloat");

		_faultLocMap.put("math_63", new Pair<String, Integer>("org.apache.commons.math.util.MathUtils", 417));
		_failedTestMap.put("math_63", "org.apache.commons.math.util.MathUtilsTest::testArrayEquals");

		_faultLocMap.put("math_70",
				new Pair<String, Integer>("org.apache.commons.math.analysis.solvers.BisectionSolver", 72));
		_failedTestMap.put("math_70", "org.apache.commons.math.analysis.solvers.BisectionSolverTest::testMath369");

		// need to avoid other failed test cases (comment others)
		// triggered by test case:
		// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testMissedEndEvent
		// _faultLocMap.put("math_71", new Pair<String,
		// Integer>("org.apache.commons.math.ode.nonstiff.RungeKuttaIntegrator", 174));
		// _failedTest.put("math_71",
		// "org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testMissedEndEvent");
		// triggered by test case:
		// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testMissedEndEvent
		_faultLocMap.put("math_71",
				new Pair<String, Integer>("org.apache.commons.math.ode.nonstiff.EmbeddedRungeKuttaIntegrator", 294));
		_failedTestMap.put("math_71",
				"org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testMissedEndEvent");

		// triggered by test case:
		// result = solver.solve(f, Math.PI, 4, 3.5);
		// _faultLocMap.put("math_72", new Pair<String,
		// Integer>("org.apache.commons.math.analysis.solvers.BrentSolver", 115));
		// triggered by Test case:
		// result = solver.solve(f, 3, Math.PI, 3.07);
		_faultLocMap.put("math_72",
				new Pair<String, Integer>("org.apache.commons.math.analysis.solvers.BrentSolver", 127));
		_failedTestMap.put("math_72", "org.apache.commons.math.analysis.solvers.BrentSolverTest::testRootEndpoints");

		_faultLocMap.put("math_75", new Pair<String, Integer>("org.apache.commons.math.stat.Frequency", 303));
		_failedTestMap.put("math_75", "org.apache.commons.math.stat.FrequencyTest::testPcts");

		_faultLocMap.put("math_79", new Pair<String, Integer>("org.apache.commons.math.util.MathUtils", 1624));
		_failedTestMap.put("math_79",
				"org.apache.commons.math.stat.clustering.KMeansPlusPlusClustererTest::testPerformClusterAnalysisDegenerate");

		// two failed test cases
		// first one, triggered by :
		// org.apache.commons.math.linear.BigMatrixImplTest::testMath209
		_faultLocMap.put("math_98", new Pair<String, Integer>("org.apache.commons.math.linear.BigMatrixImpl", 991));
		_failedTestMap.put("math_98", "org.apache.commons.math.linear.BigMatrixImplTest::testMath209");
		// second one, triggered by :
		// org.apache.commons.math.linear.RealMatrixImplTest::testMath209
		// _faultLocMap.put("math_98", new Pair<String,
		// Integer>("org.apache.commons.math.linear.RealMatrixImpl", 779));
		// _failedTest.put("math_98",
		// "org.apache.commons.math.linear.RealMatrixImplTest::testMath209");
	}

	public ManualLocator(Subject subject) {
		super(subject);
		init();
		locateFault(0);
	}

	public void setFailedTest(List<String> failedTests){
		_failedTests = failedTests;
	}
	
	@Override
	public List<Pair<String, Integer>> getLocations(int topK) {
		//System.out.println("topK:" + topK);
		// List<Pair<String, Integer>> locs = new ArrayList<>();
		// String obj = _subject.getName() + "_" + _subject.getId();
		// if(_faultLocMap.containsKey(obj)){
		// locs.add(_faultLocMap.get(obj));
		// }
		// return locs;

		// Manual verified and in TopK results of Ochiai
		String ochiFaultPath = Constant.HOME + "/d4j-info/location/ochiai/";
		// String actualFaultPath =
		// "/Users/yrr/Downloads/AuxiliaryTools-master/TransformFL/ActualFaultStatement/";
		String actualFaultPath = Constant.HOME + "/TransformFL/ActualFaultStatement/";

		File actualFaultFile = new File(actualFaultPath + _subject.getName() + "/" + _subject.getId());
		List<Pair<String, Integer>> actualFaultLocs = this.getActualFaultLocs(actualFaultFile);
		// System.out.println("actualFaultLocs Size:" + actualFaultLocs.size());
		// System.out.println("actualFaultLocs:" + actualFaultLocs);

		File ochiFaultFile = new File(ochiFaultPath + _subject.getName() + "/" + _subject.getId() + ".txt");
		return this.getLocatedActualFaultLocs(ochiFaultFile, topK, actualFaultLocs);

	}

	private List<Pair<String, Integer>> getLocatedActualFaultLocs(File file, int topK,
			List<Pair<String, Integer>> actualFaultLocs) {
		List<Pair<String, Integer>> locations = new ArrayList<>();
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = null;
			int count = 0;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.length() > 0) {
					String[] info = line.split("#");
					if (info.length < 2) {
						System.err.println("Location format error : " + line);
						System.exit(0);
					}
					String[] linesInfo = info[1].split(",");
					Integer lineNumber = Integer.parseInt(linesInfo[0]);
					String stmt = info[0];
					int index = stmt.indexOf("$");
					if (index > 0) {
						stmt = stmt.substring(0, index);
					}
					count++;		
					for (Pair<String, Integer> pair : actualFaultLocs) {
						//System.out.println("actualFaultLoc:" + pair.getFirst()+"  "+pair.getSecond());
						if (pair.getFirst().equals(stmt) && pair.getSecond().equals(lineNumber)) {
//							System.out.println("locatedActualFaultLine:" + pair);
//							System.out.println("LocatedRankInOchiaiRes:" + count);
							locations.add(pair);
						}
					}
					if (count == topK) {
						break;
					}
				}
			}
			bufferedReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return locations;
	}

	private List<Pair<String, Integer>> getActualFaultLocs(File file) {
		List<Pair<String, Integer>> actualFaultLocs = new ArrayList<Pair<String, Integer>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str = null;
			while ((str = br.readLine()) != null) {
				if (str.contains("||")) {
					String[] faultLocStrs = str.split("\\|\\|");
					for (String faultLocStr : faultLocStrs) {
						if (faultLocStr.contains(":")) {
							String faultMethodPath = faultLocStr.substring(0, faultLocStr.indexOf(":"));
							String faultFilePath = faultMethodPath.substring(0, faultMethodPath.lastIndexOf("."));
							int faultLine = Integer.parseInt(faultLocStr.substring(faultLocStr.indexOf(":") + 1));
							actualFaultLocs.add(new Pair(faultFilePath, faultLine));
						}
					}
				} else {
					String faultStmtStr = str;
					if (faultStmtStr.contains(":")) {
						String faultMethodPath = faultStmtStr.substring(0, faultStmtStr.indexOf(":"));
						String faultFilePath = faultMethodPath.substring(0, faultMethodPath.lastIndexOf("."));
						int faultLine = Integer.parseInt(faultStmtStr.substring(faultStmtStr.indexOf(":") + 1));
						actualFaultLocs.add(new Pair(faultFilePath, faultLine));
					}
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return actualFaultLocs;
	}

	@Override
	protected void locateFault(double threshold) {
		_failedTests.add(_failedTestMap.get(_subject.getName() + "_" + _subject.getId()));
	}

	public static void main(String args[]) {
		String ssrc = "/source", sbin = "/build", tsrc = "/tests", tbin = "/build-tests";
		String name = "chart";
		int id = 4;
		Subject subject = new Subject(name, id, ssrc, tsrc, sbin, tbin);
		AbstractFaultlocalization fLocalization = new ManualLocator(subject);
		System.out.println(fLocalization.getLocations(200));
	}

}
