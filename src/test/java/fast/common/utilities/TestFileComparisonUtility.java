package fast.common.utilities;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class TestFileComparisonUtility {
	private String testDataFolder = "testdata/filecomparison/";

	@Test
	public void imageDiffPercentPositiveTest() throws Exception {
		String imgOnePath = testDataFolder + "car.jpg";
		String imgTwoPath = testDataFolder + "car.jpg";
		File firstfile = new File(imgOnePath);
		File sencondfile = new File(imgTwoPath);
		double result = FileComparisonUtility.imageDiffPercent(firstfile.toURI().toURL(), sencondfile.toURI().toURL());
		Assert.assertTrue(result == 0);

	}

	@Test
	public void imageDiffPercentNegativeTest() throws Exception {
		String imgOnePath = testDataFolder + "200px-Lenna50.jpg";
		String imgTwoPath = testDataFolder + "200px-Lenna100.jpg";
		File firstfile = new File(imgOnePath);
		File sencondfile = new File(imgTwoPath);
		double result = FileComparisonUtility.imageDiffPercent(firstfile.toURI().toURL(), sencondfile.toURI().toURL());
		Assert.assertTrue(result != 0);
	}
}
