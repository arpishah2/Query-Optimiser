import java.util.ArrayList;
import java.util.Collections;


public class SelectCasesT2 {

	private SelectImplem selimp = new SelectImplem(new ArrayList<SelectInfo>(),new ArrayList<FromInfo>(), new ArrayList<WhereInfo>(), new ArrayList<OrderByInfo>());
	private ArrayList<Table> tables = new ArrayList<Table>();
	private Table tab1 = null;
	private int card1;
	private String leadTable;
	private String innerTable;
	private Table tab2 = null;
	private int card2;
	private ArrayList<Index> indxsT1 = new ArrayList<Index>();
	private ArrayList<Index> indxsT2 = new ArrayList<Index>();
	
	
	private PlanTable plantab = new PlanTable();
	private Predicate pre = new Predicate();
	private ArrayList<Predicate> predarr = new ArrayList<Predicate>();
	
	private char acctype='z',ionly='z', prefetch='z',sortcOBy='z';
	private String accname="";
	
	
	
	
	//................................constructor.................................
	
	public SelectCasesT2(SelectImplem selimp, ArrayList<Table> tables)
	{
		this.selimp = selimp;
		this.tables=tables;
		
		for(Table t: tables)
		{	if(t.getTableName().equalsIgnoreCase(selimp.getFromTables().get(0).getTNameFrom()))
			{
				this.tab1 = t;
				this.card1 = tab1.getTableCard();
				this.indxsT1=tab1.getIndexes();
			}
		}
		
		if(selimp.getFromTables().size()>1)
		{
		for(Table t: tables)
		{	if(t.getTableName().equalsIgnoreCase(selimp.getFromTables().get(1).getTNameFrom()))
			{
				this.tab2 = t;
				this.card2 = tab2.getTableCard();
				this.indxsT2=tab2.getIndexes();
			}
		}
		}
		
		if(selimp.getFromTables().size()==2)
		{
		//System.out.println("Set Where........................................");
		selimp.selGetIndexPos();
		selimp.chooseIndexWhere();
		//System.out.println("Set Select........................................");
		selimp.selGetIndexPosS();
		selimp.chooseIndexSel();
		//System.out.println("Set Order........................................");
		selimp.selGetIndexPosO();
		selimp.chooseIndexWhereO();
		}
		plantab.setPrefetch('S');
		plantab.setTable1Card(card1);
		plantab.setTable2Card(card2);
	}
	
	
	//..................................call method as per particular case.........................
	
	public void TwoTable(ArrayList<Predicate> pList)
	{		
		
		
		boolean hasLocalPred = false;
		boolean isIndPresent = false;
		
		boolean indPresentT1 = false;
		int countOfIndexT1 = 0;
		
		boolean indPresentT2 = false;
		int countOfIndexT2 = 0;
		
		//....................... has local predicates??
		
		if(selimp.getWhrInfoArr().size()==2)
		{
			hasLocalPred = false;
		}
		else if(selimp.getWhrInfoArr().size()>2)
		{
			hasLocalPred = true;
		}
		
		
		//......................is index present and its count
		
		for(int i=0; i<selimp.getIndextypeT1().size(); i++)
		{
		if(! selimp.getIndextypeT1().get(i).equals("None"))
		{
			indPresentT1 = true;
			countOfIndexT1++;
		}
		}
		
		for(int i=0; i<selimp.getIndextypeT2().size(); i++)
		{
		if(! selimp.getIndextypeT2().get(i).equals("None"))
		{
			indPresentT2 = true;
			countOfIndexT2++;
		}
		}
		
		//................................... Go for cases
		
		if(countOfIndexT1==0 && countOfIndexT2==0)		//no index on both tables
		{
			//System.out.println("Case: Index is not present on any of the tables");
			isIndPresent = false;
			
			if(hasLocalPred == false)
			{
				//System.out.println("No local predicate");
				
				noIndexNoLocal();
			}
			else
			{
				//System.out.println("Yes local predicate");
				noIndexYesLocal();
			}
			
		}
		else
		{
			isIndPresent = true;
			//System.out.println("Case: Index present on both/only one tables");
			
			if(hasLocalPred == false)
			{
				//System.out.println("No local predicate");
				
				yesIndexNoLocal();
			}
			else
			{
				//System.out.println("Yes local predicate");
				yesIndexYesLocal();
			}
			
			
		}
	
	}
	
	
	
	public void checkOby(PlanTable plantable)
	{
		
		
		Boolean indxpresentO = false;
		String indnameO="";
		String winnerIndNameO="";
		int winnerPosO =0;
		int noOfIndO=0;
		ArrayList<Integer> indPosO = new ArrayList<Integer>(); 
		ArrayList<String> indNameO = new ArrayList<String>(); 
		
		
	if(selimp.getOrdInfoArr().size() != 0)	
	{	
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
						 String name = this.indxsT1.get(j).getIdxName();
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
							 String name = indxsT1.get(j).getIdxName();
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
									bestScreenName = indxsT1.get(i).getIdxName();
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
					
					winnerIndNameO= indxsT1.get(bestmatColPosT1O).getIdxName();
					winnerPosO = bestmatColPosT1O;
					plantab.setAccessType('I');
					plantab.setAccessName(winnerIndNameO);
					plantab.setMatchCols(selimp.getMatchColsT1O().get(bestmatColPosT1O));
				}
				
				
				
			}//for t1 closed
		}
		
		if(indxpresentO == true)
		{
			plantab.setSortC_orderBy('N');
			plantab.setPrefetch(' ');
			
			
		}
		else		//no index o by
		{
			plantab.setSortC_orderBy('Y');
			
			
		}
		
	}// T1 closed
		
	if(selimp.getOrdInfoArr().size() != 0)	
	{

		//get no of ind on Oby
		
		for(int i=0; i<selimp.getIndextypeT2O().size(); i++)
		{
		    if(! selimp.getIndextypeT2O().get(i).equalsIgnoreCase("None"))
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
			
			for(int j=0; j< selimp.getIndextypeT2O().size(); j++)
			{
				if(selimp.getIndextypeT2O().get(j).equals("M"))
					{
					countMAtchingO++;
					 indPosMO.add(j);
					 String name = indxsT2.get(j).getIdxName();
					 indNameMO.add(name);
					}
			}
			
			if(countMAtchingO==0)
			{ 
				indxpresentO = true;
				ArrayList<Integer> indPosSO = new ArrayList<Integer>(); 
				ArrayList<String> indNameSO = new ArrayList<String>();
				
				
				int noOfScrreeningO = 0;
				int bestScreenColT2O = 0;
				int bestScreenColPosT2O = 0;
				//look for screening
				for(int j=0; j< selimp.getIndextypeT2O().size(); j++)
				{
					if(selimp.getIndextypeT2O().get(j).equals("S"))
					{
						noOfScrreeningO++;
						indPosSO.add(j);
						 String name = indxsT2.get(j).getIdxName();
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
						if(selimp.getIndextypeT2O().get(i).equals('S'))
						{
							if(Integer.parseInt(selimp.getArr1O().get(0).get(i))  > bestScreenPos)
							{
								bestScreenPos = i;
								bestScreenName = indxsT2.get(i).getIdxName();
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
				plantab.setMatchCols(selimp.getMatchColsT2O().get(indPosMO.get(0)));
			}
			else if(countMAtchingO > 1)
			{
				int bestMatchColT2O = 0;
				int bestmatColPosT2O = 0;
				
				for(int j=0; j< selimp.getMatchColsT2O().size(); j++)
				{
					if(selimp.getMatchColsT2O().get(j) > bestMatchColT2O)
						{
							bestMatchColT2O = selimp.getMatchColsT2O().get(j);
							bestmatColPosT2O = j;
						}
				}
				
				winnerIndNameO= indxsT2.get(bestmatColPosT2O).getIdxName();
				winnerPosO = bestmatColPosT2O;
				plantab.setAccessType('I');
				plantab.setAccessName(winnerIndNameO);
				plantab.setMatchCols(selimp.getMatchColsT2O().get(bestmatColPosT2O));
			}
			
			
			
		}
	
	
	if(indxpresentO == true)
	{
		plantab.setSortC_orderBy('N');
		plantab.setPrefetch(' ');
		
		
	}
	else		//no index o by
	
		plantab.setSortC_orderBy('Y');
		
		
		
		
	}	
		
		
		
	}
	
	
	
	
	
	
	
	
	public void checkObyIndex(PlanTable plantable, String tabname, String indexname ,int bestIndexPos)
	{
		
		if(tabname.equals(selimp.getFromTables().get(0).getTNameFrom()) &&  selimp.getIndonT1O().size() != 0 )
		{
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
		
		}//T1 close
		
		
		
		if(tabname.equals(selimp.getFromTables().get(1).getTNameFrom()) &&  selimp.getIndonT2O().size() != 0 )
		{
		for(int j=0; j<selimp.getIndonT2O().size(); j++)
		{
			//System.out.println("check if index can avoid sort");
			
			if(j==bestIndexPos)
			{
				Boolean ans = selimp.getIndonT2O().get(j);
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
		
		}//T2 close
		
		
		
	}
	
	
	
	//...................................No index - No local predicate..................................
	
	public void noIndexNoLocal()
	{
		
		//..........set leading table
		
		if(card1>card2)
		{
			leadTable = tab1.getTableName();
			innerTable = tab2.getTableName();
		}
		else
		{
			leadTable = tab2.getTableName();
			innerTable = tab2.getTableName();
		}
		
		//.............set plan table
		
		plantab.setAccessType('R');
		plantab.setLeadTable(leadTable);
		
		checkOby(plantab);
		
		
		//..............print plan table
		
		plantab.printTable(new DbmsPrinter());
		
		predarr= DBMS.predTable(selimp.getWhrInfoArr());
	
		if(predarr.get(0).isShouldSequence() != false)
		predarr.get(0).setSequence(1);
		
		pre.printTable(new DbmsPrinter(), predarr);
	
	}
	
	//...................................No index - Yes local predicate..................................
	
	public void noIndexYesLocal()
	{
		float ff=1;
		String tabname;
		ArrayList<Float> tableFF = new ArrayList<Float>();
		ArrayList<Float> resultSet = new ArrayList<Float>();
		float bestTabRS = 0;   //outer- greater RS
		String bestTabRSName ="";
		int bestTabRSPos = 0;
		
		//get Table filter factor..........................
		
		for(int i=0; i<selimp.getFromTables().size(); i++)
		{
			tabname= selimp.getFromTables().get(i).getTNameFrom();
			
			for(int j=0; j<selimp.getWhrInfoArr().size(); j++)
			{
				if(selimp.getWhrInfoArr().get(j).getTNameWhere().equalsIgnoreCase(tabname))
				{
					ff=ff* selimp.getWhrInfoArr().get(j).getFf();
				}
			}
			tableFF.add(ff);
			
			if(i==0)
			resultSet.add(ff*card1);
			else
			resultSet.add(ff*card2);
			
		}
		

		
		//get leading table on the basis of tableResultSet............
		//outer: Rs larger
		
		bestTabRS = resultSet.get(0);
		
		for(int i=0; i<resultSet.size(); i++)
		{
			if(resultSet.get(i) >= bestTabRS)
			{
				bestTabRS = resultSet.get(i);
				bestTabRSPos = i;					//lead table
			}
			
		}
		
		for(int j=0; j< selimp.getFromTables().size(); j++)
		{
			if(j==bestTabRSPos)
			{
				bestTabRSName = selimp.getFromTables().get(j).getTNameFrom();
				leadTable = bestTabRSName;
			}
			if(j!=bestTabRSPos)
			{
				innerTable = selimp.getFromTables().get(j).getTNameFrom();;
			}
		}
		//System.out.println("Laed and inner table are:"+ leadTable + " "+ innerTable);
		
		//.............set and print plan table
		
			plantab.setAccessType('R');
			plantab.setLeadTable(leadTable);	
			checkOby(plantab);
			plantab.printTable(new DbmsPrinter());
			
			
		//............set and print predicate table
			
			
			ArrayList<Float> ffArrT1 = new ArrayList<Float>();
			ArrayList<Float> ffArrT2 = new ArrayList<Float>();
			float f1=0, f2=0;
			
			//LAead table======ffArrT1
			
			for(int j=0; j<selimp.getWhrInfoArr().size(); j++)
			{
				WhereInfo w1 = selimp.getWhrInfoArr().get(j);
				if(w1.getisShouldSequence() != false)
				{
				if(w1.getTNameWhere().equalsIgnoreCase(leadTable) && w1.getIsJoinPred()==false)
				{
						f1 = w1.getFf();
						ffArrT1.add(f1);
				}
				}
				
			}
			
			for(int j=0; j<selimp.getWhrInfoArr().size(); j++)
			{
				WhereInfo w1 = selimp.getWhrInfoArr().get(j);
				if(w1.getisShouldSequence() != false)
				{
				if(w1.getTNameWhere().equalsIgnoreCase(innerTable) && w1.getIsJoinPred()==false)
				{
						f2 = w1.getFf();
						ffArrT2.add(f2);
				}
				}
			}
			
			Collections.sort(ffArrT1);
			//System.out.println("SORTED LEAD TABLE FF: "+ffArrT1);
			Collections.sort(ffArrT2);
			//System.out.println("SORTED INNER TABLE FF: "+ffArrT2);
			
			int seq1=0;
			//Predicate table
			ArrayList<Predicate> predarr= DBMS.predTable(selimp.getWhrInfoArr());
			
			for(int i=0; i<predarr.size(); i++)
			{		int seq =0;
					Predicate pp = predarr.get(i);
					double ff1 = pp.getFf1();
					
					if(pp.isShouldSequence() != false)
					{
					if(pp.text.contains(leadTable) && pp.getFf2()==0)
					{		
					for(int j=0;j<ffArrT1.size();j++)
					{
						if(ffArrT1.get(j) == ff1)
						{
							seq= j+1;
						}
					
						pp.setSequence(seq);
						seq1 = seq;
					}

					}
					}
					
			}
			
			//System.out.println("Seq1 is "+seq1);
			
			for(int i=0; i<predarr.size(); i++)
			{  	
				Predicate pp = predarr.get(i);
				if(pp.getFf2()!=0.0f && pp.isShouldSequence() != false)
				{
					seq1=seq1+1;
					pp.setSequence(seq1);
				}
				
			}
			
			for(int i=0; i<predarr.size(); i++)
			{		int seq =seq1;
					Predicate pp = predarr.get(i);
					double ff2 = pp.getFf1();
					if(pp.isShouldSequence() != false)
					{
					if(pp.text.contains(innerTable) && pp.getFf2()==0)
					{		
						
					for(int j=0;j<ffArrT2.size();j++)
					{
						if(ffArrT2.get(j) == ff2)
						{
							seq= seq+j+1;
							pp.setSequence(seq);
						}
					
					}
					
					}
					}	
			}
			
			
			pre.printTable(new DbmsPrinter(),predarr);
			
	}
	
	//...................................Yes index - No local predicate..................................
	
	public void yesIndexNoLocal()
	{
		boolean IndPresonT1 = false;
		boolean IndPresonT2 = false;
		String tabname1 = tab1.getTableName();
		String tabname2= tab2.getTableName();
		ArrayList<String> T1joinColIndxPos = new ArrayList<String>();
		ArrayList<String> T2joinColIndxPos = new ArrayList<String>();
		
		//get join column on T1 index location
		
		for(int i=0; i<selimp.getWhrInfoArr().size(); i++)
		{
			WhereInfo w1= selimp.getWhrInfoArr().get(i);
			
			if(w1.getIsJoinPred()==true && w1.getTNameWhere().equalsIgnoreCase(tabname1))
			{
				T1joinColIndxPos = w1.getWhereIndxPos();
				
			}
		}
		
		
		//get join column on T2 index location
		
		for(int i=0; i<selimp.getWhrInfoArr().size(); i++)
		{
			WhereInfo w1= selimp.getWhrInfoArr().get(i);
			
			if(w1.getIsJoinPred()==true && w1.getTNameWhere().equalsIgnoreCase(tabname2))
			{
				T2joinColIndxPos = w1.getWhereIndxPos();
				
			}
		}
		
		
		int countOfIndxT1joinCol = 0;
		int countOfIndxT2joinCol = 0;
		
		// Check if index present on T1 and its count
		
		for(int i=0; i<T1joinColIndxPos.size(); i++)
		{
			if(! T1joinColIndxPos.get(i).equals("-"))
			{
				countOfIndxT1joinCol++;
			}
		}
		
		if(countOfIndxT1joinCol==0)
			IndPresonT1 = false;
		else
			IndPresonT1 = true;		
		
		// Check if index present on T2 and its count
		

		for(int i=0; i<T2joinColIndxPos.size(); i++)
		{
			if(! T2joinColIndxPos.get(i).equals("-"))
			{
				countOfIndxT2joinCol++;
			}
		}
		
		
		if(countOfIndxT2joinCol==0)
			IndPresonT2 = false;
		else
			IndPresonT2 = true;
		
		
		Boolean only1IndonJoinCol = false;
		
		//System.out.println("Count of index on join cols is "+countOfIndxT1joinCol +" "+countOfIndxT2joinCol);
		
		
		if((countOfIndxT1joinCol==0 && countOfIndxT2joinCol!=0) || (countOfIndxT1joinCol!=0 && countOfIndxT2joinCol==0) )
		{
			only1IndonJoinCol = true;
		}
		
		
		//...No index - Yes local pred - Only 1 ind on join col..Case 1
		//2-b-1-1
		
		if(only1IndonJoinCol==true)
		{
			
			if(countOfIndxT1joinCol==0)
			{
				leadTable = selimp.getFromTables().get(0).getTNameFrom();
				innerTable = selimp.getFromTables().get(1).getTNameFrom();
			}
			
			if(countOfIndxT2joinCol==0)
			{
				leadTable = selimp.getFromTables().get(1).getTNameFrom();
				innerTable = selimp.getFromTables().get(0).getTNameFrom();
			}
			
			//System.out.println("Lead and inner table are:" + leadTable + " " + innerTable);
			int innerTableLoc=0;
			int leadTableLoc=0;
			
			for(int i=0; i<selimp.getFromTables().size(); i++)
			{
				String tname = selimp.getFromTables().get(i).getTNameFrom();
				
				if(tname.equalsIgnoreCase(innerTable))
					innerTableLoc = i;
				
				if(tname.equalsIgnoreCase(leadTable))
					leadTableLoc = i;
			}
			//System.out.println("Inner table loc is "+ innerTableLoc);
			
			ArrayList<Integer> noOfMatchCol;
			if (innerTableLoc==0)
			{ noOfMatchCol = selimp.getMatchColsT1();}
			else
			{ noOfMatchCol = selimp.getMatchColsT2();}
				
			//System.out.println("No of match cols arraylist: "+noOfMatchCol);
			int maxMatchCol = 0;
			int maxMatchPos = 0;
			
			for(int i=0; i<noOfMatchCol.size(); i++)
			{
				if(noOfMatchCol.get(i)>maxMatchCol)
				{
					maxMatchCol= noOfMatchCol.get(i);
					maxMatchPos = i;
				}
				
			}
			
			//System.out.println("Max match position is: "+maxMatchPos);
			
			String bestIndxName ="";
			for(Table t: tables)
			{
				if(t.getTableName().equals(innerTable))
				{
					ArrayList<Index> indxs = t.getIndexes();
					
					for(int j=0; j<indxs.size();j++)
					{
						if(j==maxMatchPos)
						{
							bestIndxName=indxs.get(j).getIdxName();
						}
					}
				}
			}
			
			plantab.setAccessType('I');
			plantab.setMatchCols(maxMatchCol);
			plantab.setAccessName(bestIndxName);
			plantab.setLeadTable(leadTable);
			checkObyIndex(plantab, innerTable, bestIndxName ,maxMatchPos);
			plantab.printTable(new DbmsPrinter());
			
			predarr= DBMS.predTable(selimp.getWhrInfoArr());
			
			if(predarr.get(0).isShouldSequence() != false)
			predarr.get(0).setSequence(1);
			
			pre.printTable(new DbmsPrinter(), predarr);		
		}
		
		else if(only1IndonJoinCol==false)
		{
			//System.out.println("Both table has index.....");
			ArrayList<Integer> noOfMatchColT1 = selimp.getMatchColsT1();
			ArrayList<Integer> noOfMatchColT2 = selimp.getMatchColsT2();
			
			int bestMatchColT1 = 0;
			int bestmatColPosT1 = 0;
			int bestMatchColT2 = 0;
			int bestmatColPosT2 = 0;
			
			
			for(int i=0; i<noOfMatchColT1.size(); i++)
			{
				if(noOfMatchColT1.get(i)> bestMatchColT1)
				{
					bestMatchColT1= noOfMatchColT1.get(i);
					bestmatColPosT1 = i;
				//	System.out.println("On first table: best matchcol and its pos is "+bestMatchColT1+" "+bestmatColPosT1);
				}
				
			}
			
			//System.out.println("No of match col in t2: "+noOfMatchColT2);
			
			for(int i=0; i<noOfMatchColT2.size(); i++)
			{
				if(noOfMatchColT2.get(i)> bestMatchColT2)
				{
					bestMatchColT2= noOfMatchColT2.get(i);
					bestmatColPosT2 = i;
				//	System.out.println("On second table: best matchcol and its pos is "+bestMatchColT2+" "+bestmatColPosT2);
				}
				
			}
			
			int bestMatchCol = 0, bestMAtchColPos = 0;
			//compare matchcol from 2 tables and set inner outer table
			
			if(bestMatchColT1>bestMatchColT2)
			{
				innerTable = tab1.getTableName();
				leadTable = tab2.getTableName();
				bestMatchCol = bestMatchColT1;
				bestMAtchColPos = bestmatColPosT1;
			}
			else if(bestMatchColT1<bestMatchColT2)
			{
				innerTable = tab2.getTableName();
				leadTable = tab1.getTableName();
				bestMatchCol = bestMatchColT2;
				bestMAtchColPos = bestmatColPosT2;
			}
			else if(bestMatchColT1 == bestMatchColT2)
			{
				//System.out.println("No of match cols is same");
				if(card1>card2)
				{	//System.out.println("Card1 is better: "+card1);
					innerTable = tab1.getTableName();
					leadTable = tab2.getTableName();
					bestMatchCol = bestMatchColT2;
					bestMAtchColPos = bestmatColPosT1;
					
				}
				else
				{
					//System.out.println("Card2 is better: "+card2);
					//System.out.println("Lead table is... ");
					innerTable = tab2.getTableName();
					leadTable = tab1.getTableName();
					bestMatchCol = bestMatchColT2;
					bestMAtchColPos = bestmatColPosT2;
				}
			}
			// get best index name and no of match cols
			
			String bestIndName = "";
	
			for(Table t : tables )
			{	if(t.getTableName().equalsIgnoreCase(innerTable))
				{	ArrayList<Index> i = t.getIndexes();
					for(int j=0; j<i.size(); j++)
					{	Index ind = i.get(j);
						if(j==bestMAtchColPos)
						{	bestIndName = ind.getIdxName();
						}
					}
				}
			}
			
			plantab.setAccessType('I');
			plantab.setMatchCols(bestMatchCol);
			plantab.setAccessName(bestIndName);
			plantab.setLeadTable(leadTable);
			
			checkObyIndex(plantab, innerTable, bestIndName ,bestMAtchColPos);
			plantab.printTable(new DbmsPrinter());
			
			predarr= DBMS.predTable(selimp.getWhrInfoArr());
			
			if(predarr.get(0).isShouldSequence() != false)	
			predarr.get(0).setSequence(1);
			pre.printTable(new DbmsPrinter(), predarr);
			
		}
		
		
	}
	
	
	
	
	
	
	
	//...................................Yes index - Yes local predicate..................................
	// 2-b-2-1
	
	public void yesIndexYesLocal()
	{
		
		boolean IndPresonT1 = false;
		boolean IndPresonT2 = false;
		String tabname1 = tab1.getTableName();
		String tabname2= tab2.getTableName();
		ArrayList<String> T1joinColIndxPos = new ArrayList<String>();
		ArrayList<String> T2joinColIndxPos = new ArrayList<String>();
		
		//get join column on T1 index location
		
				for(int i=0; i<selimp.getWhrInfoArr().size(); i++)
				{
					WhereInfo w1= selimp.getWhrInfoArr().get(i);
					
					if(w1.getIsJoinPred()==true && w1.getTNameWhere().equalsIgnoreCase(tabname1))
					{
						T1joinColIndxPos = w1.getWhereIndxPos();
						
					}
				}
				
				
				//get join column on T2 index location
				
				for(int i=0; i<selimp.getWhrInfoArr().size(); i++)
				{
					WhereInfo w1= selimp.getWhrInfoArr().get(i);
					
					if(w1.getIsJoinPred()==true && w1.getTNameWhere().equalsIgnoreCase(tabname2))
					{
						T2joinColIndxPos = w1.getWhereIndxPos();
						
					}
				}
				
				int countOfIndxT1joinCol = 0;
				int countOfIndxT2joinCol = 0;
				
		// Check if index present on T1 and its count
				
				for(int i=0; i<T1joinColIndxPos.size(); i++)
				{
					if(! T1joinColIndxPos.get(i).equals("-"))
					{
						countOfIndxT1joinCol++;
					}
				}
				
				if(countOfIndxT1joinCol==0)
					IndPresonT1 = false;
				else
					IndPresonT1 = true;		
				
		// Check if index present on T2 and its count
				

				for(int i=0; i<T2joinColIndxPos.size(); i++)
				{
					if(! T2joinColIndxPos.get(i).equals("-"))
					{
						countOfIndxT2joinCol++;
					}
				}
				
				
				if(countOfIndxT2joinCol==0)
					IndPresonT2 = false;
				else
					IndPresonT2 = true;
				
				
				Boolean only1IndonJoinCol = false;
	
				//System.out.println("Count of index on join cols is "+countOfIndxT1joinCol +" "+countOfIndxT2joinCol);
					
				if((countOfIndxT1joinCol==0 && countOfIndxT2joinCol!=0) || (countOfIndxT1joinCol!=0 && countOfIndxT2joinCol==0) )
				{
					only1IndonJoinCol = true;
				}
				
				//...Yes index - No local pred - Only 1 ind on join col..Case 1
				//2-b-1-1
				
				if(only1IndonJoinCol==true)
				{
					
					if(countOfIndxT1joinCol==0)
					{
						leadTable = selimp.getFromTables().get(0).getTNameFrom();
						innerTable = selimp.getFromTables().get(1).getTNameFrom();
					}
					
					if(countOfIndxT2joinCol==0)
					{
						leadTable = selimp.getFromTables().get(1).getTNameFrom();
						innerTable = selimp.getFromTables().get(0).getTNameFrom();
					}
					
					//System.out.println("Lead and inner table are:" + leadTable + " " + innerTable);
					int innerTableLoc=0;
					int leadTableLoc=0;
					
					for(int i=0; i<selimp.getFromTables().size(); i++)
					{
						String tname = selimp.getFromTables().get(i).getTNameFrom();
						
						if(tname.equalsIgnoreCase(innerTable))
							innerTableLoc = i;
						
						if(tname.equalsIgnoreCase(leadTable))
							leadTableLoc = i;
					}
					//System.out.println("Inner table loc is "+ innerTableLoc);
					
					ArrayList<Integer> noOfMatchCol;
					if (innerTableLoc==0)
					{ noOfMatchCol = selimp.getMatchColsT1();}
					else
					{ noOfMatchCol = selimp.getMatchColsT2();}
						
					//System.out.println("No of match cols arraylist: "+noOfMatchCol);
					int maxMatchCol = 0;
					int maxMatchPos = 0;
					
					for(int i=0; i<noOfMatchCol.size(); i++)
					{
						if(noOfMatchCol.get(i)>maxMatchCol)
						{
							maxMatchCol= noOfMatchCol.get(i);
							maxMatchPos = i;
						}
						
					}
					
					//System.out.println("Max match position is: "+maxMatchPos);
					
					String bestIndxName ="";
					for(Table t: tables)
					{
						if(t.getTableName().equals(innerTable))
						{
							ArrayList<Index> indxs = t.getIndexes();
							
							for(int j=0; j<indxs.size();j++)
							{
								if(j==maxMatchPos)
								{
									bestIndxName=indxs.get(j).getIdxName();
								}
							}
						}
					}
					
					
					for( WhereInfo w: selimp.getWhrInfoArr())
					{
						if(w.getOp().equalsIgnoreCase("IN"))
						{
							ArrayList<String> indxPos = w.getWhereIndxPos();
						
							for(int i=0; i<indxPos.size(); i++)
							{
								if(i == maxMatchPos)
								{
									if(! indxPos.get(i).equalsIgnoreCase("-"))
									{
										System.out.println("Set in list");
										plantab.setAccessType('N');
									}
								}
							}
						}
					}
					
					if(plantab.accessType != 'N')			//change
					plantab.setAccessType('I');
					
					//plantab.setAccessType('I');
					plantab.setMatchCols(maxMatchCol);
					plantab.setAccessName(bestIndxName);
					plantab.setLeadTable(leadTable);
					
					checkObyIndex(plantab, innerTable, bestIndxName ,maxMatchPos);
					
					plantab.printTable(new DbmsPrinter());
					
					predarr= DBMS.predTable(selimp.getWhrInfoArr());
					/*
					predarr.get(0).setSequence(1);
					pre.printTable(new DbmsPrinter(), predarr);
					*/				// CHANGE Q20
					
					//UPTO END OF LOOP...PREDICATE IMPLEMENTATION CHANGED FOR Q20

					
					//............set and print predicate table
						
						
						ArrayList<Float> ffArrT1 = new ArrayList<Float>();
						ArrayList<Float> ffArrT2 = new ArrayList<Float>();
						float f1=0, f2=0;
						
						//LAead table======ffArrT1
						
						for(int j=0; j<selimp.getWhrInfoArr().size(); j++)
						{
							WhereInfo w1 = selimp.getWhrInfoArr().get(j);
							if(w1.getisShouldSequence() != false)
							{
							if(w1.getTNameWhere().equalsIgnoreCase(leadTable) && w1.getIsJoinPred()==false)
							{
									f1 = w1.getFf();
									ffArrT1.add(f1);
							}
							}
							
						}
						
						for(int j=0; j<selimp.getWhrInfoArr().size(); j++)
						{
							WhereInfo w1 = selimp.getWhrInfoArr().get(j);
							if(w1.getisShouldSequence() != false)
							{
							if(w1.getTNameWhere().equalsIgnoreCase(innerTable) && w1.getIsJoinPred()==false)
							{
									f2 = w1.getFf();
									ffArrT2.add(f2);
							}
							}
						}
						
						Collections.sort(ffArrT1);
						//System.out.println("SORTED LEAD TABLE FF: "+ffArrT1);
						Collections.sort(ffArrT2);
						//System.out.println("SORTED INNER TABLE FF: "+ffArrT2);
						
						int seq1=0;
						
						ArrayList<Predicate> predarr= DBMS.predTable(selimp.getWhrInfoArr());
						
						for(int i=0; i<predarr.size(); i++)
						{		int seq =0;
								Predicate pp = predarr.get(i);
								double ff1 = pp.getFf1();
								if(pp.isShouldSequence() != false)
								{
								if(pp.text.contains(leadTable) && pp.getFf2()==0)
								{		
								for(int j=0;j<ffArrT1.size();j++)
								{
									if(ffArrT1.get(j) == ff1)
									{
										seq= j+1;
									}
								
									pp.setSequence(seq);
									seq1 = seq;
								}

								}
								}
								
						}
						
						//System.out.println("Seq1 is "+seq1);
						
						for(int i=0; i<predarr.size(); i++)
						{  	
							Predicate pp = predarr.get(i);
							if(pp.isShouldSequence() != false)
							{
							if(pp.getFf2()!=0.0f)
							{
								seq1=seq1+1;
								pp.setSequence(seq1);
							}
							}
						}
						
						for(int i=0; i<predarr.size(); i++)
						{		int seq =seq1;
								Predicate pp = predarr.get(i);
								double ff2 = pp.getFf1();
								if(pp.isShouldSequence() != false)
								{
								if(pp.text.contains(innerTable) && pp.getFf2()==0)
								{		
									
								for(int j=0;j<ffArrT2.size();j++)
								{
									if(ffArrT2.get(j) == ff2)
									{
										seq= seq+j+1;
										pp.setSequence(seq);
									}
								
								}
								}
								}
						}
						
						
						pre.printTable(new DbmsPrinter(),predarr);
					
					
				}
				
				else if(only1IndonJoinCol==false)
				{
					//System.out.println("Both table has index.....");
					ArrayList<Integer> noOfMatchColT1 = selimp.getMatchColsT1();
					ArrayList<Integer> noOfMatchColT2 = selimp.getMatchColsT2();
					
					int bestMatchColT1 = 0;
					int bestmatColPosT1 = 0;
					int bestMatchColT2 = 0;
					int bestmatColPosT2 = 0;
					
					
					for(int i=0; i<noOfMatchColT1.size(); i++)
					{
						if(noOfMatchColT1.get(i)> bestMatchColT1)
						{
							bestMatchColT1= noOfMatchColT1.get(i);
							bestmatColPosT1 = i;
						//	System.out.println("On first table: best matchcol and its pos is "+bestMatchColT1+" "+bestmatColPosT1);
						}
						
					}
					
					//System.out.println("No of match col in t2: "+noOfMatchColT2);
					
					for(int i=0; i<noOfMatchColT2.size(); i++)
					{
						if(noOfMatchColT2.get(i)> bestMatchColT2)
						{
							bestMatchColT2= noOfMatchColT2.get(i);
							bestmatColPosT2 = i;
						//	System.out.println("On second table: best matchcol and its pos is "+bestMatchColT2+" "+bestmatColPosT2);
						}
						
					}
					
					int bestMatchCol = 0, bestMAtchColPos = 0;
					//compare matchcol from 2 tables and set inner outer table
					
					if(bestMatchColT1>bestMatchColT2)
					{
						innerTable = tab1.getTableName();
						leadTable = tab2.getTableName();
						bestMatchCol = bestMatchColT1;
						bestMAtchColPos = bestmatColPosT1;
					}
					else if(bestMatchColT1<bestMatchColT2)
					{
						innerTable = tab2.getTableName();
						leadTable = tab1.getTableName();
						bestMatchCol = bestMatchColT2;
						bestMAtchColPos = bestmatColPosT2;
					}
					else if(bestMatchColT1 == bestMatchColT2)
					{
						//System.out.println("No of match cols is same");
						//
						
						/* Query 14 onwards 
						 * 
						 */
						ArrayList<Float> combFf = new ArrayList<Float>();
						
						for(int i=0 ; i<selimp.getFromTables().size(); i++)
						{
							String tabname= selimp.getFromTables().get(i).getTNameFrom();
							float ff=1;
							
						for(WhereInfo w: selimp.getWhrInfoArr())
						{
							if(w.getTNameWhere().equalsIgnoreCase(tabname))
							{
								ff=ff*w.getFf();
							}
						}	
						combFf.add(ff);
						}
						
						
						//compare combined ff
						
						if(combFf.get(0) < combFf.get(1))
						{
							innerTable = tab1.getTableName();
							leadTable = tab2.getTableName();
						}
						else
						{
							innerTable = tab2.getTableName();
							leadTable = tab1.getTableName();
						}
						
						
						bestMatchCol = bestMatchColT2;
						
						
						
					}
					// get best index name and no of match cols
					
					String bestIndName = "";
			
					for(Table t : tables )
					{	if(t.getTableName().equalsIgnoreCase(innerTable))
						{	ArrayList<Index> i = t.getIndexes();
							for(int j=0; j<i.size(); j++)
							{	Index ind = i.get(j);
								if(j==bestMAtchColPos)
								{	bestIndName = ind.getIdxName();
								}
							}
						}
					}
					
					
					for( WhereInfo w: selimp.getWhrInfoArr())
					{
						if(w.getOp().equalsIgnoreCase("IN"))
						{
							ArrayList<String> indxPos = w.getWhereIndxPos();
						
							for(int i=0; i<indxPos.size(); i++)
							{
								if(i == bestMAtchColPos)
								{
									if(! indxPos.get(i).equalsIgnoreCase("-"))
									{
							//			System.out.println("Set in list");
										plantab.setAccessType('N');
									}
								}
							}
						}
					}
					
					if(plantab.accessType != 'N')			//change
					plantab.setAccessType('I');
					
					
					//plantab.setAccessType('I');
					plantab.setMatchCols(bestMatchCol);
					plantab.setAccessName(bestIndName);
					plantab.setLeadTable(leadTable);
					
					checkObyIndex(plantab, innerTable, bestIndName ,bestMAtchColPos);
					
					plantab.printTable(new DbmsPrinter());
					
					
					
					//............set and print predicate table
						
						
						ArrayList<Float> ffArrT1 = new ArrayList<Float>();
						ArrayList<Float> ffArrT2 = new ArrayList<Float>();
						float f1=0, f2=0;
						
						//LAead table======ffArrT1
						
						for(int j=0; j<selimp.getWhrInfoArr().size(); j++)
						{
							WhereInfo w1 = selimp.getWhrInfoArr().get(j);
							if(w1.getisShouldSequence() != false)
							{
							if(w1.getTNameWhere().equalsIgnoreCase(leadTable) && w1.getIsJoinPred()==false)
							{
									f1 = w1.getFf();
									ffArrT1.add(f1);
							}
							}
							
						}
						
						for(int j=0; j<selimp.getWhrInfoArr().size(); j++)
						{
							WhereInfo w1 = selimp.getWhrInfoArr().get(j);
							if(w1.getisShouldSequence() != false)
							{
							if(w1.getTNameWhere().equalsIgnoreCase(innerTable) && w1.getIsJoinPred()==false)
							{
									f2 = w1.getFf();
									ffArrT2.add(f2);
							}
							}
							
						}
						
						Collections.sort(ffArrT1);
						//System.out.println("SORTED LEAD TABLE FF: "+ffArrT1);
						Collections.sort(ffArrT2);
						//System.out.println("SORTED INNER TABLE FF: "+ffArrT2);
						
						int seq1=0;
						
						ArrayList<Predicate> predarr= DBMS.predTable(selimp.getWhrInfoArr());
						
						for(int i=0; i<predarr.size(); i++)
						{		int seq =0;
								Predicate pp = predarr.get(i);
								double ff1 = pp.getFf1();
								if(pp.isShouldSequence() != false)
								{
								if(pp.text.contains(leadTable) && pp.getFf2()==0)
								{		
								for(int j=0;j<ffArrT1.size();j++)
								{
									if(ffArrT1.get(j) == ff1)
									{
										seq= j+1;
									}
								
									pp.setSequence(seq);
									seq1 = seq;
								}
								}
							
								}
								
						}
						
						//System.out.println("Seq1 is "+seq1);
						
						for(int i=0; i<predarr.size(); i++)
						{  	
							Predicate pp = predarr.get(i);
							if(pp.getFf2()!=0.0f && pp.isShouldSequence() != false)
							{
								seq1=seq1+1;
								pp.setSequence(seq1);
							}
							
						}
						
						for(int i=0; i<predarr.size(); i++)
						{		int seq =seq1;
								Predicate pp = predarr.get(i);
								double ff2 = pp.getFf1();
					
								if(pp.isShouldSequence() != false)
								{
								if(pp.text.contains(innerTable) && pp.getFf2()==0)
								{		
									
								for(int j=0;j<ffArrT2.size();j++)
								{
									if(ffArrT2.get(j) == ff2)
									{
										seq= seq+j+1;
										pp.setSequence(seq);
									}
								
								}
								}
								}
						}
						
						
						pre.printTable(new DbmsPrinter(),predarr);
		
				}
		
		
	}
	
	
}
