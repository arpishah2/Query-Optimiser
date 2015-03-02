import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * CS 267 - Project - Manages printing of the output for DBMS.
 */
public class DbmsPrinter {

	SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss :");
	public ArrayList<PrintWriter> files;

	public DbmsPrinter() {
		files = new ArrayList<PrintWriter>();
	}

	public void addPrinter(String fileName) {
		try {
			files.add(new PrintWriter(new FileOutputStream(new File(fileName),
					true)));
		} catch (FileNotFoundException e) {
		}
	}

	public void println(String line) {
		line = dateFormat.format(Calendar.getInstance().getTime()) + "\t" + line;
		System.out.println(line);
		for (PrintWriter file : files) {
			file.println(line);
		}
	}

	public void cleanup() {
		for (PrintWriter file : files) {
			file.close();
		}
	}
}
