import java.util.ArrayList;
import java.util.Arrays;

//import Column.ColType;


public class SelectImplem {
 	
private ArrayList<SelectInfo> selInfoArr = new ArrayList<SelectInfo>();			//all select info
private	ArrayList<WhereInfo> whrInfoArr = new ArrayList<WhereInfo>();			//all where info
private ArrayList<FromInfo> fromTables = new ArrayList<FromInfo>();				//all from info
private	ArrayList<OrderByInfo> ordInfoArr = new ArrayList<OrderByInfo>();		//all order by



/* 
 * 
 * 			For Select Clause
 * 			
 * 
 */


private ArrayList<String> indxPos1S = new ArrayList<String>();				//gets ind pos
private ArrayList<ArrayList<String>> arr1S = new ArrayList<ArrayList<String>>();		//stores row wise- table seperated
private ArrayList<ArrayList<String>> arr2S = new ArrayList<ArrayList<String>>();

private ArrayList<ArrayList<String>> arcol1S = new ArrayList<ArrayList<String>>(); 	//stores info colwise (acc to index)
private ArrayList<ArrayList<String>> arcol2S= new ArrayList<ArrayList<String>>();

private ArrayList<Integer> matchColsT1S = new ArrayList<Integer>();		// no of matchcolumns for every index
private ArrayList<Integer> matchColsT2S = new ArrayList<Integer>();

private ArrayList<String> indextypeT1S = new ArrayList<String>();		//index type for every index: M/S/N
private ArrayList<String> indextypeT2S = new ArrayList<String>();

private ArrayList<Boolean> indonT1S = new ArrayList<Boolean>();			//Can index be used..does prediacte use it
private ArrayList<Boolean> indonT2S = new ArrayList<Boolean>();

private ArrayList<Integer> predinIndT1S = new ArrayList<Integer>();		// no of pred that uses index
private ArrayList<Integer> predinIndT2S = new ArrayList<Integer>();

private ArrayList<Boolean> indOnlyPossibleT1S = new ArrayList<Boolean>();
private ArrayList<Boolean> indOnlyPossibleT2S = new ArrayList<Boolean>();

/* 
 * 
 * 			For Where Clause
 * 			
 * 
 */



private ArrayList<String> indxPos1 = new ArrayList<String>();	
//gets ind pos


private ArrayList<ArrayList<String>> arr1 = new ArrayList<ArrayList<String>>();		//stores row wise- table seperated
private ArrayList<ArrayList<String>> arr2 = new ArrayList<ArrayList<String>>();

private ArrayList<ArrayList<String>> arcol1 = new ArrayList<ArrayList<String>>(); 	//stores info colwise (acc to index)
private ArrayList<ArrayList<String>> arcol2= new ArrayList<ArrayList<String>>();

private ArrayList<Integer> matchColsT1 = new ArrayList<Integer>();		// no of matchcolumns for every index
private ArrayList<Integer> matchColsT2 = new ArrayList<Integer>();

private ArrayList<String> indextypeT1 = new ArrayList<String>();		//index type for every index: M/S/N
private ArrayList<String> indextypeT2 = new ArrayList<String>();

private ArrayList<Boolean> indonT1 = new ArrayList<Boolean>();			//Can index be used..does prediacte use it
private ArrayList<Boolean> indonT2 = new ArrayList<Boolean>();

private ArrayList<Integer> predinIndT1 = new ArrayList<Integer>();		// no of pred that uses index
private ArrayList<Integer> predinIndT2 = new ArrayList<Integer>();

private ArrayList<Boolean> indOnlyPossibleT1 = new ArrayList<Boolean>();		//ind only possib for where clause



private ArrayList<Boolean> indOnlyPossibleT2 = new ArrayList<Boolean>();


/* 
 * 
 * 			For OrderBy Clause
 * 			
 * 
 */

private ArrayList<String> indxPos1O = new ArrayList<String>();				//gets ind pos
private ArrayList<ArrayList<String>> arr1O = new ArrayList<ArrayList<String>>();		//stores row wise- table seperated
private ArrayList<ArrayList<String>> arr2O = new ArrayList<ArrayList<String>>();

private ArrayList<ArrayList<String>> arcol1O = new ArrayList<ArrayList<String>>(); 	//stores info colwise (acc to index)
private ArrayList<ArrayList<String>> arcol2O= new ArrayList<ArrayList<String>>();

private ArrayList<Integer> matchColsT1O = new ArrayList<Integer>();		// no of matchcolumns for every index
private ArrayList<Integer> matchColsT2O = new ArrayList<Integer>();

private ArrayList<String> indextypeT1O = new ArrayList<String>();		//index type for every index: M/S/N
private ArrayList<String> indextypeT2O = new ArrayList<String>();

private ArrayList<Boolean> indonT1O = new ArrayList<Boolean>();			//Can index be used..does prediacte use it
private ArrayList<Boolean> indonT2O = new ArrayList<Boolean>();

private ArrayList<Integer> predinIndT1O = new ArrayList<Integer>();		// no of pred that uses index
private ArrayList<Integer> predinIndT2O = new ArrayList<Integer>();


public SelectImplem(ArrayList<SelectInfo> s,  ArrayList<FromInfo> f,ArrayList<WhereInfo> w, ArrayList<OrderByInfo> o)
{
	this.selInfoArr = s;
	this.whrInfoArr = w;
	this.fromTables = f;
	this.ordInfoArr = o;
	
	
	
}








/*
 * 
 * 
 * 
 * 
 * 
 * 			Select
 * 
 * 
 * 
 */

public void selGetIndexPosS()
{
	if(! selInfoArr.isEmpty())
	{
		for (SelectInfo s : selInfoArr)
	{
		String tname = s.getTNameSel();
		//System.out.println("Table name is"+tname);
		
		if (tname.equalsIgnoreCase(fromTables.get(0).getTNameFrom()))
		{		
			indxPos1=s.getSelIndxPos();
			//System.out.println("\n Index position is "+indxPos1);
			
			
			arr1S.add(indxPos1);
			//System.out.println("Array list of arraylist of strings"+arr1S);
			
		}
		
		//System.out.println("From table size"+fromTables.size());
		
		if(fromTables.size() == 2)
		{
		if (tname.equalsIgnoreCase(fromTables.get(1).getTNameFrom()))
		{
			
			indxPos1=s.getSelIndxPos();
			//System.out.println("\n Index position is "+indxPos1);
			
			arr2S.add(indxPos1);
			//System.out.println("Array list of arraylist of strings"+arr2S);
			
		}
		
		//System.out.println("Arr2" +arr2S);
		}
		
			
}
	}
	
}


//Get index positions for first table, index column wise

	public void chooseIndexSel()
	{		
		if(! selInfoArr.isEmpty())
		{
		for(int j=0; j<arr1S.get(0).size();j++)
		{
			String val[] = new String[arr1S.size()];
			
			for(int i=0; i<arr1S.size(); i++)
			{
				val[i] = arr1S.get(i).get(j);
			}
			
			ArrayList<String> val1 = new ArrayList<String>(Arrays.asList(val));
			arcol1S.add(val1);
						
		}
		
		
		//System.out.println("Table 1 index column wise "+arcol1S);
		
		
		//Get index positions for second table, index column wise
		
		if(fromTables.size()==2)
		{
		if(! arr2S.isEmpty())
		{
		for(int j=0; j<arr2S.get(0).size();j++)
		{
			String val[] = new String[arr2S.size()];
			
			for(int i=0; i<arr2S.size(); i++)
			{
				val[i] = arr2S.get(i).get(j);
			}
			
			ArrayList<String> val1 = new ArrayList<String>(Arrays.asList(val));
			arcol2S.add(val1);
						
		}
		}
		
		//System.out.println("Table 2 index column wise "+arcol2S);
		
		}
		/*
		System.out.println("T1 "+arcol1S);
		System.out.println("T2 "+arcol2S);
		*/
		//call matchcols method
		noMatchColsS();
		}
		
	}	
	
	
	//Can index be used for predicates supplied 
	
	public void indexUseS()
	{
		//Table T1

		
		
		for(ArrayList<String> s: arcol1S)
		{
			Boolean indonly = true;
			int count1=0;
			//System.out.println("For T1");
			
			for(int i=0; i<s.size();i++)
			{
				if(s.get(i).equals("-"))
				{
					indonly = false;
				//	System.out.println("- found");
				}
				else
				{	count1=count1+1;
					//System.out.println("count for T1"+count1);
				}
			}
			
			indOnlyPossibleT1S.add(indonly);
			
			if(count1==0)
			{
				indonT1S.add(false);
				//indextypeT1.add("N");
			}
			else
			{
				indonT1S.add(true);
			}
			
			predinIndT1S.add(count1);
			
		}	
		
		//Table T2
		
		if(fromTables.size() == 2)
		{
		for(ArrayList<String> s: arcol2S)
		{
			Boolean indonly = true;
			int count2=0;
			//System.out.println("For T2");
			
			for(int i=0; i<s.size();i++)
			{
				if(s.get(i).equals("-"))
				{
					indonly = false;
				//	System.out.println("- found");
				}
				else
				{	count2=count2+1;
					//System.out.println("count for T2"+count2);
				}
			}
			
			indOnlyPossibleT2S.add(indonly);
			
			if(count2==0)
			{
				indonT2S.add(false);
			}
			else
			{
				indonT2S.add(true);
			}
			
			predinIndT2S.add(count2);
			
		}	
			
		}
		
		/*System.out.println("index on T1 is"+indonT1S);
		System.out.println("index on T1 on every column which can be used"+predinIndT1S);
		System.out.println("index on T2 is"+indonT2S);
		System.out.println("index on T2 on every column which can be used"+predinIndT2S);
		System.out.println("index only possible for T1" + indOnlyPossibleT1S );
		System.out.println("index only possible for T2" + indOnlyPossibleT2S );*/
	}
	
	
	
	
	
	
	
	//calculates no of matching columns per index and stores it in a Array-list
	
	public void noMatchColsS()
	{
		indexUseS();
		
		int matchcolsT1,matchcolsT2;
		String indextype="";
		
	
		for(ArrayList<String> s: arcol1S)
		{
			//System.out.println("For T1");
			matchcolsT1=0;
			
			for(int i=0; i<6; i++)
			{
				for(int j=0; j<s.size(); j++)
				{
					
					if(s.get(j).equals(String.valueOf(i+1)) && matchcolsT1 == i)
					{
				//		System.out.println("Matchcol val is"+matchcolsT1);
						matchcolsT1=i+1;
					}				
				}
			}
			matchColsT1S.add(matchcolsT1);
		}
		
		//System.out.println("No of matching cols in table1 is" + matchColsT1S);
	
		//For table 2, if it exists
		
		if(fromTables.size() == 2)
		{
			//System.out.println("For T2");
			
			for(ArrayList<String> s: arcol2S)
			{
				matchcolsT2=0;
				
				for(int i=0; i<6; i++)
				{
					for(int j=0; j<s.size(); j++)
					{
						if(s.get(j).equals(String.valueOf(i+1)) && matchcolsT2 == i)
						{
							matchcolsT2=i+1;
				//			System.out.println("Matchcol val is for T2"+matchcolsT2);
						}				
					}
				}
				
				matchColsT2S.add(matchcolsT2);
			}
			
			//System.out.println("No of matching cols in table2 is" + matchColsT2S);
			
		}
		
		indextypeS();
		/*
		System.out.println();
		System.out.println("No of matching cols in table1 is" + matchColsT1S);
		System.out.println("No of predicates using index is"+predinIndT1S);
		System.out.println("Index type for T1"+indextypeT1S);
		
		System.out.println("No of matching cols in table2 is" + matchColsT2S);
		System.out.println("No of predicates using index is"+predinIndT2S);
		System.out.println("Index type for T2"+indextypeT2S);*/
	}
	
	
	public void indextypeS()
	{
		
		for(int i=0; i<arcol1S.size();i++)
		{
			
			ArrayList<String> s1 = arcol1S.get(i);
			int t1 = matchColsT1S.get(i);
			int t2 = predinIndT1S.get(i);
			
			if(t1==0 && t2==0)
			{
				indextypeT1S.add("None");
			}
			else if(t1==0 && t2!=0)
			{
				indextypeT1S.add("S");
			}
			else if( t1 !=0)
			{
				indextypeT1S.add("M");
			}

		}
		
		
		
		for(int i=0; i<arcol2S.size();i++)
		{
			
			ArrayList<String> s1 = arcol2S.get(i);
			int t1 = matchColsT2S.get(i);
			int t2 = predinIndT2S.get(i);
			
			if(t1==0 && t2==0)
			{
				indextypeT2S.add("None");
			}
			else if(t1==0 && t2!=0)
			{
				indextypeT2S.add("S");
			}
			else if( t1 !=0)
			{
				indextypeT2S.add("M");
			}

		}
		
		//System.out.println("Index type for T1"+indextypeT1S);
		//System.out.println("Index type for T2"+indextypeT2S);
		
	}
	


/*
 * 
 * 
 * 
 * 			Where
 * 
 * 
 * 
 * 
 */
// Store index positions seperately for each table

public void selGetIndexPos()
{
	if(! whrInfoArr.isEmpty())
	{
		for (int i=0; i< whrInfoArr.size(); i++)
	{	WhereInfo wi = whrInfoArr.get(i);
	
		String tname = wi.getTNameWhere();
		//System.out.println("Table name is"+tname);
		
		//System.out.println("Table 1 is"+ );
		
		if (tname.equalsIgnoreCase(fromTables.get(0).getTNameFrom()))
		{		
			indxPos1=wi.getWhereIndxPos();
			//System.out.println("\n Index position is "+indxPos1);
			
			
			arr1.add(indxPos1);
			//System.out.println("Array list of arraylist of strings"+arr1);
			
		}
		
		//System.out.println("From table size"+fromTables.size());
		
		if(fromTables.size() == 2)
		{
		if (tname.equalsIgnoreCase(fromTables.get(1).getTNameFrom()))
		{
			
			indxPos1=wi.getWhereIndxPos();
			//System.out.println("\n Index position is "+indxPos1);
			
			arr2.add(indxPos1);
			//System.out.println("Array list of arraylist of strings"+arr2);
			
		}
		
		//System.out.println("Arr2" +arr2);
		}
		
			
}
	}
	
}


//Get index positions for first table, index column wise

	public void chooseIndexWhere()
	{		
		if(! whrInfoArr.isEmpty())
		{
		for(int j=0; j<arr1.get(0).size();j++)
		{
			String val[] = new String[arr1.size()];
			
			for(int i=0; i<arr1.size(); i++)
			{
			//	System.out.println("error in here"+ arr1.get(i).get(j));
				val[i] = arr1.get(i).get(j);
			}
			
			ArrayList<String> val1 = new ArrayList<String>(Arrays.asList(val));
			arcol1.add(val1);
						
		}
		
		
		//System.out.println("Table 1 index column wise "+arcol1);
		
		
		//Get index positions for second table, index column wise
		
		if(fromTables.size()==2)
		{
		if(! arr2.isEmpty())
		{
		for(int j=0; j<arr2.get(0).size();j++)
		{
			String val[] = new String[arr2.size()];
			
			for(int i=0; i<arr2.size(); i++)
			{
				val[i] = arr2.get(i).get(j);
			}
			
			ArrayList<String> val1 = new ArrayList<String>(Arrays.asList(val));
			arcol2.add(val1);
						
		}
		}
		
		//System.out.println("Table 2 index column wise "+arcol2);
		
		}
		/*
		System.out.println("T1 "+arcol1);
		System.out.println("T2 "+arcol2);*/
		
		//call matchcols method
		noMatchCols();
		}
		
	}	
	
	
	//Can index be used for predicates supplied 
	
	public void indexUse()
	{
		//Table T1

		for(ArrayList<String> s: arcol1)
		{
			Boolean indonly = true;
			
			int count1=0;
			//System.out.println("For T1");
			
			for(int i=0; i<s.size();i++)
			{
				if(s.get(i).equals("-"))
				{
					indonly = false;
				//	System.out.println("- found");
				}
				else
				{	count1=count1+1;
					//System.out.println("count for T1"+count1);
				}
			}
			
			indOnlyPossibleT1.add(indonly);
			
			if(count1==0)
			{
				indonT1.add(false);
				//indextypeT1.add("N");
			}
			else
			{
				indonT1.add(true);
			}
			
			predinIndT1.add(count1);
			
		}	
		
		//Table T2
		
		if(fromTables.size() == 2)
		{
		for(ArrayList<String> s: arcol2)
		{
			int count2=0;
			Boolean indonly = true;
			//System.out.println("For T2");
			
			for(int i=0; i<s.size();i++)
			{
				if(s.get(i).equals("-"))
				{
					indonly = false;
					//System.out.println("- found");
				}
				else
				{	count2=count2+1;
				//	System.out.println("count for T2"+count2);
				}
			}
			
			indOnlyPossibleT2.add(indonly);
			
			if(count2==0)
			{
				indonT2.add(false);
			}
			else
			{
				indonT2.add(true);
			}
			
			predinIndT2.add(count2);
			
		}	
			
		}
		/*
		System.out.println("index on T1 is"+indonT1);
		System.out.println("index on T1 on every column which can be used"+predinIndT1);
		System.out.println("index on T2 is"+indonT2);
		System.out.println("index on T2 on every column which can be used"+predinIndT2);*/
	}
	
	
	
	
	
	
	
	//calculates no of matching columns per index and stores it in a Array-list
	
	public void noMatchCols()
	{
		indexUse();
		
		int matchcolsT1,matchcolsT2;
		String indextype="";
		ArrayList<ArrayList<Integer>> indxMatchPredPos = new ArrayList<ArrayList<Integer>>();
		//ArrayList<Integer> predPosMatch = new ArrayList<Integer>();
		
		for(ArrayList<String> s: arcol1)
		{
			//System.out.println("For T1");
			matchcolsT1=0;
			
			
			for(int i=0; i<6; i++)
			{
				//System.out.println("Here");
				for(int j=0; j<s.size(); j++)
				{
					
					if(s.get(j).equals(String.valueOf(i+1)) && matchcolsT1 == i)
					{
						//predPosMatch.add(j);
						//System.out.println("Pred position for"+s +"is "+predPosMatch);
						System.out.println("Matchcol val is"+matchcolsT1);
						matchcolsT1=i+1;
					}
					
					
				}
			}
			//indxMatchPredPos.add(predPosMatch);
			matchColsT1.add(matchcolsT1);
		}
		
		//System.out.println("No of matching cols in table1 is" + matchColsT1);
		//System.out.println("Hi...............Predicate pos for matchcols" +predPosMatch);
	
		//For table 2, if it exists
		
		if(fromTables.size() == 2)
		{
			System.out.println("For T2");
			
			for(ArrayList<String> s: arcol2)
			{
				matchcolsT2=0;
				
				for(int i=0; i<6; i++)
				{
					for(int j=0; j<s.size(); j++)
					{
						if(s.get(j).equals(String.valueOf(i+1)) && matchcolsT2 == i)
						{
							matchcolsT2=i+1;
			//				System.out.println("Matchcol val is for T2"+matchcolsT2);
						}				
					}
				}
				
				matchColsT2.add(matchcolsT2);
			}
			
			//System.out.println("No of matching cols in table2 is" + matchColsT2);
			
		}
		
		indextype();
		/*
		System.out.println();
		System.out.println("No of matching cols in table1 is" + matchColsT1);
		System.out.println("No of predicates using index is"+predinIndT1);
		System.out.println("Index type for T1"+indextypeT1);
		
		System.out.println("No of matching cols in table2 is" + matchColsT2);
		System.out.println("No of predicates using index is"+predinIndT2);
		System.out.println("Index type for T2"+indextypeT2);*/
	}
	
	
	public void indextype()
	{
		
		for(int i=0; i<arcol1.size();i++)
		{
			
			ArrayList<String> s1 = arcol1.get(i);
			int t1 = matchColsT1.get(i);
			int t2 = predinIndT1.get(i);
			
			if(t1==0 && t2==0)
			{
				indextypeT1.add("None");
			}
			else if(t1==0 && t2!=0)
			{
				indextypeT1.add("S");
			}
			else if( t1 !=0)
			{
				indextypeT1.add("M");
			}

		}
		
		
		
		for(int i=0; i<arcol2.size();i++)
		{
			
			ArrayList<String> s1 = arcol2.get(i);
			int t1 = matchColsT2.get(i);
			int t2 = predinIndT2.get(i);
			
			if(t1==0 && t2==0)
			{
				indextypeT2.add("None");
			}
			else if(t1==0 && t2!=0)
			{
				indextypeT2.add("S");
			}
			else if( t1 !=0)
			{
				indextypeT2.add("M");
			}

		}
		
		//System.out.println("Index type for T1"+indextypeT1);
		//System.out.println("Index type for T2"+indextypeT2);
		
	}
	
	
	
	

/*
 * 
 * 
 * 
 * 			ORDER BY
 * 
 * 
 * 
 * 
 */
// Store index positions seperately for each table

public void selGetIndexPosO()
{
	if(! ordInfoArr.isEmpty())
	{
		for (OrderByInfo o : ordInfoArr)
	{
		String tname = o.getOrderTabName();
		//System.out.println("Table name is"+tname);
		
		if (tname.equalsIgnoreCase(fromTables.get(0).getTNameFrom()))
		{		
			indxPos1=o.getOrderIndxPos();
			//System.out.println("\n Index position is "+indxPos1);
			
			
			arr1O.add(indxPos1);
			//System.out.println("Array list of arraylist of strings"+arr1O);
			
		}
		
		//System.out.println("From table size"+fromTables.size());
		
		if(fromTables.size() == 2)
		{
		if (tname.equalsIgnoreCase(fromTables.get(1).getTNameFrom()))
		{
			
			indxPos1=o.getOrderIndxPos();
			//System.out.println("\n Index position is "+indxPos1);
			
			arr2O.add(indxPos1);
			//System.out.println("Array list of arraylist of strings"+arr2O);
			
		}
		
		//System.out.println("Arr2" +arr2O);
		}
		
			
}
	}
	
}


//Get index positions for first table, index column wise

	public void chooseIndexWhereO()
	{		
		if(! ordInfoArr.isEmpty())
		{
		for(int j=0; j<arr1O.get(0).size();j++)
		{
			String val[] = new String[arr1O.size()];
			
			for(int i=0; i<arr1O.size(); i++)
			{
				val[i] = arr1O.get(i).get(j);
			}
			
			ArrayList<String> val1 = new ArrayList<String>(Arrays.asList(val));
			arcol1O.add(val1);
						
		}
		
		
		//System.out.println("Table 1 index column wise "+arcol1O);
		
		
		//Get index positions for second table, index column wise
		
		if(fromTables.size()==2)
		{
		if(! arr2O.isEmpty())
		{
		for(int j=0; j<arr2O.get(0).size();j++)
		{
			String val[] = new String[arr2O.size()];
			
			for(int i=0; i<arr2O.size(); i++)
			{
				val[i] = arr2O.get(i).get(j);
			}
			
			ArrayList<String> val1 = new ArrayList<String>(Arrays.asList(val));
			arcol2O.add(val1);
						
		}
		}
		
		//System.out.println("Table 2 index column wise "+arcol2O);
		
		}
		
		//System.out.println("T1 "+arcol1O);
		//System.out.println("T2 "+arcol2O);
		
		//call matchcols method
		noMatchColsO();
		}
		
	}	
	
	
	//Can index be used for predicates supplied 
	
	public void indexUseO()
	{
		//Table T1

		for(ArrayList<String> s: arcol1O)
		{
			int count1=0;
			//System.out.println("For T1");
			
			for(int i=0; i<s.size();i++)
			{
				if(s.get(i).equals("-"))
				{
				//	System.out.println("- found");
				}
				else
				{	count1=count1+1;
					//System.out.println("count for T1"+count1);
				}
			}
			
			if(count1==0)
			{
				indonT1O.add(false);
				//indextypeT1.add("N");
			}
			else
			{
				indonT1O.add(true);
			}
			
			predinIndT1O.add(count1);
			
		}	
		
		//Table T2
		
		if(fromTables.size() == 2)
		{
		for(ArrayList<String> s: arcol2O)
		{
			int count2=0;
			//System.out.println("For T2");
			
			for(int i=0; i<s.size();i++)
			{
				if(s.get(i).equals("-"))
				{
				//	System.out.println("- found");
				}
				else
				{	count2=count2+1;
					//System.out.println("count for T2"+count2);
				}
			}
			
			if(count2==0)
			{
				indonT2O.add(false);
			}
			else
			{
				indonT2O.add(true);
			}
			
			predinIndT2O.add(count2);
			
		}	
			
		}
		/*
		System.out.println("index on T1 is"+indonT1O);
		System.out.println("index on T1 on every column which can be used"+predinIndT1O);
		System.out.println("index on T2 is"+indonT2O);
		System.out.println("index on T2 on every column which can be used"+predinIndT2O);*/
	}
	
	
	
	
	
	
	
	//calculates no of matching columns per index and stores it in a Array-list
	
	public void noMatchColsO()
	{
		indexUseO();
		
		int matchcolsT1,matchcolsT2;
		String indextype="";
	
		for(ArrayList<String> s: arcol1O)
		{
			System.out.println("For T1");
			matchcolsT1=0;
			
			for(int i=0; i<6; i++)
			{
				for(int j=0; j<s.size(); j++)
				{
					
					if(s.get(j).equals(String.valueOf(i+1)) && matchcolsT1 == i)
					{
	//					System.out.println("Matchcol val is"+matchcolsT1);
						matchcolsT1=i+1;
					}				
				}
			}
			matchColsT1O.add(matchcolsT1);
		}
		
		//System.out.println("No of matching cols in table1 is" + matchColsT1O);
	
		//For table 2, if it exists
		
		if(fromTables.size() == 2)
		{
			//System.out.println("For T2");
			
			for(ArrayList<String> s: arcol2O)
			{
				matchcolsT2=0;
				
				for(int i=0; i<6; i++)
				{
					for(int j=0; j<s.size(); j++)
					{
						if(s.get(j).equals(String.valueOf(i+1)) && matchcolsT2 == i)
						{
							matchcolsT2=i+1;
				//			System.out.println("Matchcol val is for T2"+matchcolsT2);
						}				
					}
				}
				
				matchColsT2O.add(matchcolsT2);
			}
			
			//System.out.println("No of matching cols in table2 is" + matchColsT2O);
			
		}
		
		indextypeO();
		/*
		System.out.println();
		System.out.println("No of matching cols in table1 is" + matchColsT1O);
		System.out.println("No of predicates using index is"+predinIndT1O);
		System.out.println("Index type for T1"+indextypeT1O);
		
		System.out.println("No of matching cols in table2 is" + matchColsT2O);
		System.out.println("No of predicates using index is"+predinIndT2O);
		System.out.println("Index type for T2"+indextypeT2O);*/
	}
	
	
	public void indextypeO()
	{
		
		for(int i=0; i<arcol1O.size();i++)
		{
			//System.out.println("there must be values: "+arcol1O.size());
			ArrayList<String> s1 = arcol1O.get(i);
			int t1 = matchColsT1O.get(i);
			int t2 = predinIndT1O.get(i);
			
			if(t1==0 && t2==0)
			{
				indextypeT1O.add("None");
			}
			else if(t1==0 && t2!=0)
			{
				indextypeT1O.add("S");
			}
			else if( t1 !=0)
			{
				indextypeT1O.add("M");
			}

		}
		
		
		
		for(int i=0; i<arcol2O.size();i++)
		{
			
			ArrayList<String> s1 = arcol2O.get(i);
			int t1 = matchColsT2O.get(i);
			int t2 = predinIndT2O.get(i);
			
			if(t1==0 && t2==0)
			{
				indextypeT2O.add("None");
			}
			else if(t1==0 && t2!=0)
			{
				indextypeT2O.add("S");
			}
			else if( t1 !=0)
			{
				indextypeT2O.add("M");
			}

		}
		
		//System.out.println("Index type for T1"+indextypeT1O);
		//System.out.println("Index type for T2"+indextypeT2O);
		
	}
	
	
	public ArrayList<String> getIndxPos1S() {
		return indxPos1S;
	}

	public ArrayList<ArrayList<String>> getArr1S() {
		return arr1S;
	}

	public ArrayList<ArrayList<String>> getArr2S() {
		return arr2S;
	}

	public ArrayList<ArrayList<String>> getArcol1S() {
		return arcol1S;
	}

	public ArrayList<ArrayList<String>> getArcol2S() {
		return arcol2S;
	}

	public ArrayList<Integer> getMatchColsT1S() {
		return matchColsT1S;
	}

	public ArrayList<Integer> getMatchColsT2S() {
		return matchColsT2S;
	}

	public ArrayList<String> getIndextypeT1S() {
		return indextypeT1S;
	}

	public ArrayList<String> getIndextypeT2S() {
		return indextypeT2S;
	}

	public ArrayList<Boolean> getIndonT1S() {
		return indonT1S;
	}

	public ArrayList<Boolean> getIndonT2S() {
		return indonT2S;
	}

	public ArrayList<Integer> getPredinIndT1S() {
		return predinIndT1S;
	}

	public ArrayList<Integer> getPredinIndT2S() {
		return predinIndT2S;
	}

	public ArrayList<Boolean> getIndOnlyPossibleT1S() {
		return indOnlyPossibleT1S;
	}

	public ArrayList<Boolean> getIndOnlyPossibleT2S() {
		return indOnlyPossibleT2S;
	}

	
	public ArrayList<String> getIndxPos1() {
		return indxPos1;
	}

	public ArrayList<ArrayList<String>> getArr1() {
		return arr1;
	}

	public ArrayList<ArrayList<String>> getArr2() {
		return arr2;
	}

	public ArrayList<ArrayList<String>> getArcol1() {
		return arcol1;
	}

	public ArrayList<ArrayList<String>> getArcol2() {
		return arcol2;
	}

	public ArrayList<Integer> getMatchColsT1() {
		return matchColsT1;
	}

	public ArrayList<Integer> getMatchColsT2() {
		return matchColsT2;
	}

	public ArrayList<String> getIndextypeT1() {
		return indextypeT1;
	}

	public ArrayList<String> getIndextypeT2() {
		return indextypeT2;
	}

	public ArrayList<Boolean> getIndonT1() {
		return indonT1;
	}

	public ArrayList<Boolean> getIndonT2() {
		return indonT2;
	}

	public ArrayList<Integer> getPredinIndT1() {
		return predinIndT1;
	}

	public ArrayList<Integer> getPredinIndT2() {
		return predinIndT2;
	}

	public ArrayList<String> getIndxPos1O() {
		return indxPos1O;
	}

	public ArrayList<ArrayList<String>> getArr1O() {
		return arr1O;
	}

	public ArrayList<ArrayList<String>> getArr2O() {
		return arr2O;
	}

	public ArrayList<ArrayList<String>> getArcol1O() {
		return arcol1O;
	}

	public ArrayList<ArrayList<String>> getArcol2O() {
		return arcol2O;
	}

	public ArrayList<Integer> getMatchColsT1O() {
		return matchColsT1O;
	}

	public ArrayList<Integer> getMatchColsT2O() {
		return matchColsT2O;
	}

	public ArrayList<String> getIndextypeT1O() {
		return indextypeT1O;
	}

	public ArrayList<String> getIndextypeT2O() {
		return indextypeT2O;
	}

	public ArrayList<Boolean> getIndonT1O() {
		return indonT1O;
	}

	public ArrayList<Boolean> getIndonT2O() {
		return indonT2O;
	}

	public ArrayList<Integer> getPredinIndT1O() {
		return predinIndT1O;
	}

	public ArrayList<Integer> getPredinIndT2O() {
		return predinIndT2O;
	}

	public ArrayList<SelectInfo> getSelInfoArr() {
		return selInfoArr;
	}

	public void setSelInfoArr(ArrayList<SelectInfo> selInfoArr) {
		this.selInfoArr = selInfoArr;
	}

	public ArrayList<WhereInfo> getWhrInfoArr() {
		return whrInfoArr;
	}

	public void setWhrInfoArr(ArrayList<WhereInfo> whrInfoArr) {
		this.whrInfoArr = whrInfoArr;
	}

	public ArrayList<FromInfo> getFromTables() {
		return fromTables;
	}

	public void setFromTables(ArrayList<FromInfo> fromTables) {
		this.fromTables = fromTables;
	}

	public ArrayList<OrderByInfo> getOrdInfoArr() {
		return ordInfoArr;
	}



	public void setOrdInfoArr(ArrayList<OrderByInfo> ordInfoArr) {
		this.ordInfoArr = ordInfoArr;
	}

	public ArrayList<Boolean> getIndOnlyPossibleT1() {
		return indOnlyPossibleT1;
	}

	public ArrayList<Boolean> getIndOnlyPossibleT2() {
		return indOnlyPossibleT2;
	}

	
		
	}




