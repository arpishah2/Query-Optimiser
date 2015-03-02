import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class SelectCasesT1 {

	private SelectImplem selimp = new SelectImplem(new ArrayList<SelectInfo>(),new ArrayList<FromInfo>(), new ArrayList<WhereInfo>(), new ArrayList<OrderByInfo>());
	public static int tcard = 0;
	public static ArrayList<Index> indxs = new ArrayList<Index>();
	
	public SelectCasesT1(SelectImplem selimp) {
		this.selimp = selimp;

		if(selimp.getFromTables().size() == 1)
		{
				
		selimp.selGetIndexPos();
		selimp.chooseIndexWhere();
		
		selimp.selGetIndexPosS();
		selimp.chooseIndexSel();
		
		selimp.selGetIndexPosO();
		selimp.chooseIndexWhereO();
		}
	}

	
	
	// if there is only one table in the query
	public void OneTable(String tabname, ArrayList<Table> tables, ArrayList<Predicate> pList)
	{
		
		
		//int tcard=0;
		//get table cardinality
		for(Table tab: tables)
		{
			if(tab.getTableName().equalsIgnoreCase(tabname))
				
				{
				this.tcard=tab.getTableCard();
				////System.out.println("tabname:"+tabname +"      tab.getTableName(): " +tab.getTableName());
				////System.out.println("hi");
				////System.out.println(tab.getTableCard());
				////System.out.println("hilf");
				 this.indxs = tab.getIndexes();
				}
		}
		
		////System.out.println(".................Only One Table..............");
		////System.out.println("Table card is: "+tcard);
		////System.out.println("Size of prediactes: "+ selimp.getWhrInfoArr().size());
		
		
		
		
		
		
		//Case I: No predicates:-------------------------------------- 
		
				if(selimp.getWhrInfoArr().isEmpty())		
				{
			//		//System.out.println("\nOne TAble: No predicates\n \n");
					/*	Following cases are possible:
					 *  1. index is present on select and order by
					 *  2. index is not present on select and o by
					 */
									
					//check if oby present
					
					PlanTable plantab = new PlanTable();
					plantab.setTable1Card(tcard);
					Boolean indxpresentO = false;
					String indnameO="";
					String winnerIndNameO="";
					int winnerPosO =0;
					int noOfIndO=0;
					ArrayList<Integer> indPosO = new ArrayList<Integer>(); 
					ArrayList<String> indNameO = new ArrayList<String>(); 
					
					if(selimp.getOrdInfoArr().isEmpty())
					{
						// Do nothing
						//check sel list for index only
					}
					else
					{
						//get no of ind on Oby
						
						for(int i=0; i<selimp.getIndextypeT1O().size(); i++)
						{
						    if(! selimp.getIndextypeT1O().get(i).equalsIgnoreCase("None"))
						    {
						    	noOfIndO++;
						    	indPosO.add(i);
						    }
							
						}
					
						if(noOfIndO==0)
						{
							//System.out.println("No index on ordr by");
							indxpresentO = false;
							plantab.setAccessType('R');
							plantab.setPrefetch('S');
							
						}
						else if(noOfIndO==1)
						{
							indxpresentO = true;
							winnerIndNameO = indNameO.get(0);
							winnerPosO = indPosO.get(0);
							plantab.setAccessType('I');
							plantab.setAccessName(winnerIndNameO);
							plantab.setPrefetch(' ');
						}
						else if(noOfIndO > 1)
						{
							plantab.setPrefetch(' ');
							//compute better matching and then go to screening if no matching
							indxpresentO = true;
							int countMAtchingO =0;
							
							ArrayList<Integer> indPosMO = new ArrayList<Integer>();
							 
							ArrayList<String> indNameMO = new ArrayList<String>(); 
							
							for(int j=0; j< selimp.getIndextypeT1O().size(); j++)
							{
								if(selimp.getIndextypeT1O().get(j).equals("M"))
									{
									countMAtchingO++;
									 indPosMO.add(j);
									 String name = indxs.get(j).getIdxName();
									 indNameMO.add(name);
									}
							}
							
							if(countMAtchingO==0)
							{ 
								indxpresentO = true;
								ArrayList<Integer> indPosSO = new ArrayList<Integer>(); 
								ArrayList<String> indNameSO = new ArrayList<String>();
								
								
								int noOfScrreeningO = 0;
								int bestScreenColT1O = 0;
								int bestScreenColPosT1O = 0;
								//look for screening
								for(int j=0; j< selimp.getIndextypeT1O().size(); j++)
								{
									if(selimp.getIndextypeT1O().get(j).equals("S"))
									{
										noOfScrreeningO++;
										indPosSO.add(j);
										 String name = indxs.get(j).getIdxName();
										 indNameSO.add(name);
									}	
								}
								
								if(noOfScrreeningO==1)
								{
									winnerIndNameO = indNameSO.get(0) ;
									plantab.setAccessType('I');
									plantab.setAccessName(winnerIndNameO);
									winnerPosO = indPosSO.get(0);
								}
								else if(noOfScrreeningO > 1)
								{
									int bestScreenPos=0;
									String bestScreenName ="";
									
									for(int i=0; i<= selimp.getArr1O().get(0).size(); i++)
									{
										if(selimp.getIndextypeT1O().get(i).equals('S'))
										{
											if(Integer.parseInt(selimp.getArr1O().get(0).get(i))  > bestScreenPos)
											{
												bestScreenPos = i;
												bestScreenName = indxs.get(i).getIdxName();
											}
												
										}
									}
									// look for best screening
									winnerPosO = bestScreenPos ;
									
									winnerIndNameO = bestScreenName;
									plantab.setAccessType('I');
									plantab.setAccessName(winnerIndNameO);
								}
								
								
								
							
							} // countMAtchingO == 0 close
							
							else if(countMAtchingO == 1)
							{
								winnerIndNameO = indNameMO.get(0) ;
								winnerPosO = indPosMO.get(0);
								plantab.setAccessType('I');
								plantab.setAccessName(winnerIndNameO);
								plantab.setMatchCols(selimp.getMatchColsT1O().get(indPosMO.get(0)));
							}
							else if(countMAtchingO > 1)
							{
								int bestMatchColT1O = 0;
								int bestmatColPosT1O = 0;
								
								for(int j=0; j< selimp.getMatchColsT1O().size(); j++)
								{
									if(selimp.getMatchColsT1O().get(j) > bestMatchColT1O)
										{
											bestMatchColT1O = selimp.getMatchColsT1O().get(j);
											bestmatColPosT1O = j;
										}
								}
								
								winnerIndNameO= indxs.get(bestmatColPosT1O).getIdxName();
								winnerPosO = bestmatColPosT1O;
								plantab.setAccessType('I');
								plantab.setAccessName(winnerIndNameO);
								plantab.setMatchCols(selimp.getMatchColsT1O().get(bestmatColPosT1O));
							}
							
							
							
						}
					}
					
					if(indxpresentO == true)
					{
						plantab.setSortC_orderBy('N');
						plantab.setPrefetch(' ');
						if(selimp.getIndOnlyPossibleT1S().get(winnerPosO) == true)
							plantab.setIndexOnly('Y');
						else
							plantab.setIndexOnly('N');
						
					}
					else		//no index o by
					{
						plantab.setSortC_orderBy('Y');
						
						
						
						
						Boolean indxpresentS = false;
						ArrayList<Integer> indonlylocS = new ArrayList<Integer>();	// Location of positions in select list that has index only
						//String bestindnameS="";
						ArrayList<String> indonlyNameS = new ArrayList<String>();	// NAmes of index where ionly is possible
						String winnerIndOnlyName="";
						
						int noOfIndS=0;
						
						
						//no index o by - 
						//ind pres on sel??? and no
						
						
						for(int i=0; i<selimp.getIndOnlyPossibleT1S().size(); i++)
						{
							Boolean b = selimp.getIndOnlyPossibleT1S().get(i);
							
							if(b == true)
							{
								noOfIndS++;
								indxpresentS = true;
								indonlylocS.add(i);
							}
						}
						
						
						//no ind on oby
						//  yes ind on select
						if(indxpresentS == true)
						{
							plantab.setPrefetch(' ');
							plantab.setAccessType('I');
							plantab.setIndexOnly('Y');
							
							if(noOfIndS == 1)
							{
								plantab.setAccessName(indxs.get( indonlylocS.get(0)).getIdxName());
							}
							else if(noOfIndS >1)
							{
								//get best matching among them
								
								int bestMValS=0;
								String bestMNameS ="";
								int bestMPos = 0;
								
								for(int i=0; i<= selimp.getMatchColsT1S().size(); i++)
								{
									if(selimp.getIndextypeT1S().get(i).equals('M'))
									{
										if(selimp.getMatchColsT1S().get(i)  > bestMValS)
										{
											bestMValS = selimp.getMatchColsT1S().get(i);
											bestMPos = i;
											bestMNameS = indxs.get(i).getIdxName();
										}
											
									}
								}
								
								plantab.setAccessName(bestMNameS);
							}
							
						}
						else
						{

							//no ind on oby
							//  no ind on select
							
							plantab.setAccessType('R');
							plantab.setIndexOnly('N');
							plantab.setPrefetch('S');
						}	
							
					}	
				
					
				
					
					//Print plan table
					plantab.printTable(new DbmsPrinter());
					
					Predicate pre = new Predicate();
				//	//System.out.println("\n\n\n\n\n\n");
					pre.printTable(new DbmsPrinter(),new ArrayList<Predicate>());
				
				
				}// Case........No Predicates ends
				
				
				
				
				//Case II: Have predicates:-------------------------------------- 
				/*
				 * Two case possible
				 * 1. Index present
				 * 2. Index not present
				 * 
				 * 
				 * 
				 */
				
				if(! selimp.getWhrInfoArr().isEmpty())		
				{
			
					Boolean indPresent = false;
					
					
					// Index Present? 
					
					for(int i=0; i<selimp.getIndextypeT1().size(); i++)
					{
						if(! selimp.getIndextypeT1().get(i).equalsIgnoreCase("None"))
						{
							indPresent = true;
						}
					}
					
					if(indPresent == false)			
					{
						havePredNoIndex(selimp , indxs, tcard);
					}
					else
					{
						havePredYesIndex(selimp , indxs, tcard);
					}
				}
				
			}// ONE TABLE END
					
	
	
				public static void havePredNoIndex(SelectImplem selimp, ArrayList<Index> indxs, int tcard)
				{				
					//Index not present in where clause
					//Rule 1-a-2
					
					ArrayList<WhereInfo> whrInfoArr = selimp.getWhrInfoArr();
					float ff;
					ArrayList<Double> ffarr = new ArrayList<Double>();
					
					MethodsUsed mu = new MethodsUsed();
					
						
						for(int j=0; j<whrInfoArr.size(); j++)
						{
							WhereInfo obj = whrInfoArr.get(j);
							if(obj.getisShouldSequence() != false)
							{
							ff= obj.getFf();
							ffarr.add((double) ff);
							}
						}
						
						////System.out.println("Unsorted ff"+ ffarr);
						
						Collections.sort(ffarr);
						////System.out.println("sorted ff"+ ffarr);
						
					//	//System.out.println("I am here");
						PlanTable plantab = new PlanTable();
						plantab.setTable1Card(tcard);
						
						/*	Following cases are possible:
						 *  1. index is present on select and order by
						 *  2. index is not present on select and o by
						 */
						
						
							//get no of ind on select list
							
							ArrayList<Integer> indPosS = new ArrayList<Integer>(); 
							ArrayList<String> indNameS = new ArrayList<String>();
							Boolean indxpresentS = false;
							String indnameS="";
							String winnerIndNameS="";
							int winnerPosS =0;
							int noOfIndS=0;
							
							
							//check if index only possible in select list
							
							for(int i=0; i<selimp.getIndOnlyPossibleT1S().size(); i++)
							{
							    if(selimp.getIndOnlyPossibleT1S().get(i) == true)
							    {
							    	noOfIndS++;
							    	indPosS.add(i);
							    }
								
							}
							
							//check no of index only possible
							int winningIndPosS = 0;
							if(noOfIndS == 0)
							{
								indxpresentS = false;
					
								
								//check orderby
								
								plantab.setIndexOnly('N');
								plantab.setPrefetch('S');
								plantab.setAccessType('R');
								//plantab.printTable(new DbmsPrinter());
							}
							else if(noOfIndS >= 1)
							{
								indxpresentS = true;
								plantab.setPrefetch(' ');
								
								int bestofMatchS = 0;
								
								for(int i=0; i<selimp.getMatchColsT1S().size(); i++)
								{
									for(int j=0; j<indPosS.size() ;j++)
									{
										if(indPosS.get(j) == i)
										{
											if(bestofMatchS < selimp.getMatchColsT1S().get(i))
											bestofMatchS = selimp.getMatchColsT1S().get(i);
											winningIndPosS = i;
										}	
									}
								}
								
								plantab.setAccessType('I');
								plantab.setAccessName(indxs.get(winningIndPosS).getIdxName());
								plantab.setPrefetch(' ');
								//plantab.printTable(new DbmsPrinter());
							}
							
							//check if order by possible
							
							if(selimp.getArr1O().size() !=0 )
							{
							if(indxpresentS == true)
							{
								for(int j=0; j<selimp.getIndextypeT1O().size(); j++)
								{
									if(j==winningIndPosS)
									{
										if(! selimp.getIndextypeT1O().get(j).equalsIgnoreCase("None"))
										{
											plantab.setSortC_orderBy('N');
											plantab.printTable(new DbmsPrinter());
										}
									}
								}
							}
							else
							{
								ArrayList<Integer> indPosMatS = new ArrayList<Integer>();
								boolean indPresonOinS = false; 
								ArrayList<String> indNameMatS = new ArrayList<String>(); 
								boolean indxpresentO = false;
								
								String indnameO="";
								String winnerIndNameO="";
								int winnerPosO =0;
								int noOfIndO=0;
								ArrayList<Integer> indPosO = new ArrayList<Integer>(); 
								ArrayList<String> indNameO = new ArrayList<String>();
								
								
								for(int j=0; j<selimp.getIndextypeT1O().size(); j++)
								{
									
										if(! selimp.getIndextypeT1O().get(j).equalsIgnoreCase("None"))
										{
											plantab.setSortC_orderBy('N');
											plantab.setAccessType('I');
											indPresonOinS = true;
											//plantab.setAccessName(accessName);
										}
								}
										
										if(indPresonOinS)
										{
											int countMAtchingO =0;
											
											ArrayList<Integer> indPosMO = new ArrayList<Integer>();
											 
											ArrayList<String> indNameMO = new ArrayList<String>(); 
											
											for(int j=0; j< selimp.getIndextypeT1O().size(); j++)
											{
												if(selimp.getIndextypeT1O().get(j).equals("M"))
													{
													countMAtchingO++;
													 indPosMO.add(j);
													 String name = indxs.get(j).getIdxName();
													 indNameMO.add(name);
													}
											}
											
											
											if(countMAtchingO==0)
											{ 
												indxpresentO = true;
												ArrayList<Integer> indPosSO = new ArrayList<Integer>(); 
												ArrayList<String> indNameSO = new ArrayList<String>();
												
												
												int noOfScrreeningO = 0;
												int bestScreenColT1O = 0;
												int bestScreenColPosT1O = 0;
												//look for screening
												for(int j=0; j< selimp.getIndextypeT1O().size(); j++)
												{
													if(selimp.getIndextypeT1O().get(j).equals("S"))
													{
														noOfScrreeningO++;
														indPosSO.add(j);
														 String name = indxs.get(j).getIdxName();
														 indNameSO.add(name);
													}	
												}
												
												if(noOfScrreeningO==1)
												{
													winnerIndNameO = indNameSO.get(0) ;
													plantab.setAccessType('I');
													plantab.setAccessName(winnerIndNameO);
													winnerPosO = indPosSO.get(0);
												}
												else if(noOfScrreeningO > 1)
												{
													int bestScreenPos=0;
													String bestScreenName ="";
													
													for(int i=0; i<= selimp.getArr1O().get(0).size(); i++)
													{
														if(selimp.getIndextypeT1O().get(i).equals('S'))
														{
															if(Integer.parseInt(selimp.getArr1O().get(0).get(i))  > bestScreenPos)
															{
																bestScreenPos = i;
																bestScreenName = indxs.get(i).getIdxName();
															}
																
														}
													}
													// look for best screening
													winnerPosO = bestScreenPos ;
													
													winnerIndNameO = bestScreenName;
													plantab.setAccessType('I');
													plantab.setAccessName(winnerIndNameO);
												}
												
											}//countMAtchingO == 0 close
											
											else if(countMAtchingO == 1)
											{
												winnerIndNameO = indNameMO.get(0) ;
												winnerPosO = indPosMO.get(0);
												plantab.setAccessType('I');
												plantab.setAccessName(winnerIndNameO);
												plantab.setMatchCols(selimp.getMatchColsT1O().get(indPosMO.get(0)));
											}
											else if(countMAtchingO > 1)
											{
												int bestMatchColT1O = 0;
												int bestmatColPosT1O = 0;
												
												for(int j=0; j< selimp.getMatchColsT1O().size(); j++)
												{
													if(selimp.getMatchColsT1O().get(j) > bestMatchColT1O)
														{
															bestMatchColT1O = selimp.getMatchColsT1O().get(j);
															bestmatColPosT1O = j;
														}
												}
												
												winnerIndNameO= indxs.get(bestmatColPosT1O).getIdxName();
												winnerPosO = bestmatColPosT1O;
												plantab.setAccessType('I');
												plantab.setAccessName(winnerIndNameO);
												//plantab.setMatchCols(selimp.getMatchColsT1O().get(bestmatColPosT1O));
												plantab.setPrefetch(' ');
												plantab.setSortC_orderBy('N');
												
												
											}
											
									//		plantab.printTable(new DbmsPrinter());
										}//indPresent close
									
										//plantab.printTable(new DbmsPrinter());
								}//indnotonSel, check Oby close
								
								
							}
							
						
						
							plantab.printTable(new DbmsPrinter());
						//plantab.setAccessType('R');
						//plantab.setPrefetch('S');
						//plantab.setSortC_orderBy('N');
						
						Predicate pre = new Predicate();
						//System.out.println("\n\n\n\n\n\n");
						
						ArrayList<Predicate> predarr= DBMS.predTable(whrInfoArr);
						
						
						for(int i=0; i<predarr.size(); i++)
						{
							if(predarr.get(i).isShouldSequence() != false)		//added 
							{
						//	//System.out.println("Already set values reset");
							Predicate pp = predarr.get(i);
							
							int seq = 0;
							double ff11= pp.getFf1();
							////System.out.println("Already set ff"+ff11);
							
							for(int j=0;j<ffarr.size();j++)
							{
								////System.out.println("Will compare with ff");
								if(ffarr.get(j)==ff11)
								{
									////System.out.println("Match found");
									////System.out.println("Set seq");
									seq=j+1;
									////System.out.println("Seq is "+seq);
								}
							}
							
							
							pp.setSequence(seq);
						}
						}	
						
						//Print predicate table	
						pre.printTable(new DbmsPrinter(),predarr);
						
					}
						
					
					
					
				
					
					
					
					
					
				
				
				public static void havePredYesIndex(SelectImplem selimp,  ArrayList<Index> indxs, int tcard)
				{
					
					////System.out.println(".............................One Table...Index Present...................");
					////System.out.println("Index present in where clause");
					////System.out.println("Hi");
										
					ArrayList<String> indType = selimp.getIndextypeT1();
					int countOfIndex = 0;
					ArrayList<String> indNames = new ArrayList<String>();
					ArrayList<Integer> whereIndexPresPos = new ArrayList<Integer>();
					
					String bestIndName ="";
					int bestIndexPos= 0;
					
					String tname = selimp.getFromTables().get(0).getTNameFrom();
					
					//check 1 index or more : countOfIndex
					
					for(int i=0; i<indType.size();i++)
					{ 	if(! indType.get(i).equalsIgnoreCase("None"))
						{	indNames.add(indxs.get(i).getIdxName());
							whereIndexPresPos.add(i);
							countOfIndex++;
						}
					}
					
					////System.out.println("No of index is "+ countOfIndex);
					////System.out.println("Index names are: "+indNames);
					////System.out.println("Index pos in index arr "+whereIndexPresPos);
					
					
					
					PlanTable plantab = new PlanTable();
					plantab.setTable1Card(tcard);
					
					if(countOfIndex == 1)   //only one index
					{
						
						////System.out.println("Inside count is 1");
						////System.out.println(selimp.getIndonT1O());
					
						bestIndName =indNames.get(0);
						bestIndexPos= whereIndexPresPos.get(0);
						
					}
					else
						// more than one index, choose on basis of match cols, --FF later
					{
						ArrayList<Integer> ChoosenIndexPos = new ArrayList<Integer>();
						ArrayList<String> ChoosenIndexNames = new ArrayList<String>();
						int noOfBestIndex = 0;
						int bestMatchcol=0;
						ArrayList<Integer> matchcols = selimp.getMatchColsT1();
						////System.out.println("Match col are:"+matchcols);
						
						for(int i=0; i<matchcols.size(); i++)
						{
							if(matchcols.get(i)>=bestMatchcol)
							{
								bestMatchcol = matchcols.get(i);
							}
							////System.out.println("Best no of Matchcol is: "+bestMatchcol);
						}
						
						for(int i=0; i<matchcols.size(); i++)
						{
							if(matchcols.get(i) == bestMatchcol)
							{
								ChoosenIndexPos.add(i);
								ChoosenIndexNames.add(indxs.get(i).getIdxName());
								noOfBestIndex++;
							}
						}
						
						////System.out.println("choosen index positions: with best match cols"+ChoosenIndexPos );
						////System.out.println("choosen index names: with best match cols"+ChoosenIndexNames );
						
						////System.out.println("no of best indexes is : "+ noOfBestIndex);
						
						if(noOfBestIndex == 1)		// only 1 index, no tie
						{
							bestIndexPos = ChoosenIndexPos.get(0);
							bestIndName = ChoosenIndexNames.get(0);
							
							
							/*
							 * bestIndName =indNames.get(0);
						bestIndexPos= whereIndexPresPos.get(0);
							 */
						}
						
						else if(noOfBestIndex>1)			//tie, break using ff
						{
							////System.out.println("\n Need to break tie using FF");
							
							ArrayList<Float> combFF = new ArrayList<Float>();
							ArrayList<ArrayList<Integer>> matchColPredPos= new ArrayList<ArrayList<Integer>>();
							
							//get position of predicates having matchcols
							
							for(int i=0; i<selimp.getArcol1().size(); i++)
							{
								for(int j=0; j<ChoosenIndexPos.size();j++)
								{
								if(i == ChoosenIndexPos.get(j))
								{
									ArrayList<String> arColWise = selimp.getArcol1().get(i);
									ArrayList<Integer> mcolpredpos = new ArrayList<Integer>();
									
									for(int k=0; k<arColWise.size();k++)
									{
										if(!arColWise.get(k).equals("-"))
										{
										if(selimp.getMatchColsT1().get(i) >= Integer.valueOf(arColWise.get(k)))
										{
											mcolpredpos.add(k);
										}
										}
											
									}
									matchColPredPos.add(mcolpredpos);
								}	
							}	
						}
							
							for(ArrayList<Integer> i : matchColPredPos)
							{
								////System.out.println("Position of matccols prediatces is"+ i);
							}
							
							
							// comput combind ff
							
							for(int i=0; i<selimp.getArcol1().size(); i++)							//go through arcols
							{
								ArrayList<String> colWise = selimp.getArcol1().get(i);
								
							
								//get combined filter factor
								
								for(int j=0; j<ChoosenIndexPos.size();j++)							//fo to choose indexpos: has location of index pos
								{
									float ff=1;
									float combiff=1;
									
								if(i == ChoosenIndexPos.get(j))										//if arcol loc == choosed index pos 
								{
									
									ArrayList<Integer> mcolpredpos = matchColPredPos.get(j);		//fetch pred pos for that arcol loc
											
									for(int k=0; k<colWise.size();k++)								//go through colwise arr list
									{
										for(int m=0; m< mcolpredpos.size(); m++)					// go through pred pos arrlist
										{
											if(k == mcolpredpos.get(m))								// if match found
											{
												ff= selimp.getWhrInfoArr().get(k).getFf();			//get ff
									//			//System.out.println("FF of "+k+ "th predicate is for index pos "+i+" is" +ff);
											}
										}
										//
									}
									combiff = ff*combiff;
									////System.out.println("Combined ff is : "+combiff);
									
									combFF.add(combiff);
									////System.out.println("Arraylist of combined ff is"+ combFF);
								}
								
							}
							}
							
							// get best combined filter factor
							float cff=combFF.get(0);
							int loc=0;
							
							
								
							for(int i=0;i<combFF.size();i++)
							{ 
								if(cff>combFF.get(i))
								{
									cff= combFF.get(i);
									loc=i;
								}
								
							}
							
							////System.out.println("Best combined filter factor is "+cff+ "which is in location(in combined ff arr): " + loc);
							int finalloc = 0;
							for(int i=0; i<ChoosenIndexPos.size();i++)
							{
								if(i==loc)
								{
									finalloc= ChoosenIndexPos.get(i);
								}
								
							}
							
							////System.out.println("In indexes the best index is in pos "+finalloc);
							
							
							for(int i=0; i<indxs.size(); i++)
							{
								if(i==finalloc)
								{
									bestIndexPos = i ;
									bestIndName = indxs.get(i).getIdxName();
								}
							}
						} //noofBest matching index 1 loop closed
							
					}// more than 1 index case closed
				
					
					

					
					
					for( WhereInfo w: selimp.getWhrInfoArr())
					{
						if(w.getOp().equalsIgnoreCase("IN") ||  w.getIsConvInList() == true)
						{
							ArrayList<String> indxPos = w.getWhereIndxPos();
							
							
							for(int i=0; i<indxPos.size(); i++)
							{
								if(i == bestIndexPos)
								{
									if(! indxPos.get(i).equalsIgnoreCase("-"))
									{
										//System.out.println("Set in list");
										plantab.setAccessType('N');
									}
								}
							}
						}
						
												
						if( w.getisInlistOneElem() == true)
						{
							
							ArrayList<String> indxPos = w.getWhereIndxPos();
							
							
							for(int i=0; i<indxPos.size(); i++)
							{
								if(i == bestIndexPos)
								{
									plantab.setAccessType('I');
								}
							}		
							
						
						}
					}
					
					//plan table set
					
					plantab.setTable1Card(tcard);
					
					if(plantab.accessType != 'N')			//change
					plantab.setAccessType('I');
					
					plantab.setAccessName(bestIndName);
					plantab.setMatchCols(selimp.getMatchColsT1().get(bestIndexPos));
					
					
					//.......index only set
					
					
					for( int i=0; i<selimp.getIndOnlyPossibleT1().size(); i++)
					{
						if(i==bestIndexPos)
						{
							if(selimp.getIndOnlyPossibleT1().get(i) == true)
							{
								//go to select list to check if it is possible
								if(selimp.getIndOnlyPossibleT1S().get(i)==true)
								{
									plantab.setIndexOnly('Y');
								}
								else
								{
									plantab.setIndexOnly('N');
								}
							}
						}
					}
						
						//check if index can avoid the sort
						
						for(int j=0; j<selimp.getIndonT1O().size(); j++)
						{
							//System.out.println("check if index can avoid sort");
							
							if(j==bestIndexPos)
							{
								Boolean ans = selimp.getIndonT1O().get(j);
								//System.out.println("Is index present in OBy:" + ans);
								if(ans==true)
								{
									plantab.setSortC_orderBy('N');
								}
								else
								{
									plantab.setSortC_orderBy('Y');
								}
							}
							
						}
						
						plantab.printTable(new DbmsPrinter());
						
			
						//Print predicate table
				
						//Predicate table
						
						ArrayList<WhereInfo> whrInfoArr1 = selimp.getWhrInfoArr();
						float ff1;
						ArrayList<Double> ffarr1 = new ArrayList<Double>();
						float ff1Ind;
						ArrayList<Double> ffarr1Ind = new ArrayList<Double>();

						if(whrInfoArr1.size()>=1)			
						{
							
							for(int j=0; j<whrInfoArr1.size(); j++)
							{
								WhereInfo obj = whrInfoArr1.get(j);
								
								if(obj.getisShouldSequence() != false && selimp.getArcol1().get(bestIndexPos).get(j).equals("-")) //change seq to note
								{
								ff1= obj.getFf();
								ffarr1.add((double) ff1);
								//unsortMapff.put(j,(double) ff);
								}
							}
							
							////System.out.println("Unsorted ff"+ ffarr1);
							Collections.sort(ffarr1);
							//System.out.println("sorted ff"+ ffarr1);
							Predicate pre = new Predicate();
							//System.out.println("\n");
							
							/* added */
							
							ArrayList<String> onlyIndPresentSeq = new ArrayList<String>();
							ArrayList<Integer> indexSeqPos = new ArrayList<Integer>();
							ArrayList<String> indexSorted = new ArrayList<String>();
							
							for(int j=0; j<whrInfoArr1.size(); j++)
							{
								WhereInfo obj = whrInfoArr1.get(j);
								
								if(obj.getisShouldSequence() != false && ! selimp.getArcol1().get(bestIndexPos).get(j).equals("-"))  //change seq to note
								{
								
									onlyIndPresentSeq.add(selimp.getArcol1().get(bestIndexPos).get(j));
									indexSeqPos.add(j);
																		
								}
							}
							
							
							
							//System.out.println("Unsorted ind seq"+ onlyIndPresentSeq);
							Collections.sort(onlyIndPresentSeq);
							//System.out.println("sorted ind seq"+ onlyIndPresentSeq);
							
							
							ArrayList<Predicate> predarr= DBMS.predTable(whrInfoArr1);
							
							
							for(int j=0; j<onlyIndPresentSeq.size(); j++)
							{
								for(int k=0; k<selimp.getArcol1().get(bestIndexPos).size(); k++)
								{
									if(onlyIndPresentSeq.get(j).equals(selimp.getArcol1().get(bestIndexPos).get(k)))
									{
										predarr.get(k).setSequence(j+1);
										//System.out.println("Seq is"+ j+1);
									}
								}
							}
								
						
							
							int seqInd = selimp.getPredinIndT1().get(bestIndexPos);		//change seq to note
							
							//seq new sort
							
							for(int i=0; i<predarr.size(); i++)
							{
								//System.out.println("Already set values reset");
								Predicate pp = predarr.get(i);
								int seq = 0;
								double ff11= pp.getFf1();
								//System.out.println("Already set ff"+ff11);
								
								//seq: change added one more condition after &&
								if(pp.isShouldSequence() != false && selimp.getArcol1().get(bestIndexPos).get(i).equals("-"))
								{
								for(int j=0;j<ffarr1.size();j++)
								{
									//System.out.println("Will compare with ff");
									if(ffarr1.get(j)==ff11)
									{
										//System.out.println("Match found");
										//System.out.println("Set seq");
										seq=seqInd+j+1;
										//System.out.println("Seq is "+seq);
									}
								}
								
								pp.setSequence(seq);
								}
							}	
							
							//Print predicate table	
							pre.printTable(new DbmsPrinter(),predarr);
			
					}
						
					
					}//havePredYesIndex closed
				
				
				
			
				
	

	
	
	
	
	
	
	
}// CLASS END
