import java.util.*;


public class OrderByInfo {

	private String orderTabName;
	private String orderColName;
	private String orderText;
	private String descOrd;
	private ArrayList<String> orderIndxPos = new ArrayList<String>();
	
	public String getDescOrd() {
		return descOrd;
	}

	public void setDescOrd(String descOrd) {
		this.descOrd = descOrd;
	}


	
	public String getOrderTabName() {
		return orderTabName;
	}
	
	public void setOrderTabName(String orderTabName) {
		this.orderTabName = orderTabName;
	}
	
	public String getOrderColName() {
		return orderColName;
	}
	
	public void setOrderColName(String orderColName) {
		this.orderColName = orderColName;
	}
	
	public String getOrderText() {
		return orderText;
	}
	
	public void setOrderText(String orderText) {
		this.orderText = orderText;
	}
	
	public ArrayList<String> getOrderIndxPos() {
		return orderIndxPos;
	}
	
	public void setOrderIndxPos(ArrayList<Table> tables) {
	
		int ordColid=0,colPos=0;
		
		ArrayList<Index.IndexKeyDef> idef;
		//System.out.println("Inside Order...............");
		
		for (Table tab : tables) {
			
			if (tab.getTableName().equals(this.orderTabName)) {
				
				//System.out.println("Inside Table");
				
				for(Column col : tab.getColumns() )
				{
					if(col.getColName().equals(this.orderColName))
					{
						//System.out.println("Inside Column when colname equal"+ this.orderColName);
						ordColid= col.getColId();
						//System.out.println("colid is"+ ordColid);
					}
				}	
				
				
				for(Index i1: tab.getIndexes())
				{
					String posInStr="-";
					//System.out.println("Working on single index from getIndexes");
					idef = i1.getIdxKey();
					
					for(Index.IndexKeyDef def: idef)
					{
						//System.out.println("Working on single index def from arraylist index key def");
						
						if(def.colId == ordColid)
							{ //System.out.println("colid is equal"+ordColid);
								colPos = def.idxColPos;
								//System.out.println("Hello"+ def.idxColPos);
								//System.out.println("colpos is" + colPos);
							  posInStr = String.valueOf(colPos);
							  //System.out.println("posinstr is"+ posInStr);
							}	
						//System.out.println("Bye");
					}
					
					//System.out.println("Setting up values");
					orderIndxPos.add(posInStr);
					//System.out.println(orderIndxPos);
						
					}
					
				}
				
				
			}
		
		
		
		
		
	}
	
}
