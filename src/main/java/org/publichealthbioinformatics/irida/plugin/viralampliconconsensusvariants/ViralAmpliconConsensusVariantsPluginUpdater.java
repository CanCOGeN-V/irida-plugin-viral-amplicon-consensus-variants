package org.publichealthbioinformatics.irida.plugin.viralampliconconsensusvariants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import ca.corefacility.bioinformatics.irida.exceptions.IridaWorkflowNotFoundException;
import ca.corefacility.bioinformatics.irida.exceptions.PostProcessingException;
import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplateField;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.MetadataEntry;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.PipelineProvidedMetadataEntry;
import ca.corefacility.bioinformatics.irida.model.workflow.IridaWorkflow;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.AnalysisOutputFile;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.type.AnalysisType;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.pipeline.results.updater.AnalysisSampleUpdater;
import ca.corefacility.bioinformatics.irida.service.sample.MetadataTemplateService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;
import ca.corefacility.bioinformatics.irida.service.workflow.IridaWorkflowsService;

/**
 * This implements a class used to perform post-processing on the analysis
 * pipeline results to extract information to write into the IRIDA metadata
 * tables. Please see
 * <https://github.com/phac-nml/irida/blob/development/src/main/java/ca/corefacility/bioinformatics/irida/pipeline/results/AnalysisSampleUpdater.java>
 * or the README.md file in this project for more details.
 */
public class ViralAmpliconConsensusVariantsPluginUpdater implements AnalysisSampleUpdater {

	private final MetadataTemplateService metadataTemplateService;
	private final SampleService sampleService;
	private final IridaWorkflowsService iridaWorkflowsService;

	/**
	 * Builds a new {@link ViralAmpliconConsensusVariantsPluginUpdater} with the given services.
	 * 
	 * @param metadataTemplateService The metadata template service.
	 * @param sampleService           The sample service.
	 * @param iridaWorkflowsService   The irida workflows service.
	 */
	public ViralAmpliconConsensusVariantsPluginUpdater(MetadataTemplateService metadataTemplateService, SampleService sampleService,
													   IridaWorkflowsService iridaWorkflowsService) {
		this.metadataTemplateService = metadataTemplateService;
		this.sampleService = sampleService;
		this.iridaWorkflowsService = iridaWorkflowsService;
	}

	/**
	 * Code to perform the actual update of the {@link Sample}s passed in the
	 * collection.
	 * 
	 * @param samples  A collection of {@link Sample}s that were passed to this
	 *                 pipeline.
	 * @param analysis The {@link AnalysisSubmission} object corresponding to this
	 *                 analysis pipeline.
	 */
	@Override
	public void update(Collection<Sample> samples, AnalysisSubmission analysis) throws PostProcessingException {
		if (samples == null) {
			throw new IllegalArgumentException("samples is null");
		} else if (analysis == null) {
			throw new IllegalArgumentException("analysis is null");
		} else if (samples.size() != 1) {
			// In this particular pipeline, only one sample should be run at a time so I
			// verify that the collection of samples I get has only 1 sample
			throw new IllegalArgumentException(
					"samples size=" + samples.size() + " is not 1 for analysisSubmission=" + analysis.getId());
		}

		// extract the 1 and only sample (if more than 1, would have thrown an exception
		// above)
		final Sample sample = samples.iterator().next();

		// extracts paths to the analysis result files
		AnalysisOutputFile resultsOutputFile = analysis.getAnalysis().getAnalysisOutputFile("quast_report");
		Path resultsFilePath = resultsOutputFile.getFile();

		try {
			Map<String, MetadataEntry> metadataEntries = new HashMap<>();

			// get information about the workflow (e.g., version and name)
			IridaWorkflow iridaWorkflow = iridaWorkflowsService.getIridaWorkflow(analysis.getWorkflowId());
			String workflowVersion = iridaWorkflow.getWorkflowDescription().getVersion();
			String workflowName = iridaWorkflow.getWorkflowDescription().getName();

			// gets information from the "results.tsv" output file and constructs metadata
			// objects
			List<Map<String, String>> results = parseResultFile(resultsFilePath);

			for (Map<String, String> result : results) {
				PipelineProvidedMetadataEntry resultEntry = new PipelineProvidedMetadataEntry("result", "xs:string", analysis);
				// key will be string like 'resistance-screen/KPC/detected'
				String key;
				key = workflowName + "/result";
				metadataEntries.put(key, resultEntry);
			}

			Map<MetadataTemplateField, MetadataEntry> metadataMap = metadataTemplateService.getMetadataMap(metadataEntries);

			// merges with existing sample metadata
			sample.mergeMetadata(metadataMap);

			// does an update of the sample metadata
			sampleService.updateFields(sample.getId(), ImmutableMap.of("metadata", sample.getMetadata()));
		} catch (IOException e) {
			throw new PostProcessingException("Error parsing gene detection status file", e);
		} catch (IridaWorkflowNotFoundException e) {
			throw new PostProcessingException("Could not find workflow for id=" + analysis.getWorkflowId(), e);
		}
	}


	/**
	 * Parses out result values.
	 * 
	 * @param resultFilePath The {@link Path} to the file containing results. Contents look like:
	 *
	 * 
	 * @return An {@link List<Map>} linking 'geneName' to 'detectionStatus'.
	 * @throws IOException             If there was an error reading the file.
	 * @throws PostProcessingException If there was an error parsing the file.
	 */
	@VisibleForTesting
	List<Map<String, String>> parseResultFile(Path resultFilePath) throws IOException, PostProcessingException {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();

		BufferedReader resultReader = new BufferedReader(new FileReader(resultFilePath.toFile()));

		try {
			String headerLine = resultReader.readLine();

			String[] fieldNames = headerLine.split("\t");
			HashMap<String, String> resultTmp = new HashMap<>();
			String resultLine;
			while (( resultLine = resultReader.readLine()) != null) {
				String[] result = resultLine.split("\t", -1);
				for (int i = 0; i < fieldNames.length; i++) {
					resultTmp.put(fieldNames[i], result[i]);
				}
				HashMap<String, String> clonedResults = (HashMap<String, String>) resultTmp.clone();
				results.add(clonedResults);
			}

		} finally {
			// make sure to close, even in cases where an exception is thrown
			resultReader.close();
		}

		return results;
	}

	/**
	 * The {@link AnalysisType} this {@link AnalysisSampleUpdater} corresponds to.
	 * 
	 * @return The {@link AnalysisType} this {@link AnalysisSampleUpdater}
	 *         corresponds to.
	 */
	@Override
	public AnalysisType getAnalysisType() {
		return ViralAmpliconConsensusVariantsPlugin.VIRAL_AMPLICON_CONSENSUS_VARIANTS;
	}
}
