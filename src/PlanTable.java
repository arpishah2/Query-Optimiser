public class PlanTable {

	public int queryBlockNo = 1; // Always 1

	public char accessType = 'R'; // 'R' Table space scan, 'I' Index scan, 'N'
									// IN list index scan

	public int matchCols; // Number of matching columns in INDEX
	public String accessName = ""; // Name of index
	public char indexOnly = 'N'; // 'Y' if index only access

	public char prefetch = ' '; // Blank - no prefetch, 'S' sequential prefetch
	public char sortC_orderBy = 'N'; // 'Y' if sort required

	public int table1Card = 0; // Table 1 cardinality
	public int table2Card = 0; // Table 2 cardinality

	public String leadTable = ""; // leading outer table in NLJ
	public String innerTable = ""; // new inner table in NLJ

	public int getQueryBlockNo() {
		return queryBlockNo;
	}

	public void setQueryBlockNo(int queryBlockNo) {
		this.queryBlockNo = queryBlockNo;
	}

	public char getAccessType() {
		return accessType;
	}

	public void setAccessType(char accessType) {
		this.accessType = accessType;
	}

	public int getMatchCols() {
		return matchCols;
	}

	public void setMatchCols(int matchCols) {
		this.matchCols = matchCols;
	}

	public String getAccessName() {
		return accessName;
	}

	public void setAccessName(String accessName) {
		this.accessName = accessName;
	}

	public char getIndexOnly() {
		return indexOnly;
	}

	public void setIndexOnly(char indexOnly) {
		this.indexOnly = indexOnly;
	}

	public char getPrefetch() {
		return prefetch;
	}

	public void setPrefetch(char prefetch) {
		this.prefetch = prefetch;
	}

	public char getSortC_orderBy() {
		return sortC_orderBy;
	}

	public void setSortC_orderBy(char sortC_orderBy) {
		this.sortC_orderBy = sortC_orderBy;
	}

	public int getTable1Card() {
		return table1Card;
	}

	public void setTable1Card(int table1Card) {
		this.table1Card = table1Card;
	}

	public int getTable2Card() {
		return table2Card;
	}

	public void setTable2Card(int table2Card) {
		this.table2Card = table2Card;
	}

	public String getLeadTable() {
		return leadTable;
	}

	public void setLeadTable(String leadTable) {
		this.leadTable = leadTable;
	}

	public String getInnerTable() {
		return innerTable;
	}

	public void setInnerTable(String innerTable) {
		this.innerTable = innerTable;
	}
	
	public void printTable(DbmsPrinter out) {
		out.println("--------------------------------------------------------------------------------------------------------------");
		out.println(String.format("%-20s %-20s %-15s", "| Plan Table", "| Value", "| Description - Possible Values"));
		out.println("--------------------------------------------------------------------------------------------------------------");
		out.println(String.format("%-20s %-20s %-15s", "| QBlockNo " , "| " + queryBlockNo, "| Always 1 since we only have one block "));
		out.println("--------------------------------------------------------------------------------------------------------------");
		out.println(String.format("%-20s %-20s %-15s", "| Access Type ", "| " + accessType, "| R – TS scan; I – Index Scan; N – IN list index scan "));
		out.println("--------------------------------------------------------------------------------------------------------------");
		out.println(String.format("%-20s %-20s %-15s", "| MatchCols ", "| " + matchCols, "| Number of matched columns in the INDEX key where ACCESSTYPE is I or N "));
		out.println("--------------------------------------------------------------------------------------------------------------");
		out.println(String.format("%-20s %-20s %-15s", "| AccessName ", "| " + accessName, "| Name of index file if ACCESSTYPE is I or N "));
		out.println("--------------------------------------------------------------------------------------------------------------");
		out.println(String.format("%-20s %-20s %-15s", "| IndexOnly ", "| " + indexOnly, "| Y or N"));
		out.println("--------------------------------------------------------------------------------------------------------------");
		out.println(String.format("%-20s %-20s %-15s", "| Prefetch ", "| " + prefetch, "| Blank – no prefetch; S – sequential prefetch "));
		out.println("--------------------------------------------------------------------------------------------------------------");
		out.println(String.format("%-20s %-20s %-15s", "| SortC_OrderBy ", "| " + sortC_orderBy, "| Y or N "));
		out.println("--------------------------------------------------------------------------------------------------------------");
		out.println(String.format("%-20s %-20s %-15s", "| Table1Card ", "| " + table1Card, "| Table 1 Cardinality "));
		out.println("--------------------------------------------------------------------------------------------------------------");
		out.println(String.format("%-20s %-20s %-15s", "| Table1Card ", "| " + table2Card, "| Table 2 Cardinality "));
		out.println("--------------------------------------------------------------------------------------------------------------");
		out.println(String.format("%-20s %-20s %-15s", "| LeadingTable ", "| " + leadTable, "| Table name of the outer table in NLJ "));
		out.println("--------------------------------------------------------------------------------------------------------------");
	}
}
