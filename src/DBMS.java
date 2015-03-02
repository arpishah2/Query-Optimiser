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
							} 
							else if (command.equalsIgnoreCase("UNIQUE")) {
								isUniq= true;
								if (command.equalsIgnoreCase("INDEX")) {
								//	System.out.println("Hello");
								     db.createIndex(sql, tokenizer, true);
								}
							}
								else if (command.equalsIgnoreCase("INDEX")) {
								// TODO your PART 1 code goes here
							//	System.out.println("Hello");
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
								db.dropIndex(sql, tokenizer);
								// TODO your PART 1 code goes here
							} else {
								throw new DbmsError("Invalid DROP " + command
										+ " statement. '" + sql + "'.");
							}
						} else {
							throw new DbmsError("Invalid DROP statement. '"
									+ sql + "'.");
						}
					} else if (command.equalsIgnoreCase("RUNSTATS")) {
						// TODO your PART 1 code goes here
						
						// TODO replace the table name below with the table name
						// in the command to print the RUNSTATS output
						//db.printRunstats("T1");
						
						 String tablename = tokenizer.nextToken().toUpperCase();
						 db.createRunstats(sql, tokenizer, tablename);
						 
					// TODO replace the table name below with the table name
					// in the command to print the RUNSTATS output
					 
					db.printRunstats(tablename);
						
						
					} else if (command.equalsIgnoreCase("SELECT")) {
						
						//db.indexinfo();
						db.selectStatement(sql, tokenizer);
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
								idxColPos++;
							}

							table.addIndex(index);
							loadIndex(table, index);
						}

						// Read the data from the file
						int numRows = Integer.parseInt(in.nextLine());
						for (int i = 0; i < numRows; i++) {
							table.addData(in.nextLine());
						}
						
						// Read RUNSTATS from the file
						while(in.hasNextLine()) {
							String line = in.nextLine();
							StringTokenizer toks = new StringTokenizer(line);
							if(toks.nextToken().equals("STATS")) {
								String stats = toks.nextToken();
								if(stats.equals("TABCARD")) {
									table.setTableCard(Integer.parseInt(toks.nextToken()));
								} else if (stats.equals("COLCARD")) {
									Column col = table.getColumns().get(Integer.parseInt(toks.nextToken()));
									col.setColCard(Integer.parseInt(toks.nextToken()));
									col.setHiKey(toks.nextToken());
									col.setLoKey(toks.nextToken());
								} else {
									throw new DbmsError("Invalid STATS.");
								}
							} else {
								throw new DbmsError("Invalid STATS.");
							}
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
	 * Loads specified index to memory
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
	
	private void createTable(String sql, StringTokenizer tokenizer)
			throws Exception {
		try {
			// Check the table name
			String tok = tokenizer.nextToken().toUpperCase();
			if (Character.isAlphabetic(tok.charAt(0))) {
				// Check if the table already exists
				for (Table tab : tables) {
					if (tab.getTableName().equals(tok) && !tab.delete) {
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
	
	
	
	

	
	//Create Index

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
										if(!tok.equals("-")) {
											int temp = Integer.parseInt(tok);
										}
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
			throw new DbmsError("Invalid DROP INDEX statement. '" + sql + "'.");
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
	
	
	public static String[] maxMinCard(String[] arr)
	{ 
	
	String maxValue="-";
	String minValue="-";
	
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
	
	
	
	public static ArrayList<Predicate> predTable(ArrayList<WhereInfo> whrInfoArr)
	{
		ArrayList<Predicate> predList = new ArrayList<Predicate>();
		
		for(int i=0; i<whrInfoArr.size(); i++)
		{
			//System.out.println("Whr info arr size is" + whrInfoArr.size());
			//System.out.println(" i is:"+i);
			
			WhereInfo w = whrInfoArr.get(i);
			Predicate p = new Predicate();
			int joinpos;
			
			
			if(w.getisShouldSequence() == false)
			{
				//i++;
				p.setShouldSequence(false);
				//System.out.println("Must not take part in sequence");
				//p.setSequence(0);
				//continue;
			}
			
			
			String op = w.getOp();
			if(op.equalsIgnoreCase("IN"))
			{
				p.setInList(true);
			}
			else
			{
				p.setInList(false);
			}
			
		
			
			
			Boolean join = w.getIsJoinPred();
			if(join == true)
			{
				p.setJoin(true);
				
			}
			else
			{
				p.setJoin(false);
			}
			
			
			String type = w.getOp();
			if(type.equals("=") && w.getIsConvInList()== false)			//change Q18
			{
				p.setType('E');
			}
			else if(type.equals("=") && w.getIsConvInList()== true)
			{
				p.setType('I');
			}
			else if(type.equals("<") || type.equals(">"))
			{
				p.setType('R');
			}
			else if(type.equalsIgnoreCase("IN"))
			{
				p.setType('I');
			}
			
			
			if(w.getisInlistOneElem() == true)
			{
				String description = w.getTNameWhere()+"."+w.getCNameWhere()+" = "+w.getValue();
				description = description.replaceAll("[()]","");
				//System.out.println("Description is" + description);
				p.setDescription(description);
				p.setType('E');
			}
			
			if(w.getIsConvInList() == true)
			{	String description1 = w.getWhereText().replace("OR", ",");
				p.setInList(true);
				description1= description1.substring(description1.indexOf("=")).replaceAll(" = ", "").replace(w.getTNameWhere()+"."+w.getCNameWhere(),"").replaceAll("=", "IN (");
				p.setDescription(w.getTNameWhere()+"."+w.getCNameWhere()+" "+description1+")");
		
			}
			
			if(w.isAddPredTCP() == true)
			{
				p.setDescription("TCP");
			}
			
			
			int c1 = w.getCard();
			p.setCard1(c1);
			
			if(join == true)
			{
				//System.out.println("i is");
				//System.out.println("setting c2");
				
				if(i< whrInfoArr.size()-1)
				{
				//System.out.println("hi");
				
				int c2=whrInfoArr.get(i+1).getCard();
				//System.out.println("c2 card is"+c2);
				p.setCard2(c2);
				
				}
			}
			
			float f1=w.getFf();
			p.setFf1(f1);
			
			if(join == true)
			{
				if(i< whrInfoArr.size()-1)
				{
				float ff2 = whrInfoArr.get(i+1).getFf();
				p.setFf2(ff2);
				
				}
				i=i+1;
			}
			
			//Change sequence
			p.setSequence(0);
			
			String text = w.getWhereText();
			p.setText(text);
		
			
			//Add to pred array list
			predList.add(p);
		}
		
		return predList;
	}
	
	
	public static int getIndPosInAtoZ (String s)
	{

		String atoz = "abcdefghijklmnopqrstuvwxyz";
		String ATOZ = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int index[]= new int[2];
		int value=0;
		
		for(int i=0; i<2; i++)
		{
			char x= s.charAt(i);
			
			if (Character.isLowerCase(x))
			{
			int y = atoz.indexOf(x);
			index[i]= y+1;
			}
			else
			{
			int y = ATOZ.indexOf(x);
			index[i]= y+1;	
			}
		}
		
		value=index[0]*26+index[1];
		//System.out.println("Computed val is"+value);
		return value;
	}
	
	
	
	
	private void selectStatement(String sql, StringTokenizer tokenizer) throws Exception
	{
		try {
			Boolean moreCols = true;
			String completeColName = "",completeColNameWhere = "",completeColNameOrder="";
			String tabName = "",tabNameWhere = "",tabNameOrder = "";
			String colName = "",colNameWhere="",colNameOrder="";
			int dotIndex = 0, dotIndexWhere = 0, dotIndexOrder = 0;
			ArrayList<String> selectTabName = new ArrayList<String>();
			ArrayList<String> selectColName = new ArrayList<String>();
			ArrayList<OrderByInfo> ordInfoArr = new ArrayList<OrderByInfo>();
			ArrayList<WhereInfo> whrInfoArr = new ArrayList<WhereInfo>();
			ArrayList<FromInfo> fromTables = new ArrayList<FromInfo>();
			ArrayList<SelectInfo> selInfoArr = new ArrayList<SelectInfo>();
			String comma = "";
			String token1 = "";
			Boolean hasMoreTables = true; 
			Boolean hasMorePredicate = true;
			Boolean hasMoreOrderBy = true;
			String LeftDiffPred = "";
			
			
			while (moreCols) {
				
				SelectInfo selinfo = new SelectInfo();
				//out.println("Inside while");
				completeColName = tokenizer.nextToken().toUpperCase();
				dotIndex = completeColName.indexOf(".");
	
				if(dotIndex == -1)
				{
				//System.out.println("No dot in token");
				LeftDiffPred = completeColName;
				//System.out.println("diff pred");
				
				}
				
				tabName = completeColName.substring(0, dotIndex);
				colName = completeColName.substring(dotIndex+1);
				
				Boolean ifexistTab = false;
				Boolean ifexistCol = false;
				
				for (Table tab : tables) {
					if (tab.getTableName().equals (tabName)) {
					//	out.println("1");
						
						ifexistTab = true;
						
						for (Column cols : tab.getColumns()) {
						//	out.println("2");
							if (cols.getColName().equalsIgnoreCase(colName)) {
							//	out.println("3");
								//System.out.println("Column exists.");	
								ifexistCol = true;
							}
							
						}
						
					}
						
				}
				
				
				if(ifexistTab == false)
				{
					throw new DbmsError("Invalid table name."+ tabName +"Table does not exist. Query is" + sql+ "'.");
				}
				if(ifexistCol == false)
				{
					throw new DbmsError("Invalid column name" +colName+ " Column does not exist. Query is" + sql+ "'.");
				}
				
				selinfo.setSelText(completeColName);
				selinfo.setCNameSel(colName);
				selinfo.setTNameSel(tabName);
				selinfo.setSelIndxPos(tables);
				// add select obj info to arraylist
				selInfoArr.add(selinfo);
				
			
			comma = tokenizer.nextToken();
			
			if (comma.equalsIgnoreCase(",")) {
				continue;
			}
			
			else if (comma.equalsIgnoreCase("FROM")) {
				moreCols = false;
			}
			
			else {
				throw new DbmsError ("Wrong query");
			}
	
		}
			
			//out.println("Printing select object");
			
			for (SelectInfo s1: selInfoArr)
			{
				//System.out.printf("%s,",s1.getTNameSel());
				//System.out.printf("%s,",s1.getCNameSel());
				//System.out.printf("%s,",s1.getSelText());
				//System.out.println(s1.getSelIndxPos());
				
			}
			
			token1=comma;
			//System.out.println("\n From Parsed");
			
			while (hasMoreTables) {
			String tok = tokenizer.nextToken().toUpperCase();
			if (Character.isAlphabetic(tok.charAt(0))) {
				
				for (Table tab : tables) {
					if (tab.getTableName().equalsIgnoreCase(tok)) {
						
						FromInfo fromTab = new FromInfo();
						fromTab.setTNameFrom(tok);
	
				//		out.println("Table Added");
					//	out.println(tok);
						fromTables.add(fromTab);
						
					}
				}
				
				for (FromInfo f: fromTables)
				{
					// System.out.printf("%s ",f.getTNameFrom());
				}
				
				
				comma = tokenizer.nextToken();
				
				if (comma.equalsIgnoreCase(",")) {
					continue;
				}
				
				else if (comma.equalsIgnoreCase("WHERE") || comma.equalsIgnoreCase("ORDER") || comma.equalsIgnoreCase(";")) {
					hasMoreTables = false;
					//break;
				}
				
				else {
					throw new DbmsError ("Wrong query");
				}	
			
		}
		}
			token1 = comma;
			Boolean isOr = false;
			Boolean isAnd = false;
			
			
			if (token1.equalsIgnoreCase("WHERE")) {
				
				//System.out.println("In Where Clause...............................");
				while(hasMorePredicate)
				{
				completeColNameWhere = tokenizer.nextToken().toUpperCase();
				dotIndexWhere = completeColNameWhere.indexOf(".");
				System.out.println();
				
				WhereInfo objWhere = new WhereInfo();
				
				if(dotIndexWhere != -1)
				{
				tabNameWhere = completeColNameWhere.substring(0, dotIndexWhere);
				//out.println("TABLE IN WHERE is"+ tabNameWhere);
				colNameWhere = completeColNameWhere.substring(dotIndexWhere+1);
				//out.println("column name is"+ completeColNameWhere);
				}
				else
				{
					tabNameWhere = completeColNameWhere;
					colNameWhere = "";
					objWhere.setDiffPred(true);
				}
				
				
				Boolean ifexistTab = false;
				Boolean ifexistCol = false;
				
				String hikey="",lokey="";
				int colcard=0;
				String hikey1="",lokey1="";
				int colcard1=0;
				float ff=1;	
				
				if(dotIndexWhere != -1)
				{
				for (Table tab : tables) {
					if (tab.getTableName().equals (tabNameWhere)) {
					//	out.println("1");
						
						ifexistTab = true;
						
						for (Column cols : tab.getColumns()) {
						//	out.println("2");
							
							if (cols.getColName().equalsIgnoreCase(colNameWhere)) {
								
							//	out.println("3");
								//System.out.println("Column exists.");		
								ifexistCol = true;
								
								hikey=cols.getHiKey();
								lokey=cols.getLoKey();
								colcard=cols.getColCard();
								
							}
							
						}
						
					}
						
				}
				
				
				if(ifexistTab == false)
				{
					throw new DbmsError("Invalid table name."+ tabName +"Table does not exist. Query is" + sql+ "'.");
				}
				if(ifexistCol == false)
				{
					throw new DbmsError("Invalid column name" +colName+ " Column does not exist. Query is" + sql+ "'.");
				}
				
				
				}
				
				WhereInfo objWhereJoin = new WhereInfo();
				WhereInfo addPredTCP = new WhereInfo(); 
				Boolean newTCP = false;
				String tnameJoin, cnameJoin, completeJoin;
				
				objWhere.setTNameWhere(tabNameWhere);
				objWhere.setCNameWhere(colNameWhere);
				objWhere.setWhereIndxPos(tables);
				objWhere.setCard(colcard);
				
				// look for operator, IN or = > <
				token1 = tokenizer.nextToken();
				String op = token1;			// change Q18
				objWhere.setOp(token1);
				// out.println("Token is"+ token1);
				
				Boolean isJoin = false;
				String textWhere = "";
				String value ="";
					
				// System.out.println("We are here.........................................");
				
				if(token1.equalsIgnoreCase("<"))
				{
					//out.println("token is "+ token1+ "in if");
					value = tokenizer.nextToken();
					//out.println("Value after operator is "+ value);
					
					if(value.matches("\\d.*"))
					{
						//System.out.println("Range predicate: Value in string is integer "+value);
						int litval =Integer.parseInt(value);
						int hkey=Integer.parseInt(hikey);
						int lkey=Integer.parseInt(lokey);
						
						ff=(float) (litval-lkey)/(hkey-lkey);
						

						if(ff<0)
							ff=0;
						else if (ff>1)
							ff=1;
						else
							ff=(float) ff;
						
						
						objWhere.setFf(ff);
						
						//System.out.println("Litvalue, highkey, lowkey int type is "+litval+" "+hkey+" "+lkey);
						//System.out.println("Filter factor for int type is "+ff);
					}
					else
					{
						String hkey=hikey.substring(0, 2);
						String lkey=lokey.substring(0, 2);
						String litval = value.substring(0, 2);
						//System.out.println("highkey and lowkey in case of Strings "+hkey+" "+lkey);
						//System.out.println("Lit val is "+litval);
						
						
						
						int hpos = getIndPosInAtoZ(hkey);
						int lpos = getIndPosInAtoZ(lkey);
						int litpos = getIndPosInAtoZ(litval);
						
						ff=(float) (litpos-lpos)/(hpos-lpos);
						

						if(ff<0)
							ff=0;
						else if (ff>1)
							ff=1;
						else
							ff=(float) ff;
							
							
						//System.out.println("Range Prediacte: String: Filter factor is "+ff);
						objWhere.setFf(ff);
					}
					
					
					textWhere = completeColNameWhere+" "+token1+" "+value;
					//out.println("Text in where for a predicate is "+textWhere);
				}
				else if(token1.equalsIgnoreCase(">"))
				{

					//out.println("token is "+ token1+ "in if");
					value = tokenizer.nextToken();
					//out.println("Value after operator is "+ value);
					
					if(value.matches("\\d.*"))
					{
						//System.out.println("Range predicate: Value in string is integer "+value);
						int litval =Integer.parseInt(value);
						int hkey=Integer.parseInt(hikey);
						int lkey=Integer.parseInt(lokey);
						
						ff=(float) (hkey-litval)/(hkey-lkey);
						

						if(ff<0)
							ff=0;
						else if (ff>1)
							ff=1;
						else
							ff=(float) ff;
						
						
						objWhere.setFf(ff);
						
						//System.out.println("Litvalue, highkey, lowkey int type is "+litval+" "+hkey+" "+lkey);
						//System.out.println("Filter factor for int type is "+ff);
					}
					else
					{
						String hkey=hikey.substring(0, 2);
						String lkey=lokey.substring(0, 2);
						String litval = value.substring(0, 2);
						//System.out.println("highkey and lowkey in case of Strings "+hkey+" "+lkey);
						//System.out.println("Lit val is "+litval);
						
						
						
						int hpos = getIndPosInAtoZ(hkey);
						int lpos = getIndPosInAtoZ(lkey);
						int litpos = getIndPosInAtoZ(litval);
						
						ff=(float) (hpos-litpos)/(hpos-lpos);
						

						if(ff<0)
							ff=0;
						else if (ff>1)
							ff=1;
						else
							ff=(float) ff;
							
							
						//System.out.println("Range Prediacte: String: Filter factor is "+ff);
						objWhere.setFf(ff);
					}
					
					
					textWhere = completeColNameWhere+" "+token1+" "+value;
					//out.println("Text in where for a predicate is "+textWhere);
				
				}
				else if(token1.equalsIgnoreCase("="))
				{	
					//out.println("token is "+ token1+ "in if");
					value = tokenizer.nextToken();
					//System.out.println(" d ind"+dotIndexWhere);
					
					if(dotIndexWhere == -1)
					{
						//System.out.println("Hi");
						if(value == LeftDiffPred)
						{
							//System.out.println("Diff preds are equal");
							objWhere.setDiffPredEq(true);
						}
						else
						{
							//System.out.println("Not equal");
							objWhere.setDiffPredEq(false);
						}
						
					}
					
					//out.println("Token after operator is "+ value);
					textWhere = completeColNameWhere+" "+token1+" "+value;
					//out.println("Text in where for a predicate is "+textWhere);
					
					//System.out.println("Colcard is "+colcard);
					ff = (float) 1/colcard;
					

					if(ff<0)
						ff=0;
					else if (ff>1)
						ff=1;
					else
						ff=(float) ff;
									
					objWhere.setFf(ff);
					//System.out.println("Filter factor for prediacte(=) is"+ff);
					
					if(value.contains("."))
					{
						isJoin = true;
						
						completeJoin = value;
						int ind = completeJoin.indexOf(".");
						
						tnameJoin = completeJoin.substring(0, ind);
						//out.println("TABLE IN WHERE join predicate is"+ tnameJoin);
						cnameJoin = completeJoin.substring(ind+1);
						//out.println("column name in where join pred is"+ cnameJoin);
						
						for (Table tab : tables) {
							if (tab.getTableName().equals (tnameJoin)) {
								ifexistTab = true;
								for (Column cols : tab.getColumns()) {
									if (cols.getColName().equalsIgnoreCase(cnameJoin)) {
										hikey1=cols.getHiKey();
										lokey1=cols.getLoKey();
										colcard1=cols.getColCard();
									}
								}
							}
						}
						
						//System.out.println("Hikey, lokey, colcard for join predicate right hand side is"+ hikey1+" "+lokey1+" "+colcard1);
						
						float ff1 = (float) 1/colcard1;
						

						if(ff<0)
							ff=0;
						else if (ff>1)
							ff=1;
						else
							ff=(float) ff;
							 
						//System.out.println("Filter factor for join pred is :"+ff1);
						objWhereJoin.setTNameWhere(tnameJoin);
						objWhereJoin.setCNameWhere(cnameJoin);
						objWhereJoin.setWhereText(textWhere);
						objWhereJoin.setCard(colcard1);
						objWhereJoin.setFf(ff1);
						objWhereJoin.setOp(token1);
						objWhereJoin.setIsJoinPred(isJoin);
						objWhereJoin.setWhereIndxPos(tables);
						//whrInfoArr.add(objWhereJoin);
						
					}
	
				}
				else if(token1.equalsIgnoreCase("IN"))
				{
					int totalelem=0;
					
					String inTokens = "";
					out.println("IN is the token");
					
					token1= tokenizer.nextToken();
					boolean hasMoreIn = true;
					
					if(token1.equalsIgnoreCase("(")) { inTokens = inTokens+ token1+" ";
					//out.println("Token is"+ token1);
					}
					else {out.println("error");
					throw new DbmsError("EXPECTING ( AFTER \"IN\". '" + sql + "'."); }
					
					while(hasMoreIn)
					{  
						totalelem++;
						token1= tokenizer.nextToken();		//value
						inTokens = inTokens+ token1+" ";
					//	out.println("value in in clause is"+token1);
						
						token1= tokenizer.nextToken();		//,
						inTokens = inTokens+ token1;
						
						if(token1.equalsIgnoreCase(","))
						{
							inTokens = inTokens+ " ";
							// 	continue;  //change Q18
							hasMoreIn = true;
						}
						else if(token1.equalsIgnoreCase(")"))
						{
							value = inTokens;
							inTokens = completeColNameWhere+" "+"IN"+" "+inTokens;
							textWhere = inTokens;
						//	out.println("tEXT IS "+"\""+inTokens+"\"");
							hasMoreIn = false;
						//  out.println(""+hasMoreIn);
						}
						else
						{
							throw new DbmsError("Tokens are seperated by space. \"OR\" EXPECTING ')' or ','  AFTER values. ALso ',' and ')' is seperated by space after value. Please check the query '" + sql + "'.");
						}
						
					}
					
					if(totalelem == 1)
					{
						objWhere.setInlistOneElem(true);
					}
					
					ff=(float) totalelem/colcard;
					
					if(ff<0)
						ff=0;
					else if (ff>1)
						ff=1;
					else
						ff=(float) ff;
						
					
					objWhere.setFf(ff);
					//System.out.println("colcard and tot elem is"+colcard+" "+totalelem);
					//System.out.println("filter factor for in is "+ff);
					//out.println("Outside hasMoreIn");
	
				}
				else
				{
					throw new DbmsError("Opertor not  valid: " + sql);
				}
				
				objWhere.setIsJoinPred(isJoin);
				objWhere.setWhereText(textWhere);
				objWhere.setValue(value);
				
				Boolean executed = false;
				token1 = tokenizer.nextToken();
				WhereInfo w = new WhereInfo();
				
				String concat="";
				
				if(isAnd == true)
				{
					if(! whrInfoArr.isEmpty() )
					{
					for(int i=0; i< whrInfoArr.size(); i++)
					{		w=whrInfoArr.get(i);
						concat = w.getTNameWhere()+"."+w.getCNameWhere();
				//		System.out.println("Value is"+value);
					//	System.out.println("Is join pred and complete cool name is"+w.getIsJoinPred()+""+concat);
						//System.out.println("completeColName"+completeColNameWhere);
							
						//For TCP
						if(w.getIsJoinPred()==true && concat.equalsIgnoreCase(completeColNameWhere))
							{
									for(int j=0 ;j<whrInfoArr.size(); j++)
									{	
										WhereInfo w1 = whrInfoArr.get(j);
							//			System.out.println("Inner Loop");
								//		System.out.println("Inner: Is join pred and complete cool name is"+w1.getIsJoinPred());
									//	System.out.println("Inner: completeColName"+w1.getTNameWhere()+"."+w1.getCNameWhere());
										String concat1 = w1.getTNameWhere()+"."+w1.getCNameWhere();
										
										if(w1.getIsJoinPred()==true &&  (! concat1.equalsIgnoreCase(completeColNameWhere)))
										{
											System.out.println("hi");
											addPredTCP.setTNameWhere(w1.getTNameWhere());
											addPredTCP.setCNameWhere(w1.getCNameWhere());
											addPredTCP.setWhereText(w1.getTNameWhere()+"."+w1.getCNameWhere()+" = "+value);
											addPredTCP.setCard(w1.getCard());
											addPredTCP.setAddPredTCP(true);
											addPredTCP.setFf(w1.getFf());
											addPredTCP.setOp(w.getOp());
											addPredTCP.setValue(value);
											addPredTCP.setWhereIndxPos(tables);
											addPredTCP.setShouldSequence(false);
											executed = true;
											//whrInfoArr.add(addPredTCP);
											//System.out.println("Predicate added");
											//set 
										}	
									}				
							}// is join loop end: FOR TCP
						
					
					//check sam col, =, And case
						//fishy
						else if(w.getOp().equals("=") && op.equals("=") && concat.equalsIgnoreCase(completeColNameWhere))
						{
							//System.out.println("Say hello");
							w.setShouldSequence(false);
							objWhere.setShouldSequence(false);
						}
						
						
						else if(((w.getOp().equals("<") && op.equals(">") && concat.equalsIgnoreCase(completeColNameWhere))||(w.getOp().equals(">") && op.equals("<") && concat.equalsIgnoreCase(completeColNameWhere))) && (w.getValue().equalsIgnoreCase(value)))
						{
							//System.out.println("< and > in AND");
							w.setShouldSequence(false);
							objWhere.setShouldSequence(false);
						}
						
						
						else if( (op.equals("=") && dotIndexWhere == -1 && completeColNameWhere == value))
						{
							objWhere.setShouldSequence(false);
						}
						
						else if  (w.getOp().equals("=") && w.getisDiffPred() == true && w.getisDiffPredEq() == true && concat.equalsIgnoreCase(completeColNameWhere)) 
						{
							w.setShouldSequence(false);
						}
						
						else if  (w.getOp().equals("=") && w.getisDiffPred() == true && w.getisDiffPredEq() == false &&  !concat.equalsIgnoreCase(completeColNameWhere)) 
						{
							w.setShouldSequence(false);
							objWhere.setShouldSequence(false);
						}
						
						else if (op.equals("=") &&  dotIndexWhere == -1 && completeColNameWhere != value)
						{
							//System.out.println("In and: value for diff predicate is: diff ");
							w.setShouldSequence(false);
							objWhere.setShouldSequence(false);
							
						}
						
					}
					}
					
				}
				
				
				if(isOr == true)
				{
									
					//conv to inlist
					
				String descrip="";
				int count=1;
				if(! whrInfoArr.isEmpty() )
				{
				for(int i=0; i< whrInfoArr.size(); i++)
				{		w=whrInfoArr.get(i);
							//out.println("Check if OR can be converted to In-List");
							
					if(completeColNameWhere.equals(w.getTNameWhere()+"."+w.getCNameWhere()) && op.equals("="))
					{
						count=count+1;
						objWhere.setIsConvInList(true);
						
						if(count == 2)
						{	if(w.getWhereText().contains("OR"))
							{
								String newText = w.getWhereText()+" OR "+completeColNameWhere+" = "+value;
								objWhere.setWhereText(newText);
							}
							else
							{
								String newText = w.getTNameWhere()+"."+w.getCNameWhere()+" = "+w.getValue()+" OR "+completeColNameWhere+" = "+value;
								objWhere.setWhereText(newText);
							}
						}
						else
						{	String newText = w.getWhereText()+" OR "+completeColNameWhere+" = "+value;
							objWhere.setWhereText(newText);
						}
						
						//float ff3= (float) 1/objWhere.getCard()
						float ff3 = 0;
						
						if(true)
						{
							ff3 = w.getFf()+ ((float) 1/objWhere.getCard());
							objWhere.setFf(ff3);
						}
						else
						{
							
						}
						whrInfoArr.remove(w);
						
					}// conv to in list- if loop closed
					
					
					
					// SAME COLUMN- RANGE PREDICATE - GREATER THAN
					//Q23: T1.C5>60 AND T1.C5>70
					
					if(completeColNameWhere.equals(w.getTNameWhere()+"."+w.getCNameWhere()) && op.equals(">") && w.getOp().equals(">"))
					{
						float wval = Float.parseFloat(w.getValue());
						float val = Float.parseFloat(value);
						
						if(wval>val)
						{
							w.setShouldSequence(false);
						}
						else
						{
							objWhere.setShouldSequence(false);
						}
												
					}
				
					// SAME COLUMN- RANGE PREDICATE - LESS THAN
					//T1.C5<60 AND T1.C5<70
					
					if(completeColNameWhere.equals(w.getTNameWhere()+"."+w.getCNameWhere()) && op.equals("<") && w.getOp().equals("<"))
					{
						float wval = Float.parseFloat(w.getValue());
						float val = Float.parseFloat(value);
						
						if(wval>val)
						{
							objWhere.setShouldSequence(false);
							
						}
						else
						{
							w.setShouldSequence(false);
						}
												
					}
					
					
					
				} // for - whereInfo loop closed
				}// if WhereInfo arr is empty closed
					
				} // isOr method end
				
				
				
				if(token1.equalsIgnoreCase("OR"))
				{
					isOr= true;
					
					// out.println("It is OR.... ->");
					
					
				}
				else if(token1.equalsIgnoreCase("AND"))
				{
					//loop through predicate processing again
					//		continue;				//change Q18
					
					isAnd = true;
					
					// out.println("It is AND predicate");
					
					
					
					
				}
				else
				{
					//come out
					hasMorePredicate = false;
				}
				
				//Add objects
				
				whrInfoArr.add(objWhere);
				
				if(isJoin==true)
				whrInfoArr.add(objWhereJoin);
				
				if(executed == true)
				whrInfoArr.add(addPredTCP);
				
			/*	if(isAnd == true && executed == true)
				{
					whrInfoArr.add(addPredTCP);
				}*/
			} // where- has more cols closed
				
				
			}
			
			
			// out.println("Printing where object");
			/*
			for (WhereInfo w1: whrInfoArr)
			{
				System.out.println("For objects: ...");
				System.out.printf("%s, ",w1.getWhereText());
				System.out.printf("%s, ",w1.getTNameWhere());
				System.out.printf("%s, ",w1.getCNameWhere());
				System.out.printf("%s ,",w1.getOp());
				System.out.printf("%s ,",w1.getValue());
				System.out.printf("%s ",w1.getWhereIndxPos());
				
				System.out.printf("\n","");
				
			}
			*/
	
			if (token1.equalsIgnoreCase("ORDER")) {
				
				token1 = tokenizer.nextToken();
				//out.println("token after ORDER is"+ token1);
				
				if(token1.equalsIgnoreCase("BY"))
				{
					while(hasMoreOrderBy)
					{					
						
					OrderByInfo ord = new OrderByInfo();
					token1=tokenizer.nextToken();
					//out.println("token after BY is "+ token1);
					
					completeColNameOrder = token1;
				
					dotIndexOrder = completeColNameOrder.indexOf(".");
					//out.println("dot index is "+ dotIndexOrder);
					
					tabNameOrder = completeColNameOrder.substring(0, dotIndexOrder);
					colNameOrder = completeColNameOrder.substring(dotIndexOrder+1);
									
					//out.println("In order, table is: "+tabNameOrder +" ,column name is "+colNameOrder);
					
					ord.setOrderText(completeColNameOrder);
					ord.setOrderTabName(tabNameOrder);
					ord.setOrderColName(colNameOrder);
					ord.setOrderIndxPos(tables);
					
					String descOrd = "A";
					//get A or D
					
					ord.setDescOrd(descOrd);
					token1 = tokenizer.nextToken();
					//out.println("Order is"+ token1);
					
					if(token1.equalsIgnoreCase(","))
					{
						ordInfoArr.add(ord);
						continue;
					}
					else if(token1.equalsIgnoreCase("A") || (token1.equalsIgnoreCase("D")))
					{
						descOrd = token1;
						token1 = tokenizer.nextToken();
						
						ord.setDescOrd(descOrd);
						if(token1.equalsIgnoreCase(","))
						{
							ordInfoArr.add(ord);
							continue;
						}
						else if(token1.equalsIgnoreCase(";"))
						{
							ordInfoArr.add(ord);
							break;
						}
						else
						{
							throw new DbmsError("EXPECTING ; or ,  AFTER \"token\". '" + sql + "'.");
						}
					}
					else if(token1.equalsIgnoreCase(";"))
					{
						ordInfoArr.add(ord);
						break;
					}
					else
					{
						throw new DbmsError("EXPECTING ; or ,  AFTER \"token\". '" + sql + "'.");
					}
			
				}	//while close
	
				}
				else
				{
					throw new DbmsError("EXPECTING By AFTER \"ORDER\". '" + sql + "'.");
				}
				
			
				
			}
			/*
			System.out.println("Printing order objects");
			for (OrderByInfo w1: ordInfoArr)
			{
				System.out.printf("%s, ", w1.getOrderTabName());
				System.out.printf("%s, ",w1.getOrderColName());
				System.out.printf("%s, ",w1.getOrderText());
				System.out.printf("%s ,",w1.getDescOrd());
				System.out.printf("%s ",w1.getOrderIndxPos());
				//System.out.printf("%s ",w1.getWhereIndxPos());
				
				System.out.printf("\n","");
				
			}*/
			
			if (token1.equalsIgnoreCase(";")) {
				
			}
			else
			{
				throw new DbmsError("EXPECTING ; After Query:" + sql + "'.");
			}
			
			//After parsing the query and storing info in respective tables
			
			ArrayList<Predicate> pList = predTable(whrInfoArr);
			
			//System.out.println("complete query is parsed.................................");
	
			
			

			for(int i=0 ; i<fromTables.size(); i++)
			{
				String tname = fromTables.get(i).getTNameFrom();
				for (Table tab : tables) {
					
					if(tab.getTableName().equalsIgnoreCase(tname))
					{
						ArrayList<Index> indArrList = tab.getIndexes();
						IndexList il = new IndexList();
						il.list = indArrList;
						il.printTable(new DbmsPrinter());
					}
					
					
				}
			}
			
			
			
			IndexList il = new IndexList();
			il.printTable(new DbmsPrinter());
			
			SelectImplem selimp = new SelectImplem(selInfoArr,fromTables,whrInfoArr,ordInfoArr);
			
			//System.out.println("Created selimp obj....just above it");
			
			SelectCasesT1 selcase1 = new SelectCasesT1(selimp);
			SelectCasesT2 selcase2 = new SelectCasesT2(selimp,tables);
			
			
			if(fromTables.size()==1)
			{
				selcase1.OneTable(fromTables.get(0).getTNameFrom(),tables,pList);
			}
			else if(fromTables.size()==2)
			{
				selcase2.TwoTable(pList);
			}
			else
			{
				throw new DbmsError ("Incorrect Select Statement.");
			}
			
		}
		
		catch (NoSuchElementException exc){
			throw new DbmsError ("Incorrect Select Statement.");
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
			
			// Delete the index files too
			for (Index index : table.getIndexes()) {
				File indexFile = new File(TABLE_FOLDER_NAME, table.getTableName()
						+ index.getIdxName() + INDEX_FILE_EXT);
				
				try {
					indexFile.delete();
				} catch (Exception ex) {
					out.println("Unable to delete table file for "
							+ indexFile.getName() + ".");
				}
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

			// Write RUNSTATS
			out.println("STATS TABCARD " + table.getTableCard());
			for (int i = 0; i < table.getColumns().size(); i++) {
				Column col = table.getColumns().get(i);
				if(col.getHiKey() == null)
					col.setHiKey("-");
				if(col.getLoKey() == null)
					col.setLoKey("-");
				out.println("STATS COLCARD " + i + " " + col.getColCard() + " " + col.getHiKey() + " " + col.getLoKey());
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
