/* MicroJava Scanner (HM 06-12-28)
   =================
*/
package CFlat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

public class Scanner {
	private static final char eofCh = '\u0080';
	private static final char eol = '\n';
	private static final int  // token codes
			none      = 0,
			ident     = 1,
			variable  = 2,
			number    = 3,
			charCon   = 4,
			plus      = 5,
			minus     = 6,
			times     = 7,
			slash     = 8,
			rem       = 9,
			eql       = 10,
			neq       = 11,
			lss       = 12,
			leq       = 13,
			gtr       = 14,
			geq       = 15,
			assign    = 16,
			colon     = 17,
			semicolon = 18,
			comma     = 19,
			period    = 20,
			money     = 21,
			lpar      = 22,
			rpar      = 23,
			lbrack    = 24,
			rbrack    = 25,
			lbrace    = 26,
			rbrace    = 27,
			START_    = 28,
			END_      = 29,
			READ_     = 30,
			PRINT_    = 31,
			PRINTNL_  = 32,
			IF_       = 33,
			ELSE_     = 34,
			FOR_      = 35,
			WHILE_    = 36,
			AND_      = 37,
			OR_       = 38,
			NOT_      = 39,
			eof       = 40,
			INT_      = 41,
			DOUBLE_   = 42,
			STRING_   = 43,
			intV      = 44,
			doubleV   = 45,
			stringV   = 46,
			intAV     = 47,
			doubleAV  = 48,
			stringAV  = 49,
			invalid   = 50;

	private static final String key[] = { // sorted list of keywords
			"AND",
			"DOUBLE",
			"ELSE",
			"END",
			"FOR",
			"IF",
			"INT",
			"NOT",
			"OR",
			"PRINT",
			"PRINTNL",
			"READ",
			"START",
			"STRING",
			"WHILE"
	};

	private static final int keyVal[] = {
			AND_,
			DOUBLE_,
			ELSE_,
			END_,
			FOR_,
			IF_,
			INT_,
			NOT_,
			OR_,
			PRINT_,
			PRINTNL_,
			READ_,
			START_,
			STRING_,
			WHILE_
	};

	private static char ch;			// lookahead character
	public  static int col;			// current column
	public  static int line;		// current line
	private static int pos;			// current position from start of source file
	private static Reader in;  	// source file reader
	private static char[] lex;	// current lexeme (token string)
	public static Pattern stringsPattern = Pattern.compile("[a-zA-Z0-9:!.?'&]");
	public static Pattern lettersAndNumbersPattern = Pattern.compile("[a-zA-Z0-9]");
	public static Pattern lettersPattern = Pattern.compile("[a-zA-Z]");
	public static Pattern numbersPattern = Pattern.compile("[0-9.]");
	public static TableElement tempElement;


	//----- ch = next input character
	private static void nextCh() {
		try {
			ch = (char)in.read();
			col++;
			pos++;
			if (ch == eol) {
				line++;
				col = 0;
			}
			else if (ch == '\uffff')
				ch = eofCh;
		} catch (IOException e) {
			ch = eofCh;
		}
	}

	//--------- Initialize scanner
	public static void init(Reader r) {
		in = new BufferedReader(r);
		lex = new char[64];
		line = 1;
		col = 0;
		nextCh();
	}

	//---------- Return next input token
	public static Token next() {
		while (ch <= ' ') {
			nextCh();
		}
		Token t = new Token();
		t.line = line;
		t.col = col;
		switch(ch){
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'h':
			case 'i':
			case 'j':
			case 'k':
			case 'l':
			case 'm':
			case 'n':
			case 'o':
			case 'p':
			case 'q':
			case 'r':
			case 's':
			case 't':
			case 'u':
			case 'v':
			case 'w':
			case 'x':
			case 'y':
			case 'z':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'J':
			case 'K':
			case 'L':
			case 'M':
			case 'N':
			case 'O':
			case 'P':
			case 'Q':
			case 'R':
			case 'S':
			case 'T':
			case 'U':
			case 'V':
			case 'W':
			case 'X':
			case 'Y':
			case 'Z':
				readName(t);
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				readNumber(t);
				break;
			case '"':
				readName(t);
				break;
			case ':':
				t.kind = colon;
				nextCh();
				break;
			case ';':
				t.kind = semicolon;
				nextCh();
				break;
			case '.':
				t.kind = period;
				nextCh();
				break;
			case '+':
				t.kind = plus;
				nextCh();
				break;
			case '-':
				t.kind = minus;
				nextCh();
				break;
			case '*':
				t.kind = times;
				nextCh();
				break;
			case '/':
				t.kind = slash;
				nextCh();
				break;
			case '$':
				boolean nestedComment = true;
				while (nestedComment) {
					nextCh();
					if (ch == '$') {
						nestedComment = false;
						nextCh();
						t = next();
					} else if (ch == eofCh) {
						nestedComment = false;
						t.kind = none;
					}
				}
				break;
			case '%':
				t.kind = rem;
				nextCh();
				break;
			case '=':
				nextCh();
				if(ch == '='){
					t.kind = eql;
					nextCh();
				} else {
					t.kind = assign;
				}
				break;
			case ',':
				t.kind = comma;
				nextCh();
				break;
			case '(':
				t.kind = lpar;
				nextCh();
				break;
			case ')':
				t.kind = rpar;
				nextCh();
				break;
			case '[':
				t.kind = lbrack;
				nextCh();
				break;
			case ']':
				t.kind = rbrack;
				nextCh();
				break;
			case '{':
				t.kind = lbrace;
				nextCh();
				break;
			case '}':
				t.kind = rbrace;
				nextCh();
				break;
			case '\'':
				readCharCon(t);
				break;
			case '!':
				nextCh();
				if(ch == '='){
					t.kind = neq;
				}else{
					t.kind = none; //maybe?
				}
				nextCh();
				break;
			case '<':
				nextCh();
				if(ch == '='){
					t.kind = leq;
				}else{
					t.kind = lss;
				}
				nextCh();
				break;
			case '>':
				nextCh();
				if(ch == '='){
					t.kind = geq;
				}else{
					t.kind = gtr;
				}
				nextCh();
				break;
			case eofCh:
				t.kind = eof;
				nextCh();
				break;
			default:
				t.kind = none;
				nextCh();
				break;
		}
		return t;
	}

	private static void readNumber(Token t) {
		String numberS = "";
		while(numbersPattern.matcher(Character.toString(ch)).matches()){
			numberS += Character.toString(ch);
			nextCh();
		}
		try{
			if(numberS.contains(".")){
				t.doubleValue = Double.parseDouble(numberS);
				t.kind = doubleV;
			}else{
				t.intValue = Integer.parseInt(numberS);
				t.kind = intV;
			}

		}catch (Exception e){
			System.out.println("Overflow");
		}
	}


	private static void readName(Token t) {
		if(ch == '"'){
			nextCh();
			while(stringsPattern.matcher(Character.toString(ch)).matches() || ch == ' '){
				t.string += ch;
				nextCh();
			}
			if(ch == '"'){
				t.kind = stringV;
				nextCh();
			} else {
				System.out.println("Missing \"");
			}
			return;
		}
		while(lettersAndNumbersPattern.matcher(Character.toString(ch)).matches()){
			t.string += ch;
			nextCh();
		}
		int m = binarySearch(key,t.string);
		if(m != -1){
			t.kind = keyVal[m];
		} else {
			tempElement = Parser.STNVX.stream()
					.filter(TableElement -> t.string.equals(TableElement.name))
					.findAny()
					.orElse(null);
			//if null = ident, if not null = variable
			if (tempElement == null){
				t.kind = ident;
				if(ch == ']') {
					nextCh();
					tokenErr(t,"Invalid Array");
				}
				if (ch == '['){
					nextCh();
					t.array = true;
					//ident cannot have variable index, if so throw error
					if (lettersPattern.matcher(Character.toString(ch)).matches()){
						t.kind = invalid;
						do {
							nextCh();
						} while(lettersPattern.matcher(Character.toString(ch)).matches());
					}
					String numberS = "";
					while(numbersPattern.matcher(Character.toString(ch)).matches()){
						if (ch == '.') {
							tokenErr(t,"Array index must be an int constant in declaration");
							nextCh();
						}
						else {
							numberS += Character.toString(ch);
							nextCh();
						}
					}
					if (lettersPattern.matcher(Character.toString(ch)).matches()){
						t.kind = invalid;
						numberS = "";
						do {
							nextCh();
						} while(lettersPattern.matcher(Character.toString(ch)).matches());
					}
					if (numberS.equals("") || t.kind == invalid){
						tokenErr(t,"Array index must be an int constant in declaration");
					} else {
						t.length = Integer.parseInt(numberS);
					}
					if(ch != ']') tokenErr(t,"Invalid Array");
					else nextCh();
				}
				// token = variable
			} else {
				// int var as index?
				TableElement indexVariable;
				t.kind = variable;
				if(ch == ']') {
					nextCh();
					tokenErr(t,"Invalid Array");
				}
				if (ch == '[') {
					nextCh();
					t.array = true;
					String numberS = "";
					// index is an int constant
					if (numbersPattern.matcher(Character.toString(ch)).matches()){
						do {
							if (ch == '.') {
								tokenErr(t,"Invalid Array");
								nextCh();
							}
							else {
								numberS += Character.toString(ch);
								nextCh();
							}
						} while(numbersPattern.matcher(Character.toString(ch)).matches());
						t.index = Integer.parseInt(numberS);
						// if index was a variable
					} else {
						while (lettersAndNumbersPattern.matcher(Character.toString(ch)).matches()){
							numberS += Character.toString(ch);
							nextCh();
						}

						//Needs to be final in order to be use with lambda
						String finalNumberS = numberS;

						//search variable index in variable table
						indexVariable = Parser.STNVX.stream()
								.filter(TableElement -> finalNumberS.equals(TableElement.name))
								.findAny()
								.orElse(null);
						if (indexVariable == null){
							tokenErr(t,"Variable in index not defined");
						} else if (indexVariable.type != intV) {
							tokenErr(t,"Can only use int type variables or constants as index");
						}else {
							t.varIndex = true;
							t.index = indexVariable.dir;
						}
					}
					if(ch != ']') tokenErr(t,"Invalid Array");
					else nextCh();
				} else if (tempElement.type == intAV || tempElement.type == doubleAV ||tempElement.type == stringAV) {
					tokenErr(t,"Variable index not specified");
				}
			}
		}
	}

	private static void tokenErr (Token t,String message){
		if (Parser.errDist >= 3){
			System.err.println("Line " + t.line + ", pos " + t.col + ": " + message);
			Parser.errors++;
		}
		t.kind = invalid;
		Parser.errDist = 0;
	}

	private static void readCharCon(Token t){
		t.string += ch;
		//'
		nextCh();
		//'a'
		if(ch == '\\'){
			t.string += ch;
			//'\
			nextCh();
			t.string += ch;
			//'\q
			t.kind = charCon;
			if(numbersPattern.matcher(Character.toString(ch)).matches()){
				t.charValue = ch;
			}
		}else if(lettersPattern.matcher(Character.toString(ch)).matches()){
			t.string += ch;
			t.kind = charCon;
		}else if(numbersPattern.matcher(Character.toString(ch)).matches()){
			t.charValue = ch;
			t.string += ch;
			t.kind = charCon;
		}
		nextCh();
		if(ch == '\''){
			t.string += ch;
		} else {
			while (ch != '\'' && ch != eol) {
				nextCh();
			}
			t.kind = none;
		}
		nextCh();
	}

	static int binarySearch(String[] arr, String x) {
		int l = 0, r = arr.length - 1;
		while (l <= r) {
			int m = l + (r - l) / 2;
			int res = x.compareTo(arr[m]);
			// Check if x is present at mid
			if (res == 0)
				return m;
			// If x greater, ignore left half
			if (res > 0)
				l = m + 1;
				// If x is smaller, ignore right half
			else
				r = m - 1;
		}
		return -1;
	}
}






