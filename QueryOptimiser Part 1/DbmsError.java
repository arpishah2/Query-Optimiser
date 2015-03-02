
/**
 * CS 286 - Project - Implements Error Exception for DBMS.
 */
public class DbmsError extends Exception {

	private static final long serialVersionUID = 1L;

	// Constructor initializes instance variable to unknown.
	public DbmsError() {
		super("UNKNOWN error occured.");
	}

	// Constructor receives a message.
	public DbmsError(String message) {
		super(message);
	}
}
