package de.mpg.mpdl.service.rest.swc.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Joiner;
import de.mpg.mpdl.service.rest.swc.ServiceConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger LOGGER = LoggerFactory
			.getLogger(LMeasure.class);

	private static String LMEASURE_CMD;
	private ServiceConfiguration config = new ServiceConfiguration();
	// private Map<String, String> measureMap;
	private Map<String, Map<String, String>> measureMap;

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
	public Map<String, Map<String, String>> execute(File swcFile, String query,
			int numberOfBins, boolean widthOfBins) {
		measureMap = new HashMap<String, Map<String, String>>();
		try {
			File output = File.createTempFile("lmeasure", ".txt");
			query = query == null || query.trim().equals("") ? getDefaultQuery(
					numberOfBins, widthOfBins) : query;

			Process p = Runtime.getRuntime().exec(
					Joiner.on(" ").join(
							new String[] { LMEASURE_CMD, query,
									"-s" + output.getAbsolutePath(),
									swcFile.getAbsolutePath() }));

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
	private Map<String, Map<String, String>> parseOutput(File input, File output)
			throws IOException {
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
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
	private  Map<String, String> getLineMeasureValue(File input, String line) {
		String[] metrics = line.trim().split("\t");
		Map<String, String> map = new HashMap<String, String>();
		map.put("total_sum", metrics[2]);
		map.put("compartments_considered", metrics[3]);
		map.put("compartments_discarded", metrics[4].replace(")", "").replace("(", ""));
		map.put("minimum", metrics[5]);
		map.put("average", metrics[6]);
		map.put("average", metrics[6]);
		map.put("maximum", metrics[7]);
		map.put("s_d", metrics[8]);
		return map;
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
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper()
				.enable(SerializationFeature.INDENT_OUTPUT);
		return mapper.writeValueAsString(measureMap);
	}

}
