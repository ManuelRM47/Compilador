/* MicroJava Scanner Token  (HM 06-12-28)
   =======================
*/
package VMmachine;

public class Token {
	public int kind;		// token kind
	public int line;		// token line
	public int col;			// token column
	public int intValue;			// token value (for number and charConst)
	public double doubleValue;
	public int charValue;
	public String string = "";	// token string
}