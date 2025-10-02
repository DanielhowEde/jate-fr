package utils.pdf;

import java.nio.file.*;
import java.util.Objects;
import java.util.stream.Stream;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.OutputStream;

public class DocToPdf {
/**
  * Reusables API: convert all .docx > .pdf
  *@param inputDir		directory to scan
  *@param outputDir		directory to put pdfs in
  *@param recursive 		scan subfolders if true
*/

	public static void convertFile(Path docx, Path pdf) throws Exception {
		Files.createDirectories(pdf.getParent() == null ? Paths.get(".") : pdf.getParent());
		try(OutputStream os = Files.newOutputStream(pdf)) {
			WordprocessingMLPackage word = WordprocessingMLPackage.load(docx.toFile());
			Docx4J.toPDF(word, os);		
		}
	}
	
	public static void convertDirectory(Path inputDir, Path outputDir, boolean recursive) throws Exception{
		Objects.requireNonNull(inputDir, "inputDir");
		Objects.requireNonNull(outputDir, "outputDir");
		
		if (!Files.isDirectory(inputDir)){
			throw new IllegalArgumentException("Input is not a directory:" + inputDir);
		}
		try {
			Files.createDirectories(outputDir);
		}catch (Exception e) {
			new RuntimeException("Can not create output Dir. " + outputDir);
		}
		
		Stream<Path> stream = recursive ? Files.walk(inputDir) : Files.list(inputDir);
		try (stream) {
			stream
			.filter(p -> Files.isRegularFile(p))
			.filter(p -> p.getFileName().toString().toLowerCase().endsWith("docx"))
			.forEach(docx -> {
				try {
					Path rel = inputDir.relativize(docx);
					Path outRel = replaceExt(rel, "pdf");
					Path pdf = outputDir.resolve(outRel);
					Files.createDirectories(pdf.getParent());
					convertFile(docx, pdf);
				} catch (Exception e) {
					System.err.println(" Failed: " + docx + " : " + e.getMessage());
				}
			});
		}
	}

	private static Path replaceExt(Path path, String newExt) {
		String name = path.getFileName().toString();
		int dot = name.lastIndexOf('.');
		String base = (dot > 0) ? name.substring(0,dot) : name;
		return path.getParent() == null ? Paths.get(base + newExt) : path.getParent().resolve(base + newExt);
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("""
					Usage: java DocToPdf <inputDir> <outputDir> [--recursive]
					
					Examples:
					  java  DocToPdf C:/docs C:/pdfs
					  java  DocToPdf /data/in /data/out --recursive
					
					""");
			return;
		}
		Path in = Paths.get(args[0]);
		Path out = Paths.get(args[1]);
		
		boolean recursive = args.length >= 3 && "--recursive".equalsIgnoreCase(args[2]);
		convertDirectory(in, out, recursive);
	}
}
