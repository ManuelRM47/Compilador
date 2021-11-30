/* MicroJava Scanner Token  (HM 06-12-28)
   =======================
*/
package CFlat;

public class Token {
	public int kind;		        // token kind
	public int line;		        // token line
	public int col;			        // token column
	public int intValue;			// token value (for number and charConst)
	public double doubleValue;
	public int charValue;
	public String string = "";	// token string
	public boolean array = false;
	public boolean varIndex = false;
	public int index;
	public int length;

	public  Token (){

	}

	public Token(Token t) {
		this.kind = t.kind;
		this.line = t.line;
		this.col = t.col;
		this.intValue = t.intValue;
		this.doubleValue = t.doubleValue;
		this.charValue = t.charValue;
		this.string = t.string;
		this.array = t.array;
		this.index = t.index;
		this.length = t.length;
	}
}

