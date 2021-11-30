package ASSembler;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ASSembler {
    public static int tokenType;
    public static Token t;
    public static Token next;
    public static int errors = 0;
    public static int errDist = 0;
    public static int SD = 0;
    public static int SC = 0;
    public static int VS = 0;
    public static String tempString, source, gen;
    public static int tempInt;
    public static TableElement tempElement;
    public static LabelElement tempLabel, tempRef;
    public static boolean err = false;

    public static List<TableElement> variableTable = new  ArrayList<TableElement>();
    public static List<LabelElement> labelTable = new  ArrayList<LabelElement>();
    public static List<LabelElement> referenceTable = new  ArrayList<LabelElement>();

    public static RandomAccessFile raf;

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

    private static String[] tokenName = {
            "none",	"ident  ",	"number ",	"char   ",
            "+", "-", "*", "/",	"%",
            "==", "!=", "<", "<=", ">", ">=","=",
            ";", ",", ".", "(",	")", "[", "]", "{",	"}",
            "NOP", "DEFI", "DEFD", "DEFS", "DEFAI",
            "DEFAD", "DEFAS", "ADD", "SUB", "MULT",
            "DIV", "MOD", "INC", "DEC", "CMPEQ",
            "CMPNE", "CMPLT", "CMPLE", "CMPGT", "CMPGE",
            "JMP", "JMPT", "JMPF", "SETIDX", "SETIDXK",
            "PUSHI", "PUSHD", "PUSHS", "PUSHAI", "PUSHAD",
            "PUSHAS", "PUSHKI", "PUSHKD", "PUSHKS", "POPI",
            "POPD", "POPS", "POPAI", "POPAD", "POPAS",
            "POPIDX", "READI", "READD", "READS", "READAI",
            "READAD", "READAS", "PRTM", "PRTI", "PRTD",
            "PRTS", "PRTAI", "PRTAD", "PRTAS", "NL", "HALT", "label",
            "eof", "int", "double", "str"
    };

    public static void scanner() { //Checo token actual y "adivino" token siguiente
        t = next;
        next = Scanner.next();
        tokenType = next.kind;
        errDist++;
    }

    public static void check(int expected)
    {
        if (tokenType == expected)
        {
            scanner();
        }else{
            syntaxError(tokenName[expected] + " Expected");
        }
    }

    public static void syntaxError(String message) {
        err = true;
        if (errDist >= 3){
            System.err.println("Line " + next.line + ", col " + next.col + ": " + message);
            errors++;
        }
        errDist = 0;
    }

    private static void program(){
        try{
            raf.seek(12);
            while(tokenType != eof && tokenType != HALT_)
            {
                //System.out.println(tokenName[tokenType]);
                switch (tokenName[tokenType]){
                    case "NOP":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 0).array());
                        SC++;
                        break;
                    case "DEFI":
                        scanner();
                        check(ident);
                        variableTable.add(new TableElement(t.string, SD, DEFI_,0,0));
                        SD+=4;
                        //
                        //Se escribe el valor el el file ????
                        //
                        break;
                    case "DEFD":
                        scanner();
                        check(ident);
                        variableTable.add(new TableElement(t.string, SD, DEFD_,0,0));
                        SD+=8;
                        break;
                    case "DEFS":
                        scanner();
                        check(ident);
                        variableTable.add(new TableElement(t.string, SD, DEFS_,0,VS));
                        SD+=2;
                        VS++;
                        break;
                    case "DEFAI":
                        scanner();
                        check(ident);
                        tempString = t.string;
                        check(comma);
                        check(INT);
                        variableTable.add(new TableElement(tempString, SD, DEFAI_,t.intValue,0));
                        SD+=t.intValue*4;
                        break;
                    case "DEFAD":
                        scanner();
                        check(ident);
                        tempString = t.string;
                        check(comma);
                        check(INT);
                        variableTable.add(new TableElement(tempString, SD, DEFAD_,t.intValue,0));
                        SD+=t.intValue*8;
                        break;
                    case "DEFAS":
                        scanner();
                        check(ident);
                        tempString = t.string;
                        check(comma);
                        check(INT);
                        variableTable.add(new TableElement(tempString, SD, DEFAS_,t.intValue,VS));
                        SD+=t.intValue *2;
                        VS+=t.intValue;
                        break;
                    case "ADD":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 1).array());
                        SC++;
                        break;
                    case "SUB":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 2).array());
                        SC++;
                        break;
                    case "MULT":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 3).array());
                        SC++;
                        break;
                    case "DIV":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 4).array());
                        SC++;
                        break;
                    case "MOD":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 5).array());
                        SC++;
                        break;
                    case "INC":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 6).array());
                        SC++;
                        check(ident); //i
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "DEC":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 7).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "CMPEQ":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 8).array());
                        SC++;
                        break;
                    case "CMPNE":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 9).array());
                        SC++;
                        break;
                    case "CMPLT":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 10).array());
                        SC++;
                        break;
                    case "CMPLE":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 11).array());
                        SC++;
                        break;
                    case "CMPGT":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 12).array());
                        SC++;
                        break;
                    case "CMPGE":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 13).array());
                        SC++;
                        break;
                    case "JMP":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 14).array());
                        SC++;
                        check(ident);
                        try {
                            tempLabel = labelTable.stream()
                                    .filter(LabelElement -> t.string.equals(LabelElement.name))
                                    .findAny()
                                    .orElse(null);
                            if (tempLabel != null){
                                raf.writeShort(tempLabel.dir);
                            } else {
                                syntaxError(t.string + " does not have a flag declared");
                            }
                        } catch (NullPointerException e) {
                            referenceTable.add(new LabelElement(t.string,SC));
                            raf.write(ByteBuffer.allocate(2).put((byte) 0).array());
                        }
                        SC+=2;
                        break;
                    case "JMPT":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 15).array());
                        SC++;
                        check(ident);
                        try {
                            tempLabel = labelTable.stream()
                                    .filter(LabelElement -> (t.string + ":").equals(LabelElement.name))
                                    .findAny()
                                    .orElse(null);
                            if (tempLabel != null){
                                raf.writeShort(tempLabel.dir);
                            } else {
                                syntaxError(t.string + " does not have a flag declared");
                            }
                        } catch (NullPointerException e) {
                            referenceTable.add(new LabelElement(t.string,SC));
                            raf.write(ByteBuffer.allocate(2).put((byte) 0).array());
                        }
                        SC+=2;
                        break;
                    case "JMPF":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 16).array());
                        SC++;
                        check(ident);
                        try {
                            tempLabel = labelTable.stream()
                                    .filter(LabelElement -> t.string.equals(LabelElement.name))
                                    .findAny()
                                    .orElse(null);
                            if (tempLabel != null){
                                raf.writeShort(tempLabel.dir);
                            } else {
                                syntaxError(t.string + " does not have a flag declared");
                            }
                        } catch (NullPointerException e) {
                            referenceTable.add(new LabelElement(t.string,SC));
                            raf.write(ByteBuffer.allocate(2).put((byte) 0).array());
                        }
                        SC+=2;
                        break;
                    case "SETIDX":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 17).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "SETIDXK":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 18).array());
                        SC++;
                        check(INT);
                        //Query search table element by name
                        //Falta Error Handling ?
                        raf.writeInt(t.intValue);
                        SC+=4;
                        break;
                    case "PUSHI":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "PUSHD":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 20).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "PUSHS":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 21).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "PUSHAI":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 22).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "PUSHAD":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 23).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "PUSHAS":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 24).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "PUSHKI":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                        SC++;
                        check(INT);
                        raf.writeInt(t.intValue);
                        SC+=4;
                        break;
                    case "PUSHKD":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 26).array());
                        SC++;
                        //Falta definir tipos de números ?
                        check(DOUBLE);
                        raf.writeDouble(t.doubleValue);
                        SC+=8;
                        break;
                    case "PUSHKS":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 27).array());
                        SC++;
                        //Falta definir strings en el scanner ?
                        check(str);
                        tempInt = t.string.length();
                        raf.writeUTF(t.string);
                        SC+=tempInt+2;
                        //VS++; ?
                        break;
                    case "POPI":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 28).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "POPD":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 29).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "POPS":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 30).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "POPAI":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 31).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "POPAD":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 32).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "POPAS":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 33).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "POPIDX":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 34).array());
                        SC++;
                        break;
                    case "READI":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 35).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "READD":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 36).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "READS":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 37).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "READAI":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 38).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "READAD":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 39).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "READAS":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 40).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "PRTM":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 41).array());
                        SC++;
                        //Falta definir strings en el scanner ?
                        check(str);
                        tempInt = t.string.length();
                        raf.writeUTF(t.string);
                        SC+=tempInt+2;
                        //VS++; ?
                        break;
                    case "PRTI":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 42).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "PRTD":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 43).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "PRTS":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 44).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "PRTAI":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 45).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "PRTAD":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 46).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);

                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "PRTAS":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 47).array());
                        SC++;
                        check(ident);
                        //Query search table element by name
                        tempElement = variableTable.stream()
                                .filter(TableElement -> t.string.equals(TableElement.name))
                                .findAny()
                                .orElse(null);
                        if (tempElement != null){
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                        } else {
                            syntaxError(t.string + " is not a declared variable");
                        }
                        break;
                    case "NL":
                        scanner();
                        raf.write(ByteBuffer.allocate(1).put((byte) 48).array());
                        SC++;
                        break;
                        //Ni idea con los labels ? Tabla de Label
                    case "label":
                        scanner();
                        try {
                            tempLabel = new LabelElement(t.string,SC);
                            tempRef = labelTable.stream()
                                    .filter(LabelElement -> tempLabel.name.equals(LabelElement.name))
                                    .findAny()
                                    .orElse(null);

                            if (tempRef == null){
                                labelTable.add(tempLabel);
                            } else {
                                throw new Exception("Flag "+ tempLabel.name +" already exists.");
                            }
                        } catch (Exception e){
                            syntaxError(e.getMessage());
                        }
                        break;
                    default:
                        syntaxError("Line " + next.line + ", col " + next.col + ": " +"keyword "+ next.string +" not recognized in language");
                        scanner();
                        break;
                }
            }

            if (tokenType == HALT_)
            {
                //System.out.println(tokenName[tokenType]);
                scanner();
                raf.write(ByteBuffer.allocate(1).put((byte) 49).array());
                SC++;
            } else {
                System.err.println("HALT must be the last instruction in a file");
            }

            raf.seek(0);

            //Verifying STN constitution before running the VM
            if (errors > 0) raf.write("NOTTSN".getBytes(StandardCharsets.UTF_8));
            else raf.write("ICCTSN".getBytes(StandardCharsets.UTF_8));
            raf.writeShort(SC);
            raf.writeShort(SD);
            raf.writeShort(VS);
            for (LabelElement label : referenceTable){
                try{
                    tempLabel = labelTable.stream()
                            .filter(LabelElement -> (label.name+":").equals(LabelElement.name))
                            .findAny()
                            .orElse(null);
                    if (tempElement != null){

                    } else {
                        syntaxError(t.string + " is not a declared variable");
                    }
                    raf.seek(label.dir + 12);
                    raf.writeShort(tempLabel.dir);
                } catch (Exception e){
                    err = true;
                    syntaxError("Label referenced but not defined");
                }
            }
            raf.close();

            RandomAccessFile rafv = new RandomAccessFile(gen + ".stnv","rw");
            rafv.seek(0);
            rafv.write(ByteBuffer.allocate(1).put((byte) variableTable.size()).array());

            for (TableElement variable : variableTable){
                rafv.write(ByteBuffer.allocate(30).put(variable.name.getBytes()).array());
                rafv.writeShort(variable.dir);
                rafv.writeByte(variable.type);
                rafv.writeShort(variable.elementInt);
                rafv.writeShort(variable.vs);
            }
            rafv.close();
        } catch (IOException e){
            System.out.println("File Error");
        } catch (RuntimeException e){
            if (e.getMessage() == null) System.err.println("Program stopped");
            else System.err.println(e.getMessage());
        }
 }

    public static void assembler(){
        errors = 0;
        errDist = 4;
        try {
            scanner();
            program();
            if (tokenType != eof) syntaxError("The file ended before the assembler program");
        } catch (Exception E){
            System.out.println("-- cannot open input file");
        }
    }

    public static void main(String[] args) {
        CFlat.Token t;
        if (args.length > 0) {
            source = args[0];
            System.out.println("");
            System.out.println("Input Path: " + args[0]);
            File f = new File(source);
            gen = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf("\\")+1);
            System.out.println("Input file: " + gen);
            gen = gen.split("\\.")[0];
            try {
                System.out.println("---------------------Cleaning cache---------------------");
                File file = new File(gen + ".stn");
                File file2 = new File(gen + ".stnv");
                if (file.delete()) System.out.println("Old file stn deleted");;
                if (file2.delete()) System.out.println("Old file stnv deleted");

                System.out.println("---------------------Cache cleaned---------------------");
                Scanner.init(new InputStreamReader(new FileInputStream(source)));
                System.out.println("---------------------Assembling program---------------------");
                try {
                    raf = new RandomAccessFile(file, "rw");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                assembler();
                if (!err){
                    System.out.println("---------------------Program assembled successfully---------------------");
                    System.out.println(file.getName() + " created");
                    System.out.println(file2.getName() + " created\n\n");
                } else if (errors > 0) System.out.println(errors + " errors found\nFix errors to run program");
                else System.out.println("Program stopped");
            } catch (IOException e) {
                System.out.println("-- Cannot open input file " + source);
            }
        } else System.out.println("-- Syntax: Assembler <inputfilePath>");
    }

}
