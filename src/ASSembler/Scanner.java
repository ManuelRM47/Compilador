/* MicroJava Scanner (HM 06-12-28)
   =================
*/
package ASSembler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

//Falta diferenciar String y typos de datos ?

public class Scanner {
	private static final char eofCh = '\u0080';
	private static final char eol = '\n';
	private static final int  // token codes
			none      = 0, ident     = 1, number    = 2, charCon   = 3,
			plus      = 4,	minus     = 5,	times     = 6,	slash     = 7,	rem       = 8,
			eql       = 9,	neq       = 10,	lss       = 11,	leq       = 12, gtr       = 13,	geq       = 14,
			assign    = 15,	semicolon = 16,	comma     = 17,	period    = 18,
			lpar      = 19,	rpar      = 20,	lbrack    = 21,	rbrack    = 22,	lbrace    = 23,	rbrace    = 24,
			NOP_      = 25,
			DEFI_     = 26,
			DEFD_     = 27,
			DEFS_     = 28,
			DEFAI_    = 29,
			DEFAD_    = 30,
			DEFAS_    = 31,
			ADD_      = 32,
			SUB_      = 33,
			MULT_     = 34,
			DIV_      = 35,
			MOD_ 	  = 36,
			INC_ 	  = 37,
			DEC_ 	  = 38,
			CMPEQ_ 	  = 39,
			CMPNE_ 	  = 40,
			CMPLT_ 	  = 41,
			CMPLE_ 	  = 42,
			CMPGT_ 	  = 43,
			CMPGE_ 	  = 44,
			JMP_ 	  = 45,
			JMPT_ 	  = 46,
			JMPF_ 	  = 47,
			SETIDX_   = 48,
			SETIDXK_  = 49,
			PUSHI_ 	  = 50,
			PUSHD_    = 51,
			PUSHS_ 	  = 52,
			PUSHAI_   = 53,
			PUSHAD_   = 54,
			PUSHAS_   = 55,
			PUSHKI_   = 56,
			PUSHKD_   = 57,
			PUSHKS_   = 58,
			POPI_ 	  = 59,
			POPD_ 	  = 60,
			POPS_ 	  = 61,
			POPAI_ 	  = 62,
			POPAD_ 	  = 63,
			POPAS_ 	  = 64,
			POPIDX_   = 65,
			READI_ 	  = 66,
			READD_ 	  = 67,
			READS_ 	  = 68,
			READAI_   = 69,
			READAD_   = 70,
			READAS_   = 71,
			PRTM_ 	  = 72,
			PRTI_ 	  = 73,
			PRTD_ 	  = 74,
			PRTS_ 	  = 75,
			PRTAI_ 	  = 76,
			PRTAD_ 	  = 77,
			PRTAS_ 	  = 78,
			NL_       = 79,
			HALT_ 	  = 80,
			label	  = 81,
			eof       = 82,
			INT       = 83,
			DOUBLE    = 84,
			str       = 85;

	private static final String key[] = { // sorted list of keywords
			"ADD",
			"CMPEQ",
			"CMPGE",
			"CMPGT",
			"CMPLE",
			"CMPLT",
			"CMPNE",
			"DEC",
			"DEFAD",
			"DEFAI",
			"DEFAS",
			"DEFD",
			"DEFI",
			"DEFS",
			"DIV",
			"HALT",
			"INC",
			"JMP",
			"JMPF",
			"JMPT",
			"MOD",
			"MULT",
			"NL",
			"NOP",
			"POPAD",
			"POPAI",
			"POPAS",
			"POPD",
			"POPI",
			"POPIDX",
			"POPS",
			"PRTAD",
			"PRTAI",
			"PRTAS",
			"PRTD",
			"PRTI",
			"PRTM",
			"PRTS",
			"PUSHAD",
			"PUSHAI",
			"PUSHAS",
			"PUSHD",
			"PUSHI",
			"PUSHKD",
			"PUSHKI",
			"PUSHKS",
			"PUSHS",
			"READAD",
			"READAI",
			"READAS",
			"READD",
			"READI",
			"READS",
			"SETIDX",
			"SETIDXK",
			"SUB"
	};

	private static final int keyVal[] = {
			ADD_,
			CMPEQ_,
			CMPGE_,
			CMPGT_,
			CMPLE_,
			CMPLT_,
			CMPNE_,
			DEC_,
			DEFAD_,
			DEFAI_,
			DEFAS_,
			DEFD_,
			DEFI_,
			DEFS_,
			DIV_,
			HALT_,
			INC_,
			JMP_,
			JMPF_,
			JMPT_,
			MOD_,
			MULT_,
			NL_,
			NOP_,
			POPAD_,
			POPAI_,
			POPAS_,
			POPD_,
			POPI_,
			POPIDX_,
			POPS_,
			PRTAD_,
			PRTAI_,
			PRTAS_,
			PRTD_,
			PRTI_,
			PRTM_,
			PRTS_,
			PUSHAD_,
			PUSHAI_,
			PUSHAS_,
			PUSHD_,
			PUSHI_,
			PUSHKD_,
			PUSHKI_,
			PUSHKS_,
			PUSHS_,
			READAD_,
			READAI_,
			READAS_,
			READD_,
			READI_,
			READS_,
			SETIDX_,
			SETIDXK_,
			SUB_
	};

	private static Map<String, Integer> labels = new HashMap<String, Integer>();

	private static char ch;			// lookahead character
	public  static int col;			// current column
	public  static int line;		// current line
	private static int pos;			// current position from start of source file
	private static Reader in;  	// source file reader
	private static char[] lex;	// current lexeme (token string)
	public static Pattern lettersAndNumbersPattern = Pattern.compile("[a-zA-Z0-9:]");
	public static Pattern lettersPattern = Pattern.compile("[a-zA-Z]");
	public static Pattern numbersPattern = Pattern.compile("[0-9.]");

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
				nextCh();
				if (ch == '/') {
					while (ch != eol) {
						nextCh();
					}
					t = next();
				} else if (ch == '*') {

					boolean nestedComment = true;

					while (nestedComment) {
						nextCh();
						if (ch == '*') {
							nextCh();
							if (ch == '/') {
								nestedComment = false;
								nextCh();
								t = next();
							}
						} else if (ch == eofCh) {
							nestedComment = false;
							t.kind = none;
						}
					}
				} else {
					t.kind = slash;
					nextCh();
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
			case ':':
				t.kind = label;
				labels.put(t.string, t.line);
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
				t.kind = DOUBLE;
			}else{
				t.intValue = Integer.parseInt(numberS);
				t.kind = INT;
			}

		}catch (Exception e){
			System.out.println("Overflow");
		}
	}

	private static void readName(Token t) {
		if(ch == '"'){
			nextCh();
			while(lettersAndNumbersPattern.matcher(Character.toString(ch)).matches() || ch == ' '){
				t.string += ch;
				nextCh();
			}
			if(ch == '"'){
				t.kind = str;
				nextCh();
			}else{
				System.out.println("Missing ");
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
		} else if(t.string.contains(":")) {
			t.kind = label;
		} else {
			t.kind = ident;
		}

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






