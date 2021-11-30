/* MicroJava Scanner Tester
   ========================
   Place this file in a subdirectory MJ
   Compile with
     javac MJ\TestScanner.java
   Run with
     java MJ.Scanner.TestScanner <inputFileName>
*/
package CFlat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TestScanner {

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
			stringV   = 46;

	private static String[] tokenName = {
			"none",	"ident", "variable", "number",	"char",
			"+", "-", "*", "/",	"%",
			"==", "!=", "<", "<=", ">", ">=","=",
			":",";", ",", ".","$", "(",	")", "[", "]", "{",	"}",
			"START",
			"END",
			"READ",
			"PRINT",
			"PRINTNL",
			"IF",
			"ELSE",
			"FOR",
			"WHILE",
			"AND",
			"OR",
			"NOT",
			"eof",
			"INT",
			"DOUBLE",
			"STRING",
			"intV",
			"doubleV",
			"stringV"
	};

	// Main method of the scanner tester
	/*
	public static void main(String args[]) {
		Token t;
		if (args.length > 0) {
			String source = args[0];
			try {
				System.out.println("---------------------Cleaning cache program---------------------\n\n");
				File file = new File("proyecto.stn");
				File file2 = new File("proyecto.stnv");
				File file3 = new File("dass.txt");
				try{
					file.delete();
					file2.delete();
					file3.delete();
					System.out.println("---------------------Cache cleaned program---------------------\n\n");
				} catch (Exception e){
					System.out.println("---------------------Cache cleaned program---------------------\n\n");
				}
				Scanner.init(new InputStreamReader(new FileInputStream(source)));
				System.out.println("---------------------Compiling program---------------------");
                Parser.parser();
				if (!Parser.err){
					System.out.println("---------------------Program Compiled successfully---------------------\n\n");
					System.out.println("---------------------Generating Decompiler---------------------");
					DisASSembler.desassembler();
					System.out.println("---------------------Program Decompiled successfully---------------------\n\n");

					System.out.println("---------------------Running Virtual Machine---------------------");
					VMachine.runTime();
					System.out.println("\n---------------------Program run successfully ---------------------\n\n");

				} else if (Parser.errors > 0) System.out.println(Parser.errors + " errors found\nFix errors to run program");
				else System.out.println("Program stopped");
			} catch (IOException e) {
				System.out.println("-- cannot open input file " + source);
			}
		} else System.out.println("-- synopsis: java MJ.Scanner.TestScanner <inputfileName>");
	}
	*/
}