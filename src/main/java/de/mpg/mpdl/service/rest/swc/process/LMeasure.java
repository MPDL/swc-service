package de.mpg.mpdl.service.rest.swc.process;

import com.google.common.base.Joiner;
import de.mpg.mpdl.service.rest.swc.ServiceConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * L-Measure Facade to use LMeasure in java
 * 
 * @author saquet
 *
 */
public class LMeasure {

	private static String LMEASURE_CMD;
	private ServiceConfiguration config = new ServiceConfiguration();
	private Map<String, String> measureMap;

	/**
	 * Default constructor
	 */
	public LMeasure() throws IOException, URISyntaxException {
		LMEASURE_CMD = config.getLMeasureBinary();
	}

	/**
	 * Execute L-Measure and return a key value response;
	 * 
	 * @param swcFile
	 * @param query
	 * @return
	 */
	public Map<String, String> execute(File swcFile, String query,
			int numberOfBins, boolean widthOfBins) {
		measureMap = new HashMap<String, String>();
		try {
			File output = File.createTempFile("lmeasure", ".txt");
			query = query == null || query.trim().equals("") ? getDefaultQuery(
					numberOfBins, widthOfBins) : query;


			Process p = Runtime.getRuntime().exec(Joiner.on(" ").join(
                new String[] {
                        LMEASURE_CMD,
                        query,
                        "-s"+output.getAbsolutePath(),
                        swcFile.getAbsolutePath()
                }
            ));

			p.waitFor();
			measureMap = parseOutput(swcFile, output);
		} catch (Exception e) {
			throw new RuntimeException("Error executing L-Measure", e);
		}
		return measureMap;
	}

	/**
	 * Parse the file output of L-Measure into a key value List
	 * 
	 * @param input
	 * @param output
	 * @return
	 * @throws IOException
	 */
	private Map<String, String> parseOutput(File input, File output)
			throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		for (String line : FileUtils.readLines(output)) {
			String name = getLineMeasureName(input, line);
			if (name != null)
				map.put(name, getLineMeasureValue(input, line));
		}
		return map;
	}

	/**
	 * Get the name of a measure for one line
	 * 
	 * @param input
	 * @param line
	 * @return
	 */
	private String getLineMeasureName(File input, String line) {
		String[] elements = line.replace(input.getAbsolutePath(), "").trim()
				.split(" ");
		if (elements.length > 1)
			return elements[0];
		return null;
	}

	/**
	 * Get the value of a measure for one line
	 * 
	 * @param input
	 * @param line
	 * @return
	 */
	private String getLineMeasureValue(File input, String line) {
		String[] elements = line.replace(input.getAbsolutePath(), "").trim()
				.split(" ");
		String value = "";
		if (elements.length > 1) {
			for (int i = 1; i < elements.length; i++) {
				value += elements[i].trim() + " ";
			}
		}
		return value;
	}

	private String getDefaultQuery(int numberOfBins, boolean widthOfBins) {
		String q = "";
		for (int i = 0; i < 45; i++) {
			if (i != 17 && i != 42) {
				// don't do 17 and 42, since not implemented by lmeasure
				q += " -f" + i + getParam(numberOfBins, widthOfBins);
			}
		}
		return q;
	}

	/**
	 * return the parameter as defined by L-Measure
	 * 
	 * @param numberOfBins
	 * @param widthOfBins
	 * @return
	 */
	private String getParam(int numberOfBins, boolean widthOfBins) {
		return ",0," + (widthOfBins ? 1 : 0) + ","
				+ (numberOfBins > 0 ? numberOfBins : "10");
	}

	/**
	 * Render the response into a json output
	 * 
	 * @return
	 */
	public String toJSON() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : measureMap.entrySet()) {
            sb
                .append("\"")
                .append(entry.getKey())
                .append("\": \"")
                .append(entry.getValue())
                .append("\",\n");
        }
        sb.setLength(sb.length() - 2);
		return "{\n" + sb.toString() + "\n}" ;
	}

}
