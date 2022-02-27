// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>The FilesProcessor processes all files within the given directory (constructor param 1) and will call the also
 * given functional interface instance (constructor param 2) for each file. The given directory of param 1 has to be
 * part of the application's resources directory.</p>
 *
 * <p>The FilesProcessor is able to locate the files from either directly the filesystem or within the JAR
 * depending on how the application is running, thus working well from either deployed JAR or within the IDE.</p>
 */
public class FilesProcessor
{
	private static final Logger LOG = LoggerFactory.getLogger(FilesProcessor.class);

	@FunctionalInterface
	public interface IProcessorFunction {
		void process(String filePath, String content);
	}

	private final String directory;
	private final IProcessorFunction processor;

	public FilesProcessor(final String directory, final IProcessorFunction processor)
	{
		this.directory = directory;
		this.processor = processor;
	}

	private boolean isRunningFromJar()
	{
		URL url = FilesProcessor.class.getResource("FilesProcessor.class");
		if (url != null) {
			return url.toString().startsWith("jar:");
		}
		throw new RuntimeException("Class not found: " + FilesProcessor.class.getSimpleName());
	}

	public void work()
	{
		try {
			List<Path> result = isRunningFromJar() ? getPathsFromResourceJAR(directory)
									: getAllFilesFromResource(directory);
			for (Path path : result)
			{
				String filePath = path.toString();
				// Windows will return /path/file for contents within JAR files,
				// and same for the results of getAllFilesFromResource().
				// So cut the first /, the correct path should be path/file
				if (filePath.startsWith("/")) {
					filePath = filePath.substring(1);
				}
				// Read a file from resource folder
				InputStream is = getFileFromResourceAsStream(filePath);
				String content = readInputStream(is);
				// Call worker for each file
				processor.process(filePath, content);
			}
		} catch (URISyntaxException | IOException e) {
			LOG.error("Error", e);
		}
	}

	private InputStream getFileFromResourceAsStream(String fileName)
	{
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(fileName);

		if (inputStream == null) {
			throw new IllegalArgumentException("File not found: " + fileName);
		} else {
			return inputStream;
		}
	}

	private List<Path> getPathsFromResourceJAR(String folder)
		throws URISyntaxException, IOException
	{
		List<Path> result;
		// Get path of the current running JAR
		String jarPath = getClass().getProtectionDomain()
			.getCodeSource()
			.getLocation()
			.toURI()
			.getPath();
		URI uri = URI.create("jar:file:" + jarPath);

		try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap()))
		{
			result = Files.walk(fs.getPath(folder))
				.filter(Files::isRegularFile)
				.collect(Collectors.toList());
		}

		return result;
	}

	private List<Path> getAllFilesFromResource(String folder)
		throws URISyntaxException, IOException
	{
		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource(folder);
		Path directory = Path.of(classLoader.getResource(".").toURI());

		List<Path> collect = Files.walk(Paths.get(resource.toURI()))
			.filter(Files::isRegularFile)
			.map(x -> Path.of(x.toString().replace(directory.toString(), "")))
			.collect(Collectors.toList());

		return collect;
	}

	private static String readInputStream(InputStream is)
	{
		String str = null;
		try {
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}
			str = result.toString(StandardCharsets.UTF_8.name());
		} catch (IOException e) {
			LOG.error("Error reading file", e);
		}
		return str;
	}
}