package jss.webframework;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

/**
 * Request parameter
 * 
 * @author lukas
 */
public class RequestParam {
	// for all types
	private final String name;

	// value parameter
	private final List<String> values;

	// file parameter
	private final List<FileInfo> files;

	/**
	 * Konstruktor
	 * 
	 * @param name
	 * @param values
	 * @param files
	 */
	private RequestParam(String name, String[] values, FileInfo[] files) {
		this.name = name;

		this.values = new ArrayList<String>();
		if (values != null) {
			for (String v : values) {
				this.values.add(URLDecoder.decode(v, StandardCharsets.UTF_8));// dekoduj URL
			}
		}

		this.files = new ArrayList<FileInfo>();
		if (files != null) {
			this.files.addAll(Arrays.asList(files));
		}
	}

	/**
	 * Konstruktor dla wartości
	 * 
	 * @param name
	 * @param values
	 */
	public RequestParam(String name, String[] values) {
		this(name, values, null);
	}

	/**
	 * Konstruktor dla plików
	 * 
	 * @param name
	 * @param files
	 */
	public RequestParam(String name, FileInfo[] files) {
		this(name, null, files);
	}

	/**
	 * Nazwa
	 */
	public String getName() {
		return name;
	}

	/**
	 * Pojedyńcza wartość, lub null (gdy ilość wartości wynosi 0, lub jest ich
	 * więcej niż 1)
	 */
	public String getSingleValue() {
		if (values.size() == 1) {
			return values.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Tablica wartości (może być o długości 0 gdy nie ma wartości)
	 */
	public String[] getMultipleValues() {
		return values.toArray(new String[values.size()]);
	}

	/**
	 * Pojedyńczy plik, lub null (gdy ilość plików wynosi 0, lub jest ich więcej niż
	 * 1)
	 */
	public FileInfo getSingleFile() {
		if (files.size() == 1) {
			return files.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Tablica plików (może być o długości 0, gdy nie ma plików)
	 */
	public FileInfo[] getMultipleFiles() {
		return files.toArray(new FileInfo[files.size()]);
	}

	/**
	 * Czy posiada pojedyńczy plik, lub wiele plików?
	 */
	public boolean hasFile() {
		return hasSingleFile() || hasMultipleFiles();
	}

	/**
	 * Czy posiada pojedyńczy plik?
	 */
	public boolean hasSingleFile() {
		return files.size() == 1;
	}

	/**
	 * Czy posiada wiele plików?
	 */
	public boolean hasMultipleFiles() {
		return files.size() > 1;
	}

	/**
	 * Czy posiada pojedyńczą wartość?
	 */
	public boolean hasSingleValue() {
		return values.size() == 1;
	}

	/**
	 * Czy posiada wiele wartości?
	 */
	public boolean hasMultipleValues() {
		return values.size() > 1;
	}

	/**
	 * Czy posiada pojedyńczą wartość lub wiele wartości?
	 */
	public boolean hasValue() {
		return hasSingleValue() || hasMultipleValues();
	}

	/**
	 * Czy posiada pojedyńczą wartość lub wiele wartości, lub posiada jeden plik lub
	 * więcej plików?
	 */
	public boolean hasValueOrIsFile() {
		return hasValue() || hasFile();
	}

	List<String> getValuesInternalList() {
		return values;
	}

	List<FileInfo> getFilesInternalList() {
		return files;
	}

	@Override
	public String toString() {
		return "RequestParam [name=" + name + ", values=" + values + ", files=" + files + "]";
	}

	/**
	 * Info o pliku
	 * 
	 * @author lukas
	 */
	public static class FileInfo {
		private final long size;
		private final String fileName;
		private final String contentType;
		private Part part;
		private HttpServletRequest request;

		/**
		 * Konstruktor
		 * 
		 * @param size
		 * @param fileName
		 * @param contentType
		 * @param part
		 * @param request
		 */
		FileInfo(long size, String fileName, String contentType, Part part, HttpServletRequest request) {
			this.size = size;
			this.fileName = fileName;
			this.contentType = contentType;
			this.part = part;
			this.request = request;
		}

		/**
		 * Rozmiar pliku
		 */
		public long getSize() {
			return size;
		}

		/**
		 * Nazwa przesłanego pliku
		 */
		public String getUploadedFileName() {
			return fileName;
		}

		/**
		 * Typ zawartości pliku
		 */
		public String getContentType() {
			return contentType;
		}

		/**
		 * Zapisuje plik na dysku, w katalogu wewnątrz aplikacji. Jeżeli brak folderów -
		 * tworzy je.
		 * 
		 * @param dir      katalog w którym ma zostać zapisany plik
		 * @param filename nazwa pliku
		 * @throws IOException w przypadku błędu, lub gdy plik został już zapisany!
		 */
		public void saveFileInsideWebapp(String dir, String filename) throws IOException {
			if (part == null || request == null) {
				throw new IOException("File was already saved!");
			}

			if (!dir.endsWith(File.separator)) {
				dir = dir + File.separator;
			}
			String path = request.getServletContext().getRealPath(dir);// ścieżka względna

			saveFileInPath(path, filename);
		}

		/**
		 * Zapisuje plik na dysku w podanej ścieżce bezwzględnej
		 * 
		 * @param path     ścieżka bezwzględna do katalogu
		 * @param filename nazwa pliku
		 * @throws IOException w przypadku błędu, lub gdy plik został już zapisany!
		 */
		public void saveFileInPath(String path, String filename) throws IOException {
			if (part == null || request == null) {
				throw new IOException("File was already saved!");
			}

			if (!path.endsWith(File.separator)) {
				path = path + File.separator;
			}
			new File(path).mkdirs();// utwórz foldery

			File file = new File(path + filename);
			try (InputStream input = part.getInputStream()) {
				Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}

			// file was saved, clear fields need to save
			part = null;
			request = null;
		}

		@Override
		public String toString() {
			return "FileInfo [size=" + size + ", fileName=" + fileName + ", contentType=" + contentType + ", part="
					+ part + ", request=" + request + "]";
		}

	}// class FileInfo

}
