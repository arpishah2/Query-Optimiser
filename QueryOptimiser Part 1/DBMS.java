import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList; 
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;
//import Index.IndexKeyDef;
//import Index.IndexKeyVal;

/**
 * CS 267 - Project - Implements create index, drop index, list table, and
 * exploit the index in select statements.
 */
public class DBMS {
	private static final String COMMAND_FILE_LOC = "Commands.txt";
	private static final String OUTPUT_FILE_LOC = "Output.txt";

	private static final String TABLE_FOLDER_NAME = "tables";
	private static final String TABLE_FILE_EXT = ".tab";
	private static final String INDEX_FILE_EXT = ".idx";

	private DbmsPrinter out;
	private ArrayList<Table> tables;

	public DBMS() {
		tables = new ArrayList<Table>();
	}

	/**
	 * Main method to run the DBMS engine.
	 * 
	 * @param args
	 *            arg[0] is input file, arg[1] is output file.
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		DBMS db = new DBMS();
		db.out = new DbmsPrinter();
		Scanner in = null;
		try {
			// set input file
			if (args.length > 0) {
				in = new Scanner(new File(args[0]));
			} else {
				in = new Scanner(new File(COMMAND_FILE_LOC));
			}

			// set output files
			if (args.length > 1) {
				db.out.addPrinter(args[1]);
			} else {
				db.out.addPrinter(OUTPUT_FILE_LOC);
			}

			// Load data to memory
			db.loadTables();

			// Go through each line in the Command.txt file
			while (in.hasNextLine()) {
				String sql = in.nextLine();
				StringTokenizer tokenizer = new StringTokenizer(sql);
				boolean isUniq= false;
				

				// Evaluate the SQL statement
				if (tokenizer.hasMoreTokens()) {
					String command = tokenizer.nextToken();
					if (command.equalsIgnoreCase("CREATE")) {
						if (tokenizer.hasMoreTokens()) {
							command = tokenizer.nextToken();
							if (command.equalsIgnoreCase("TABLE")) {
								db.createTable(sql, tokenizer);
							} else if (command.equalsIgnoreCase("UNIQUE")) {
								isUniq= true;
								if (command.equalsIgnoreCase("INDEX")) {
									System.out.println("Hello");
								     db.createIndex(sql, tokenizer, true);
								}
								
							}else if(command.equalsIgnoreCase("INDEX")) {
								System.out.println("Hello");
							     db.createIndex(sql, tokenizer, false);
							} else {
								throw new DbmsError("Invalid CREATE " + command
										+ " statement. '" + sql + "'.");
							}
						} else {
							throw new DbmsError("Invalid CREATE statement. '"
									+ sql + "'.");
						}
					} else if (command.equalsIgnoreCase("INSERT")) {
						db.insertInto(sql, tokenizer);
					} else if (command.equalsIgnoreCase("DROP")) {
						if (tokenizer.hasMoreTokens()) {
							command = tokenizer.nextToken();
							if (command.equalsIgnoreCase("TABLE")) {
								db.dropTable(sql, tokenizer);
							} else if (command.equalsIgnoreCase("INDEX")) {
								// TODO your PART 1 code goes here
								db.dropIndex(sql, tokenizer);
							} else {
								throw new DbmsError("Invalid DROP " + command
										+ " statement. '" + sql + "'.");
							}
						} else {
							throw new DbmsError("Invalid DROP statement. '"
									+ sql + "'.");
						}
					} else if (command.equalsIgnoreCase("RUNSTATS")) {

						
						String tablename = tokenizer.nextToken().toUpperCase();
							 db.createRunstats(sql, tokenizer, tablename);
							 
						// TODO replace the table name below with the table name
						// in the command to print the RUNSTATS output
						 
						db.printRunstats(tablename);
					} else if (command.equalsIgnoreCase("SELECT")) {
						// TODO your PART 2 code goes here
					} else if (command.equalsIgnoreCase("--")) {
						// Ignore this command as a comment
					} else if (command.equalsIgnoreCase("COMMIT")) {
						try {
							// Check for ";"
							if (!tokenizer.nextElement().equals(";")) {
								throw new NoSuchElementException();
							}

							// Check if there are more tokens
							if (tokenizer.hasMoreTokens()) {
								throw new NoSuchElementException();
							}

							// Save tables to files
							for (Table table : db.tables) {
								db.storeTableFile(table);
							}
						} catch (NoSuchElementException ex) {
							throw new DbmsError("Invalid COMMIT statement. '"
									+ sql + "'.");
						}
					} else {
						throw new DbmsError("Invalid statement. '" + sql + "'.");
					}
				}
			}

			// Save tables to files
			for (Table table : db.tables) {
				db.storeTableFile(table);
			}
		} catch (DbmsError ex) {
			db.out.println("DBMS ERROR:  " + ex.getMessage());
			ex.printStackTrace();
		} catch (Exception ex) {
			db.out.println("JAVA ERROR:  " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			// clean up
			try {
				in.close();
			} catch (Exception ex) {
			}

			try {
				db.out.cleanup();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Loads tables to memory
	 * 
	 * @throws Exception
	 */
	private void loadTables() throws Exception {
		// Get all the available tables in the "tables" directory
		File tableDir = new File(TABLE_FOLDER_NAME);
		if (tableDir.exists() && tableDir.isDirectory()) {
			for (File tableFile : tableDir.listFiles()) {
				// For each file check if the file extension is ".tab"
				String tableName = tableFile.getName();
				int periodLoc = tableName.lastIndexOf(".");
				String tableFileExt = tableName.substring(tableName
						.lastIndexOf(".") + 1);
				if (tableFileExt.equalsIgnoreCase("tab")) {
					// If it is a ".tab" file, create a table structure
					Table table = new Table(tableName.substring(0, periodLoc));
					Scanner in = new Scanner(tableFile);

					try {
						// Read the file to get Column definitions
						int numCols = Integer.parseInt(in.nextLine());

						for (int i = 0; i < numCols; i++) {
							StringTokenizer tokenizer = new StringTokenizer(
									in.nextLine());
							String name = tokenizer.nextToken();
							String type = tokenizer.nextToken();
							boolean nullable = Boolean.parseBoolean(tokenizer
									.nextToken());
							switch (type.charAt(0)) {
							case 'C':
								table.addColumn(new Column(i + 1, name,
										Column.ColType.CHAR, Integer
												.parseInt(type.substring(1)),
										nullable));
								break;
							case 'I':
								table.addColumn(new Column(i + 1, name,
										Column.ColType.INT, 4, nullable));
								break;
							default:
								break;
							}
						}

						// Read the file for index definitions
						int numIdx = Integer.parseInt(in.nextLine());
						for (int i = 0; i < numIdx; i++) {
							StringTokenizer tokenizer = new StringTokenizer(
									in.nextLine());
							Index index = new Index(tokenizer.nextToken());
							index.setIsUnique(Boolean.parseBoolean(tokenizer
									.nextToken()));

							int idxColPos = 1;
							while (tokenizer.hasMoreTokens()) {
								String colDef = tokenizer.nextToken();
								Index.IndexKeyDef def = index.new IndexKeyDef();
								def.idxColPos = idxColPos;
								def.colId = Integer.parseInt(colDef.substring(
										0, colDef.length() - 1));
								switch (colDef.charAt(colDef.length() - 1)) {
								case 'A':
									def.descOrder = false;
									break;
								case 'D':
									def.descOrder = true;
									break;
								default:
									break;
								}

								index.addIdxKey(def);
							}

							table.addIndex(index);
							loadIndex(table, index);
						}

						// Read the data from the file
						int numRows = Integer.parseInt(in.nextLine());
						for (int i = 0; i < numRows; i++) {
							table.addData(in.nextLine());
						}
					} catch (DbmsError ex) {
						throw ex;
					} catch (Exception ex) {
						throw new DbmsError("Invalid table file format.");
					} finally {
						in.close();
					}
					tables.add(table);
				}
			}
		} else {
			throw new FileNotFoundException(
					"The system cannot find the tables directory specified.");
		}
	}

	/**
	 * Loads specified table to memory
	 * 
	 * @throws DbmsError
	 */
	private void loadIndex(Table table, Index index) throws DbmsError {
		try {
			Scanner in = new Scanner(new File(TABLE_FOLDER_NAME,
					table.getTableName() + index.getIdxName() + INDEX_FILE_EXT));
			String def = in.nextLine();
			String rows = in.nextLine();

			while (in.hasNext()) {
				String line = in.nextLine();
				Index.IndexKeyVal val = index.new IndexKeyVal();
				val.rid = Integer.parseInt(new StringTokenizer(line)
						.nextToken());
				val.value = line.substring(line.indexOf("'") + 1,
						line.lastIndexOf("'"));
				index.addKey(val);
			}
			in.close();
		} catch (Exception ex) {
			throw new DbmsError("Invalid index file format.");
		}
	}

	/**
	 * CREATE TABLE
	 * <table name>
	 * ( <col name> < CHAR ( length ) | INT > <NOT NULL> ) ;
	 * 
	 * @param sql
	 * @param tokenizer
	 * @throws Exception
	 */
	

	
	public static String padRight(String s, int n) {
	     return String.format("%1$-" + n + "s", s);  
	}
	
	public static String padLeft(String s, int n) {
	    return String.format("%1$" + n + "s", s);  
	}
	
	
	
	public boolean isIndexPresent(Table table, Index idx) {
		boolean r = false;
		for (Index i : table.getIndexes()) {
			if (i.getIdxName().equals(idx.getIdxName()))
				r = true;
		}
		return r;
	}
	
	


	
	public static String invertString (String s)
	{
		String atoz = "abcdefghijklmnopqrstuvwxyz";
		String ATOZ = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String result = "";
		char charatnewindex;
		
		for(int i=1; i<=s.length(); i++)
		{
			char x= s.charAt(i-1);
			
			if (Character.isLowerCase(x))
			{
			int y = atoz.indexOf(x);
			int index= y+1;
			int changedindex= 26-index;
			charatnewindex = atoz.charAt(changedindex);
			}
			else
			{
				int y = ATOZ.indexOf(x);
				int index= y+1;
				int changedindex= 26-index;
				if (changedindex == 26)
					changedindex= changedindex - 1;	
				charatnewindex = ATOZ.charAt(changedindex);
			}
			
			result=result+charatnewindex;
		}
		return result;
	}
	
	
	private static Map<Integer, String> sortByComparator(
			Map<Integer, String> unsortMap, final boolean order) {

		List<Entry<Integer, String>> list = new LinkedList<Entry<Integer, String>>(
				unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<Integer, String>>() {
			public int compare(Entry<Integer, String> o1,
					Entry<Integer, String> o2) {
				if (order) {
					return o1.getValue().compareTo(o2.getValue());
				} else {
					return o2.getValue().compareTo(o1.getValue());

				}
			}
		});

		// Maintaining insertion order with the help of LinkedList
		Map<Integer, String> sortedMap = new LinkedHashMap<Integer, String>();
		for (Entry<Integer, String> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}


	
	
	private void createIndex(String sql, StringTokenizer tokenizer, Boolean isUnique)
			throws Exception {
	
	try
	{
				Boolean uniqindx = isUnique;
		
		// get idxname
				String indexName = tokenizer.nextToken().toUpperCase();
				

				// escape ON keyword
				String on = tokenizer.nextToken().toUpperCase();

				// get tablename
				String tableName = tokenizer.nextToken().toUpperCase();
				
				//verify if table exists
				boolean isTableExist = false;
				for (Table tab : tables) {
					if (tab.getTableName().equalsIgnoreCase(tableName)) {
						isTableExist = true;
						break;
					}
				}
				
				for (Table tab : tables) {
					for (Index ind :tab.getIndexes())
					{
						if (ind.getIdxName().equalsIgnoreCase(indexName))
							throw new DbmsError("Index " + indexName
									+ "already exists. '");
					}
				}
				
				//exit if table does not exist
				if(!isTableExist){
					System.out.println("\nError: Table "+ tableName + " does not exist!!!");
					System.exit(0);
				}
				
				Index index = new Index(indexName);
	

				// get table object from loaded tables array
				Table table = null;
				for (Table tab : tables) {
					if (tab.getTableName().equals(tableName)) {
						table = tab;
					}
				}

				// escape '(' keyword
				String temp = tokenizer.nextToken().toUpperCase();
				
				// parse columns and make IdxDef objects for Index object
				int colsPos = 1;
				boolean escapeToken = false;
				String columnName = "";

				while (tokenizer.hasMoreTokens()) {

					// Read token
					if (!escapeToken)
						columnName = tokenizer.nextToken();

					//create new IndexDef object
					Index.IndexKeyDef def = index.new IndexKeyDef();

					//System.out.println("\n in create idx col = " + columnName);

					//If ')'token, then exit the parsing loop
					if (columnName.equals(")"))
						break;
					else if (columnName.equals(",")) {  //parsethe next token by setting escapeToken false
						escapeToken = false;
						continue;
					}

					//Set IndexColPos and then increment colsPos by 1 for dealing with next IndexColPos
					def.idxColPos = colsPos++;

					
					boolean isIdxColPresentOnTable = false;

					// get colId for current columnname
					for (Column col : table.getColumns()) {
						//System.out.println("\ncol = " + columnName);
						if (col.getColName().equalsIgnoreCase(columnName)) {

							isIdxColPresentOnTable = true;
							//Set ColId
							def.colId = col.getColId();
							//System.out.println("\ncol = " + columnName + " and id = "
							//		+ def.colId);
							break;
						}
					}

					// Exit if column mentioned in index does not exist
					if (!isIdxColPresentOnTable) {
						System.out.println("\nError: Column " + columnName
								+ " does not exist on Table "
								+ table.getTableName());
						System.exit(0);
					}

					// set ORDER i.e. ASC / DESC
					if (tokenizer.hasMoreTokens()) {
						String order = tokenizer.nextToken();
						if (order.equalsIgnoreCase("DESC")) {
							def.descOrder = true;
							//System.out.println("DESC it is for " + columnName);
							escapeToken = false;
						} else {
							def.descOrder = false;
							columnName = order;
							//System.out.println("in ASC and nxt col = " + columnName);
							escapeToken = true;
						}
					}

					//Add IndexDef object to Index object
					index.addIdxKey(def);
				}
		
				table.addIndex(index);
				
				//Index Key Val: Implementation: 
					ArrayList<String> data1= table.getData();
					ArrayList<Index.IndexKeyDef> idef= index.getIdxKey();
					String data[] = new String[data1.size()];
					Map<Integer, String> unsortMap = new HashMap<Integer, String>();
					
					data1.toArray(data);
					int tcard= data1.size();
					String arr[]=new String[table.getNumColumns()+1];
					Index.IndexKeyVal kvalue = index.new IndexKeyVal();
					
					int rid[] = new int[tcard];
					
					//System.out.println("tcard is "+tcard);
					
					for(int i=0; i<tcard; i++)
					{
						//System.out.println("Data is"+ data[i]);
						StringTokenizer tok = new StringTokenizer(data[i]);
						
						for(int j=0; j< index.getIdxKey().size()+1; j++)
						{
							arr[j]=tok.nextToken();
							//System.out.println("Value is" +arr[j]);
						}
						
						rid[i] = Integer.parseInt(arr[0]);
						//System.out.println("rid is"+ rid[i]);
						
						String key = "";
						int collen=0;
						String valofcol="";
						
						for (Index.IndexKeyDef indxdef : idef)
						{
							int colid=indxdef.colId;
							int colpos = indxdef.idxColPos;
							boolean ord = indxdef.descOrder;
							
							Column.ColType coltype = Column.ColType.INT;
							//System.out.println(colid + " " + colpos + " " + ord);
							
							for (Column cols : table.getColumns())
							{
								if(cols.getColId() == colid)
								{
									//System.out.println("----------");
									collen= cols.getColLength();	
									coltype= cols.getColType();	
									
									//System.out.println("Collen:" +collen + "ColType " + coltype);
									
									if (ord == false)
									{
										if(coltype == Column.ColType.INT )
										{
											if (arr[colpos].contains("-"))
											{
												valofcol = arr[colpos];
												valofcol= padLeft(valofcol, 10);
											}
											else
											{
												valofcol = arr[colpos];
												valofcol= String.format("%010d", Long.parseLong(valofcol));
												//System.out.println("Value in case of int"+valofcol);
											}
										}
										
										else
										{
											valofcol = arr[colpos];
											valofcol= padRight(valofcol, collen);
											//System.out.println("Value in case of char"+valofcol);
											
										}
											
									}
									else if (ord == true)
									{
										
										if(coltype == Column.ColType.INT)
										{
											valofcol = arr[colpos];
											long x = 9999999999L;
											long valueinint;
											
											if (valofcol.contains("-"))
											{
												valofcol="|";
												valofcol= padLeft(valofcol, 10);
												//out.println(valofcol);
												
											}
											else
											{
												long y= Long.parseLong(valofcol);
												y = x-y;
												valofcol = String.valueOf(y);
												//System.out.println("value of int inverted is"+ valofcol);	
												//valofcol = String.format("%010d", valofcol);
												//System.out.println("value of int inverted is"+ valofcol);	
											}
											
											//out.println("Value of index col after padding and inverting is : "+ valofcol);
										}
										else
										{
											valofcol = arr[colpos];
											
											if (valofcol.contains("-"))
											{
												valofcol = "|";
												valofcol= padRight(valofcol, collen);
												//System.out.println("Value in case of char desc"+valofcol);
												
											}
											else
											{
											String test = invertString(valofcol);
											valofcol= padRight(test, collen);
											//System.out.println("Value in case of char desc"+valofcol+"o");
											}
										}

									}

								}

							}
							
							key = key + valofcol;
							
							
						}
						
						//System.out.println("Key is"+ key);
						unsortMap.put(rid[i], key);
						
						
					}
					
					//Display values of unsorted map
					Iterator<Integer> keySetIterator = unsortMap.keySet().iterator();

					while(keySetIterator.hasNext()){
					  Integer key = keySetIterator.next();
					  System.out.println("key: " + key + " value: " +unsortMap.get(key));
					}
					
					//Sort unsortted map
					
					Map<Integer, String> sortedMapAsc = new HashMap<Integer, String>();

					//SORT the map i.e. Index Contents
					sortedMapAsc = sortByComparator(unsortMap, true);

					
					
					for (Map.Entry<Integer, String> entry : sortedMapAsc.entrySet()) {
						int rid1 = entry.getKey();
						String value = entry.getValue();
						Index.IndexKeyVal key_val = index.new IndexKeyVal();
						key_val.rid = rid1;
						key_val.value = value;

						index.addKey(key_val);
					}
					
				
				//Dispalysorted map
					
					Iterator<Integer> keySetIterator1 = sortedMapAsc.keySet().iterator();

					while(keySetIterator1.hasNext()){
					  Integer key = keySetIterator1.next();
					  System.out.println("key: " + key + " value: " +sortedMapAsc.get(key));
					}

		
	}
	catch (NoSuchElementException ex) {
		throw new DbmsError("Invalid CREATE INDEX statement. '" + sql
				+ "'.");
	}
		
		
	
	}
	
	private void createTable(String sql, StringTokenizer tokenizer)
			throws Exception {
		try {
			// Check the table name
			String tok = tokenizer.nextToken().toUpperCase();
			if (Character.isAlphabetic(tok.charAt(0))) {
				// Check if the table already exists
				for (Table tab : tables) {
					if (tab.getTableName().equals(tok)) {
						throw new DbmsError("Table " + tok
								+ "already exists. '" + sql + "'.");
					}
				}

				// Create a table instance to store data in memory
				Table table = new Table(tok.toUpperCase());

				// Check for '('
				tok = tokenizer.nextToken();
				if (tok.equals("(")) {
					// Look through the column definitions and add them to the
					// table in memory
					boolean done = false;
					int colId = 1;
					while (!done) {
						tok = tokenizer.nextToken();
						if (Character.isAlphabetic(tok.charAt(0))) {
							String colName = tok;
							Column.ColType colType = Column.ColType.INT;
							int colLength = 4;
							boolean nullable = true;

							tok = tokenizer.nextToken();
							if (tok.equalsIgnoreCase("INT")) {
								// use the default Column.ColType and colLength

								// Look for NOT NULL or ',' or ')'
								tok = tokenizer.nextToken();
								if (tok.equalsIgnoreCase("NOT")) {
									// look for NULL after NOT
									tok = tokenizer.nextToken();
									if (tok.equalsIgnoreCase("NULL")) {
										nullable = false;
									} else {
										throw new NoSuchElementException();
									}

									tok = tokenizer.nextToken();
									if (tok.equals(",")) {
										// Continue to the next column
									} else if (tok.equalsIgnoreCase(")")) {
										done = true;
									} else {
										throw new NoSuchElementException();
									}
								} else if (tok.equalsIgnoreCase(",")) {
									// Continue to the next column
								} else if (tok.equalsIgnoreCase(")")) {
									done = true;
								} else {
									throw new NoSuchElementException();
								}
							} else if (tok.equalsIgnoreCase("CHAR")) {
								colType = Column.ColType.CHAR;

								// Look for column length
								tok = tokenizer.nextToken();
								if (tok.equals("(")) {
									tok = tokenizer.nextToken();
									try {
										colLength = Integer.parseInt(tok);
									} catch (NumberFormatException ex) {
										throw new DbmsError(
												"Invalid table column length for "
														+ colName + ". '" + sql
														+ "'.");
									}

									// Check for the closing ')'
									tok = tokenizer.nextToken();
									if (!tok.equals(")")) {
										throw new DbmsError(
												"Invalid table column definition for "
														+ colName + ". '" + sql
														+ "'.");
									}

									// Look for NOT NULL or ',' or ')'
									tok = tokenizer.nextToken();
									if (tok.equalsIgnoreCase("NOT")) {
										// Look for NULL after NOT
										tok = tokenizer.nextToken();
										if (tok.equalsIgnoreCase("NULL")) {
											nullable = false;

											tok = tokenizer.nextToken();
											if (tok.equals(",")) {
												// Continue to the next column
											} else if (tok
													.equalsIgnoreCase(")")) {
												done = true;
											} else {
												throw new NoSuchElementException();
											}
										} else {
											throw new NoSuchElementException();
										}
									} else if (tok.equalsIgnoreCase(",")) {
										// Continue to the next column
									} else if (tok.equalsIgnoreCase(")")) {
										done = true;
									} else {
										throw new NoSuchElementException();
									}
								} else {
									throw new DbmsError(
											"Invalid table column definition for "
													+ colName + ". '" + sql
													+ "'.");
								}
							} else {
								throw new NoSuchElementException();
							}

							// Everything is ok. Add the column to the table
							table.addColumn(new Column(colId, colName, colType,
									colLength, nullable));
							colId++;
						} else {
							// if(colId == 1) {
							throw new DbmsError(
									"Invalid table column identifier " + tok
											+ ". '" + sql + "'.");
							// }
						}
					}

					// Check for the semicolon
					tok = tokenizer.nextToken();
					if (!tok.equals(";")) {
						throw new NoSuchElementException();
					}

					// Check if there are more tokens
					if (tokenizer.hasMoreTokens()) {
						throw new NoSuchElementException();
					}

					if (table.getNumColumns() == 0) {
						throw new DbmsError(
								"No column descriptions specified. '" + sql
										+ "'.");
					}

					// The table is stored into memory when this program exists.
					tables.add(table);

					out.println("Table " + table.getTableName()
							+ " was created.");
				} else {
					throw new NoSuchElementException();
				}
			} else {
				throw new DbmsError("Invalid table identifier " + tok + ". '"
						+ sql + "'.");
			}
		} catch (NoSuchElementException ex) {
			throw new DbmsError("Invalid CREATE TABLE statement. '" + sql
					+ "'.");
		}
	}

	/**
	 * INSERT INTO
	 * <table name>
	 * VALUES ( val1 , val2, .... ) ;
	 * 
	 * @param sql
	 * @param tokenizer
	 * @throws Exception
	 */
	private void insertInto(String sql, StringTokenizer tokenizer)
			throws Exception {
		try {
			String tok = tokenizer.nextToken();
			if (tok.equalsIgnoreCase("INTO")) {
				tok = tokenizer.nextToken().trim().toUpperCase();
				Table table = null;
				for (Table tab : tables) {
					if (tab.getTableName().equals(tok)) {
						table = tab;
						break;
					}
				}

				if (table == null) {
					throw new DbmsError("Table " + tok + " does not exist.");
				}

				tok = tokenizer.nextToken();
				if (tok.equalsIgnoreCase("VALUES")) {
					tok = tokenizer.nextToken();
					if (tok.equalsIgnoreCase("(")) {
						tok = tokenizer.nextToken();
						String values = String.format("%3s", table.getData()
								.size() + 1)
								+ " ";
						int colId = 0;
						boolean done = false;
						while (!done) {
							if (tok.equals(")")) {
								done = true;
								break;
							} else if (tok.equals(",")) {
								// Continue to the next value
							} else {
								if (colId == table.getNumColumns()) {
									throw new DbmsError(
											"Invalid number of values were given.");
								}

								Column col = table.getColumns().get(colId);

								if (tok.equals("-") && !col.isColNullable()) {
									throw new DbmsError(
											"A NOT NULL column cannot have null. '"
													+ sql + "'.");
								}

								if (col.getColType() == Column.ColType.INT) {
									try {
										int temp = Integer.parseInt(tok);
									} catch (Exception ex) {
										throw new DbmsError(
												"An INT column cannot hold a CHAR. '"
														+ sql + "'.");
									}

									tok = String.format("%10s", tok.trim());
								} else if (col.getColType() == Column.ColType.CHAR) {
									int length = tok.length();
									if (length > col.getColLength()) {
										throw new DbmsError(
												"A CHAR column cannot exceede its length. '"
														+ sql + "'.");
									}

									tok = String.format(
											"%-" + col.getColLength() + "s",
											tok.trim());
								}

								values += tok + " ";
								colId++;
							}
							tok = tokenizer.nextToken().trim();
						}

						if (colId != table.getNumColumns()) {
							throw new DbmsError(
									"Invalid number of values were given.");
						}

						// Check for the semicolon
						tok = tokenizer.nextToken();
						if (!tok.equals(";")) {
							throw new NoSuchElementException();
						}

						// Check if there are more tokens
						if (tokenizer.hasMoreTokens()) {
							throw new NoSuchElementException();
						}

						// insert the value to table
						table.addData(values);
						out.println("One line was saved to the table. "
								+ table.getTableName() + ": " + values);
					} else {
						throw new NoSuchElementException();
					}
				} else {
					throw new NoSuchElementException();
				}
			} else {
				throw new NoSuchElementException();
			}
		} catch (NoSuchElementException ex) {
			throw new DbmsError("Invalid INSERT INTO statement. '" + sql + "'.");
		}
	}
	
	
	
	private void dropIndex(String sql, StringTokenizer tokenizer)
			throws Exception {
		try {
			// Get table name
			String indexName = tokenizer.nextToken();

			// Check for the semicolon
			String tok = tokenizer.nextToken();
			if (!tok.equals(";")) {
				throw new NoSuchElementException();
			}

			// Check if there are more tokens
			if (tokenizer.hasMoreTokens()) {
				throw new NoSuchElementException();
			}

			boolean dropped = false;
			String tname="";
			
			for (Table table : tables) {
				
				ArrayList<Index> indexes = table.getIndexes();
				
				for (Index inx : indexes)
				{
					String name = inx.getIdxName();
					if (name.equalsIgnoreCase(indexName))
					{	tname = table.getTableName();
						inx.delete= true;
						dropped = true;
						
						int noofindx = table.getNumIndexes();
						noofindx = noofindx-1;
						table.setNumIndexes(noofindx);
						
						break;
					}
				}
			}
			
			if (dropped) {
				out.println("Index " + indexName + " on " + tname + "was dropped.");
			} else {
				throw new DbmsError("Index " + indexName + "does not exist. '" + sql + "'."); 
			}
		} catch (NoSuchElementException ex) {
			throw new DbmsError("Invalid DROP TABLE statement. '" + sql + "'.");
		}

	}

	
	

	/**
	 * DROP TABLE
	 * <table name>
	 * ;
	 * 
	 * @param sql
	 * @param tokenizer
	 * @throws Exception
	 */
	private void dropTable(String sql, StringTokenizer tokenizer)
			throws Exception {
		try {
			// Get table name
			String tableName = tokenizer.nextToken();

			// Check for the semicolon
			String tok = tokenizer.nextToken();
			if (!tok.equals(";")) {
				throw new NoSuchElementException();
			}

			// Check if there are more tokens
			if (tokenizer.hasMoreTokens()) {
				throw new NoSuchElementException();
			}

			// Delete the table if everything is ok
			boolean dropped = false;
			for (Table table : tables) {
				if (table.getTableName().equalsIgnoreCase(tableName)) {
					table.delete = true;
					dropped = true;
					break;
				}
			}

			if (dropped) {
				out.println("Table " + tableName + " was dropped.");
			} else {
				throw new DbmsError("Table " + tableName + "does not exist. '" + sql + "'."); 
			}
		} catch (NoSuchElementException ex) {
			throw new DbmsError("Invalid DROP TABLE statement. '" + sql + "'.");
		}

	}

	
public static String[] maxMinCard(String[] arr){ 
		
		String maxValue="";
		String minValue="";
		
		for(int i=0 ;i<arr.length; i++)
		{ 
			int l=arr.length-1;
			int n=0;
			 maxValue=arr[l];
			 minValue=arr[0];
			
				for(int m=1;m<l;m++)
				{
					if (maxValue.contains("-"))
					maxValue = arr[l-m];
				}
				
				for(int m=1;m<l;m++)
				{
					if (minValue.contains("-"))
					minValue = arr[n+m];
				}
				
			}
		
		String[] ret = {maxValue,minValue};
		return ret;
	}
	
	public static String[] getDistinct(String[] input) {

	    Set<String> distinct = new HashSet<String>();
	    for(String element : input) {
	        distinct.add(element);
	    }

	    return distinct.toArray(new String[0]);
	}
	
	private void createRunstats(String sql, StringTokenizer tokenizer, String tabl) throws Exception
	{
		try{
					
		//String tablename = tokenizer.nextToken().toUpperCase();
			String tablename = tabl;
			// System.out.println("Table is"+ tablename);
			
			if (Character.isAlphabetic(tablename.charAt(0))) {
		
				for (Table tab : tables) {
					if (tab.getTableName().equals(tablename)) {
						
						ArrayList<String> data1= tab.getData();
						String data[] = new String[data1.size()];
						data1.toArray(data);
						
						int tcard=data1.size();
						//System.out.println("Tsble card"+ tcard);
						
						String content[]=new String[data1.size()];
						StringTokenizer tokenizer1;
						String command;
						String colval;
						
						ArrayList<Column> tcols = tab.getColumns();
						
						for(Column col : tcols)
						{
							int colid = col.getColId();
							//System.out.println("Column id is"+ colid);
							
							for(int i=0; i<data1.size(); i++)
							{
								//out.println("Inside loop");
								//System.out.println("Data is"+ data[i]);
								tokenizer1 = new StringTokenizer(data[i]);
								command= tokenizer1.nextToken();
								
								for(int j = 0; j<colid; j++)
								{
									command= tokenizer1.nextToken();
								}
								
								content[i] = command;					
							}
							
							Arrays.sort(content);
							String res1[] = new String[2];
							String max,min;
							
							res1=maxMinCard(content);
							max=res1[0];
							min=res1[1];
							
							String[] distinct = getDistinct(content);
							
							int len=getDistinct(content).length;
							List list1 = Arrays.asList(distinct);
							if (list1.contains("-"))
							{
								len=len-1;
							}
							
							int colcard = len;
							
							col.setHiKey(max);
							col.setLoKey(min);
							col.setColCard(colcard);
							
						}
			
					}
					
			}
		}
		
		else {
			throw new DbmsError("Invalid table identifier " + tablename + ". '"
					+ sql + "'.");
		}
		}
		
		catch (NoSuchElementException ex) {
			 throw new DbmsError("Invalid statement. '" + sql
					+ "'."); 
		}
	}


	
	private void printRunstats(String tableName) {
		for (Table table : tables) {
			if (table.getTableName().equals(tableName)) {
				out.println("TABLE CARDINALITY: " + table.getTableCard());
				for (Column column : table.getColumns()) {
					out.println(column.getColName());
					out.println("\tCOLUMN CARDINALITY: " + column.getColCard());
					out.println("\tCOLUMN HIGH KEY: " + column.getHiKey());
					out.println("\tCOLUMN LOW KEY: " + column.getLoKey());
				}
				break;
			}
		}
	}

	private void storeTableFile(Table table) throws FileNotFoundException {
		File tableFile = new File(TABLE_FOLDER_NAME, table.getTableName()
				+ TABLE_FILE_EXT);

		// Delete the file if it was marked for deletion
		if (table.delete) {
			try {
				tableFile.delete();
			} catch (Exception ex) {
				out.println("Unable to delete table file for "
						+ table.getTableName() + ".");
			}
		} else {
			// Create the table file writer
			PrintWriter out = new PrintWriter(tableFile);

			// Write the column descriptors
			out.println(table.getNumColumns());
			for (Column col : table.getColumns()) {
				if (col.getColType() == Column.ColType.INT) {
					out.println(col.getColName() + " I " + col.isColNullable());
				} else if (col.getColType() == Column.ColType.CHAR) {
					out.println(col.getColName() + " C" + col.getColLength()
							+ " " + col.isColNullable());
				}
			}

			// Write the index info
			out.println(table.getNumIndexes());
			for (Index index : table.getIndexes()) {
				if(!index.delete) {
					String idxInfo = index.getIdxName() + " " + index.getIsUnique()
							+ " ";

					for (Index.IndexKeyDef def : index.getIdxKey()) {
						idxInfo += def.colId;
						if (def.descOrder) {
							idxInfo += "D ";
						} else {
							idxInfo += "A ";
						}
					}
					out.println(idxInfo);
				}
			}

			// Write the rows of data
			out.println(table.getData().size());
			for (String data : table.getData()) {
				out.println(data);
			}

			out.flush();
			out.close();
		}

		// Save indexes to file
		for (Index index : table.getIndexes()) {

			File indexFile = new File(TABLE_FOLDER_NAME, table.getTableName()
					+ index.getIdxName() + INDEX_FILE_EXT);

			// Delete the file if it was marked for deletion
			if (index.delete) {
				try {
					indexFile.delete();
				} catch (Exception ex) {
					out.println("Unable to delete index file for "
							+ indexFile.getName() + ".");
				}
			} else {
				PrintWriter out = new PrintWriter(indexFile);
				String idxInfo = index.getIdxName() + " " + index.getIsUnique()
						+ " ";

				// Write index definition
				for (Index.IndexKeyDef def : index.getIdxKey()) {
					idxInfo += def.colId;
					if (def.descOrder) {
						idxInfo += "D ";
					} else {
						idxInfo += "A ";
					}
				}
				out.println(idxInfo);

				// Write index keys
				out.println(index.getKeys().size());
				for (Index.IndexKeyVal key : index.getKeys()) {
					String rid = String.format("%3s", key.rid);
					out.println(rid + " '" + key.value + "'");
				}

				out.flush();
				out.close();

			}
		}
	}
}
