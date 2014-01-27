package nf.co.korshyadoo.calendar;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@code FileVisitor} that finds
 * all files that match the
 * specified pattern.
 */
public class Finder extends SimpleFileVisitor<Path> {
	private final PathMatcher matcher;
	private List<Path> results = new ArrayList<>();
	private PstSearchFrame parent;									//A reference to the calling JFrame. Used to update lblSearchingFile showing the file that is currently being checked

	public Finder(String pattern, PstSearchFrame parent) {
		matcher = FileSystems.getDefault()
				.getPathMatcher("glob:" + pattern);
		this.parent = parent;
	}

	// Compares the glob pattern against
	// the file or directory name.
	boolean find(Path file) {
		Path name = file.getFileName();
		if (name != null && matcher.matches(name)) {
			return true;
		} else {
			return false;
		}
	}

	// Invoke the pattern matching
	// method on each file.
	@Override
	public FileVisitResult visitFile(Path file,
			BasicFileAttributes attrs) {
		if(find(file)) {
			results.add(file);
		}
		return CONTINUE;
	}

	// Invoke the pattern matching
	// method on each directory.
	@Override
	public FileVisitResult preVisitDirectory(Path dir,
			BasicFileAttributes attrs) {
		parent.setLBLSearchingFileText(dir.toString());
		find(dir);
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file,
			IOException exc) {
		//System.err.println(exc);
		return CONTINUE;
	}

	public List<Path> getResults() {
		return results;
	}
}

