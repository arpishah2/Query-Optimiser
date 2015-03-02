import java.util.ArrayList;


public class WhereInfo {

	private String TNameWhere ="";
	private String CNameWhere ="";
	private String whereText ="";
	private String op="";
	private String value="";
	private float ff= 0.0f;
	private int card=0;
	private Boolean isJoinPred = false;
	private boolean isInlistOneElem = false;
	private ArrayList<String> whereIndxPos=new ArrayList<String>();
	private boolean isConvInList = false;		//change Q18
	private boolean isAddPredTCP = false;		//Q20 check if working or not
	private boolean shouldSequence = true;
	private boolean diffPredEq = false; 
	
	public boolean getisDiffPredEq() {
		return diffPredEq;
	}

	public void setDiffPredEq(boolean diffPredEq) {
		this.diffPredEq = diffPredEq;
	}

	public boolean getisDiffPred() {
		return diffPred;
	}

	public void setDiffPred(boolean diffPred) {
		this.diffPred = diffPred;
	}

	private boolean diffPred = false;
	
	public boolean getisShouldSequence() {
		return shouldSequence;
	}

	public void setShouldSequence(boolean shouldSequence) {
		this.shouldSequence = shouldSequence;
	}

	public boolean isAddPredTCP() {
		return isAddPredTCP;
	}

	public void setAddPredTCP(boolean isAddPredTCP) {
		this.isAddPredTCP = isAddPredTCP;
	}

	public boolean getIsConvInList() {
		return isConvInList;
	}

	public void setIsConvInList(boolean isConvInList) {
		this.isConvInList = isConvInList;
	}

	public boolean getisInlistOneElem() {
		return isInlistOneElem;
	}

	public void setInlistOneElem(boolean isInlistOneElem) {
		this.isInlistOneElem = isInlistOneElem;
	}
	
	public int getCard() {
		return card;
	}

	public void setCard(int card) {
		this.card = card;
	}

	
	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	
	public String getTNameWhere() {
		return TNameWhere;
	}
	
	public void setTNameWhere(String TNameWhere) {
		this.TNameWhere = TNameWhere;
	}
	
	public String getCNameWhere() {
		return CNameWhere;
	}
	
	public void setCNameWhere(String CNameWhere) {
		this.CNameWhere = CNameWhere;
	}
	
	public String getWhereText() {
		return whereText;
	}
	
	public void setWhereText(String whereText) {
		this.whereText = whereText;
	}
	
	public ArrayList<String> getWhereIndxPos() {
		return whereIndxPos;
	}
	
	public void setWhereIndxPos(ArrayList<Table> tables) {
		
		
		int whrColid=0,colPos=0;
		
		ArrayList<Index.IndexKeyDef> idef;
		
		for (Table tab : tables) {
			if (tab.getTableName().equals(this.TNameWhere)) {
				
				//System.out.println("Inside Table");
				
				for(Column col : tab.getColumns() )
				{
					if(col.getColName().equals(this.CNameWhere))
					{
					//	System.out.println("Inside Column when colname equal"+ this.CNameWhere);
						whrColid= col.getColId();
						//System.out.println("colid is"+ whrColid);
					}
				}	
				
				
				for(Index i1: tab.getIndexes())
				{
					//System.out.println("No of index is:"+tab.getNumIndexes());
					String posInStr="-";
					//System.out.println("Working on single index from getIndexes");
					idef = i1.getIdxKey();
					
					for(Index.IndexKeyDef def: idef)
					{
						//System.out.println("Working on single index def from arraylist index key def");
						
						if(def.colId == whrColid)
							{ //System.out.println("colid is equal"+whrColid);
								colPos = def.idxColPos;
								//System.out.println("Hello"+ def.idxColPos);
								//System.out.println("colpos is" + colPos);
							  posInStr = String.valueOf(colPos);
							  //System.out.println("posinstr is"+ posInStr);
							}	
					}
					
					
					whereIndxPos.add(posInStr);
					//System.out.println(whereIndxPos);
						
					}
					
				}
				
				
			}	
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	
	public float getFf() {
		return ff;
	}

	public void setFf(float ff) {
		this.ff = ff;
	}
	
	public Boolean getIsJoinPred() {
		return isJoinPred;
	}

	public void setIsJoinPred(Boolean isJoinPred) {
		this.isJoinPred = isJoinPred;
	}



	
}
