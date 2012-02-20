package org.sakaiproject.nakamura.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Extracts static content from an OSGi bootstrap jar
 * 
 * @author ieb
 * 
 */
public class UnBundleStaticContent {

	private byte[] buffer = new byte[4096];
	private BootStrapLogger logger;

	public UnBundleStaticContent(BootStrapLogger bootStrapLogger) {
		this.logger = bootStrapLogger;
	}

	/**
	 * @param markerClass
	 *            a class to identify the jar in which the content is to be
	 *            loaded from, normally the bootstrap jar.
	 * @param bundlePath
	 *            the path in the jar where OSGi bundles are contained.
	 * @param strings
	 *            the path within the OSGi bundle where static content is
	 *            located.
	 * @param strings2
	 *            the folder where that content will be unpacked.
	 * @throws MalformedURLException
	 *             if the classpath element containing the marker class is not a
	 *             jar.
	 * @throws IOException
	 *             if the bootstrap jar file can't be opened.
	 */
	public void extract(Class<?> markerClass, String bundlePath,
			String[] source, String[] dest) throws MalformedURLException,
			IOException {
		File tempFolder = File.createTempFile("unpack", "bundles");

		tempFolder.delete();
		if (!tempFolder.mkdir()) {
			throw new IOException(
					"Unable to create working space at "
							+ tempFolder.getAbsolutePath()
							+ " (deleted a temp file but failed to recreate it as a directory, " +
									"please report at https://github.com/ieb/sparsemapcontent )");
		}
		try {
			List<File> unpackedBundles = unpackJarContents(
					getContainingJarFile(markerClass),
					new String[] { bundlePath },
					new String[] { tempFolder.getAbsolutePath() });
			for (File f : unpackedBundles) {
				try {
					unpackJarContents(f, source, dest);
				} catch (IOException e) {
					logger.info("Failed to Unpack " + f.getName(), e);
				}
			}
		} finally {
			deleteAll(tempFolder);
		}
	}

	private void deleteAll(File tempFolder) {
		if (tempFolder.exists()) {
			if (tempFolder.isDirectory()) {
				for (File f : tempFolder.listFiles()) {
					deleteAll(f);
				}
			}
			tempFolder.delete();
		}
	}

	private List<File> unpackJarContents(File containingJarFile,
			String[] source, String[] dest) throws IOException {
		JarFile jf = new JarFile(containingJarFile);
		List<File> files = new ArrayList<File>();
		for (Enumeration<JarEntry> jee = jf.entries(); jee.hasMoreElements();) {
			JarEntry je = jee.nextElement();
			String name = je.getName();
			for (int i = 0; i < source.length; i++) {
				if (name.startsWith(source[i]) && !je.isDirectory()) {
					File target = new File(dest[i], name.substring(source[i]
							.length()));
					if (!target.exists()
							|| target.lastModified() < je.getTime()
							|| je.getTime() < 0) {
						target.getParentFile().mkdirs();
						OutputStream out = new FileOutputStream(target);
						InputStream in = jf.getInputStream(je);
						copy(in, out);
						out.close();
						in.close();
						logger.info("Updated      " + target.getAbsoluteFile(),
								null);
					} else {
						logger.info("No Update to " + target.getAbsoluteFile(),
								null);

					}
					files.add(target);
				}
			}
		}
		return files;
	}

	private void copy(InputStream in, OutputStream out) throws IOException {
		int i = 0;
		while ((i = in.read(buffer)) >= 0) {
			if (i == 0) {
				Thread.yield();
			} else {
				out.write(buffer, 0, i);
			}
		}
	}

	private File getContainingJarFile(Class<?> clazz)
			throws MalformedURLException {
		String resource = clazz.getName().replace('.', '/') + ".class";
		URL u = clazz.getClassLoader().getResource(resource);
		String jarFilePath = u.getFile();
		jarFilePath = jarFilePath.substring(0,
				jarFilePath.length() - resource.length() - 2);
		u = new URL(jarFilePath);
		jarFilePath = u.getFile();
		return new File(jarFilePath);
	}

}
