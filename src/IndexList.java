import java.util.ArrayList;

public class IndexList {
	public ArrayList<Index> list;

	public IndexList() {
		list = new ArrayList<Index>();
	}

	/**
	 * Prints this index list.
	 */
	public void printTable(DbmsPrinter out) {
		StringBuilder listOut = new StringBuilder();

		int maxCols = 0;
		int keySize = 5;
		for (Index index : list) {
			if (maxCols < index.getIdxKey().size())
				maxCols = index.getIdxKey().size();

			if (index.getKeys().size() > 0) {
				if (keySize < (index.getKeys().get(0).value.length() + 5))
					keySize = index.getKeys().get(0).value.length();
			}
		}

		listOut.append("\nIndex Name   ");
		for (int i = 0; i < maxCols; i++)
			listOut.append("COL" + i + "  ");

		listOut.append(String.format("%-" + keySize + "s", "HiKey"));
		listOut.append(" ");
		listOut.append(String.format("%-" + keySize + "s", "LoKey"));

		listOut.append("\n-------------");
		for (int i = 0; i < maxCols; i++)
			listOut.append("------");

		for (int i = 0; i < keySize * 2; i++)
			listOut.append("-");

		for (Index index : list) {
			listOut.append(String.format("\n%-13s", index.getIdxName()));

			int i = 0;
			for (int j = 0; j < maxCols; j++) {
				if (i < index.getIdxKey().size()) {
					Index.IndexKeyDef def = index.getIdxKey().get(i);
					if (def.descOrder) {
						listOut.append(String.format("%-6s", def.colId + "D"));
					} else {
						listOut.append(String.format("%-6s", def.colId + "A"));
					}

				} else {
					listOut.append(String.format("%-6s", ""));
				}
				i++;
			}

			if (index.getKeys().size() > 0) {
				listOut.append(String.format("%-" + keySize + "s", index
						.getKeys().get(index.getKeys().size() - 1)));
			} else {
				listOut.append(String.format("%-5s", "-"));
			}

			listOut.append(" ");

			if (index.getKeys().size() > 0) {
				listOut.append(String.format("%-" + keySize + "s", index
						.getKeys().get(0)));
			} else {
				listOut.append(String.format("%-5s", "-"));
			}
		}

		out.println(listOut.toString());
	}
}
