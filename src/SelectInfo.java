import java.util.*;

//import Index.IndexKeyDef;


public class SelectInfo {
	
	private String TNameSel;
	private String CNameSel;
	private String selText;
	private ArrayList<String> selIndxPos= new ArrayList<String>();
	
	public String getTNameSel() {
		return TNameSel;
	}
	
	public void setTNameSel(String TNameSel) {
		this.TNameSel = TNameSel;
	}
	
	public String getCNameSel() {
		return CNameSel;
	}
	
	public void setCNameSel(String CNameSel) {
		this.CNameSel = CNameSel;
	}
	
	public String getSelText() {
		return selText;
	}
	
	public void setSelText(String selText) {
		this.selText = selText;
	}
	
	public ArrayList<String> getSelIndxPos() {
		return selIndxPos;
	}
	
	public void setSelIndxPos(ArrayList<Table> tables) {
		
		int selColid=0,colPos=0;
		
		ArrayList<Index.IndexKeyDef> idef;
		
		for (Table tab : tables) {
			if (tab.getTableName().equals(this.TNameSel)) {
				
				//System.out.println("Inside Table");
				
				for(Column col : tab.getColumns() )
				{
					if(col.getColName().equals(this.CNameSel))
					{
						//System.out.println("Inside Column when colname equal"+ this.CNameSel);
						selColid= col.getColId();
						//System.out.println("colid is"+ selColid);
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
						
						if(def.colId == selColid)
							{// System.out.println("colid is equal"+selColid);
								colPos = def.idxColPos;
								//System.out.println("Hello"+ def.idxColPos);
								//System.out.println("colpos is" + colPos);
							  posInStr = String.valueOf(colPos);
							  //System.out.println("posinstr is"+ posInStr);
							}	
					}
					
					selIndxPos.add(posInStr);
					//System.out.println(selIndxPos);
						
					}
					
				}
				
				
			}
		
		
		
		
		
		//this.selIndxPos = selIndxPos;
	}
	
}
