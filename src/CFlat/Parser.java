package CFlat;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    public static int tokenType;
    public static Token t;
    public static Token next;
    public static int errors = 0;
    public static int errDist = 0;
    public static int SD = 0;
    public static int SC = 0;
    public static int VS = 0;
    public static int accum = 0;
    public static String tempString, source, gen;
    public static int tempInt;
    public static TableElement tempElement = new TableElement();
    public static LabelElement tempLabel, tempRef;
    public static boolean err = false;

    public static List<TableElement> STNVX = new ArrayList<TableElement>();

    public static RandomAccessFile raf;

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
            intAV      = 47,
            doubleAV   = 48,
            stringAV   = 49,
            invalid    = 50;

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

    public static List<Integer> decFirst = Arrays.asList(INT_,DOUBLE_,STRING_,eof);
    public static List<Integer> instrFirst = Arrays.asList(READ_,PRINT_,PRINTNL_,variable,IF_,FOR_,WHILE_);
    public static List<Integer> AROPFirst = Arrays.asList(plus,minus,lpar,intV,doubleV,stringV,variable);
    public static List<Integer> factorFirst = Arrays.asList(lpar,intV,doubleV,stringV,variable);
    public static List<Integer> LogOp = Arrays.asList(eql,neq,leq,geq,lss,gtr);
    public static List<Integer> condFirst = Arrays.asList(intV,doubleV,stringV,variable);
    public static List<Integer> instrSync = Arrays.asList(READ_,PRINT_,PRINTNL_,IF_,FOR_,WHILE_,eof,END_,semicolon);


    public static void scanner() { //Checo token actual y "reviso" token siguiente
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
        } else {
            syntaxError(tokenName[expected] + " Expected");
        }
    }

    public static void syntaxError(String message) {
        err = true;
        if (errDist >= 3){
            System.err.println("Line " + next.line + ", pos " + next.col + ": " + message);
            errors++;
        }
        errDist = 0;
    }

    // cflat = "START" {DECLARATION} {INSTRUCTION} "END"
    public static void cflat() throws IOException {
        check(START_);
        while (tokenType != eof && !instrFirst.contains(tokenType) && tokenType != END_) {
            declaration();
        }
        while (tokenType != eof && tokenType != END_) {
            instruction();
        }
        check(END_);
        // HALT
        raf.write(ByteBuffer.allocate(1).put((byte) 49).array());
        SC++;
    }

    //DECLARATION = "INT"|"DOUBLE"|"STRING" ident {"," ident } ";"
    public static void declaration(){
        //error handling
        if (!decFirst.contains(tokenType)){
            syntaxError(next.string + " is invalid start of declaration");
            while (!decFirst.contains(tokenType) && !instrFirst.contains(tokenType) && tokenType != semicolon) scanner();
            errDist = 0;
            if (instrFirst.contains(tokenType)) {
                return;
            } else if (tokenType == semicolon) {
                scanner();
                return;
            }
        }
        tempInt = 0;
        switch (tokenType){
            case INT_:
                tempInt = intV;
                accum = 4;
                break;
            case DOUBLE_:
                tempInt = doubleV;
                accum = 8;
                break;
            case STRING_:
                tempInt = stringV;
                accum = 2;
                break;
            default:
                syntaxError("Invalid declaration type");
                break;
        }
        scanner();
        while (true) {
            if (tokenType == invalid){
                syntaxError("Invalid array declaration");
                scanner();
            } else check(ident);
            tempElement.name = t.string;
            tempElement.dir = SD;
            tempElement.type = tempInt;
            tempElement.elementInt = 0;
            tempElement.vs = VS;
            SD += accum;
            if (tempInt == stringV) VS++;
            if (t.array) {
                tempElement.elementInt = t.length;
                tempElement.type += 3;
                SD += (accum * (t.length - 1));
                if (tempInt == stringV) VS += t.length - 1;
            }

            // add new variable to variable table
            STNVX.add(new TableElement(tempElement));

            if (tokenType == comma) {
                scanner();
            }
            else break;
        }
        check(semicolon);
    }

    // INSTRUCTION = READ|PRINT|PRINTLN|ASSIGNMENT|IF|FOR|WHILE
    public static void instruction() throws IOException {
        //error handling
        if(!instrFirst.contains(tokenType)){
            syntaxError("Instruction " + next.string + " not recognized");
            while(!instrSync.contains(tokenType)) scanner();
            errDist = 0;
            if (tokenType == semicolon) {
                scanner();
                return;
            }
        }

        switch (tokenType){
            case READ_:
                read();
                break;
            case PRINT_:
                print();
                break;
            case PRINTNL_:
                println();
                break;
            case variable:
                assignment();
                break;
            case IF_:
                ifState();
                break;
            case FOR_:
                forLoop();
                break;
            case WHILE_:
                whileLoop();
                break;
        }
    }

    // READ = "READ" [string] variable {"," [string] variable} ";"
    public static void read() throws IOException {
        scanner();
        while (true) {
            if (tokenType == stringV) {
                scanner();
                //PRTM
                raf.write(ByteBuffer.allocate(1).put((byte) 41).array());
                SC++;
                //String
                tempInt = t.string.length();
                raf.writeUTF(t.string);
                SC+=tempInt+2;
            }
            check(variable);
            tempElement = STNVX.stream()
                    .filter(TableElement -> t.string.equals(TableElement.name))
                    .findAny()
                    .orElse(null);

            if (tempElement != null){
                // Array idx
                if (t.array){
                    // if index is a variable
                    if (t.varIndex){
                        //PUSHI int variable value
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        raf.writeShort(t.index);
                        SC += 3;
                        // index is constant
                    } else {
                        //PUSHKI token index
                        raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                        raf.writeInt(t.index);
                        SC += 5;
                    }
                    //POPIDX to index register in VM
                    raf.write(ByteBuffer.allocate(1).put((byte) 34).array());
                    SC ++;
                }

                switch (tempElement.type) {
                    case intV:
                        //READI
                        raf.write(ByteBuffer.allocate(1).put((byte) 35).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC+=2;
                        break;
                    case doubleV:
                        //READD
                        raf.write(ByteBuffer.allocate(1).put((byte) 36).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC+=2;
                        break;
                    case stringV:
                        //READS
                        raf.write(ByteBuffer.allocate(1).put((byte) 37).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC+=2;
                        break;
                    case intAV:
                        //READAI
                        raf.write(ByteBuffer.allocate(1).put((byte) 38).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC+=2;
                        break;
                    case doubleAV:
                        //READAD
                        raf.write(ByteBuffer.allocate(1).put((byte) 39).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC+=2;
                        break;
                    case stringAV:
                        //READAS
                        raf.write(ByteBuffer.allocate(1).put((byte) 40).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC+=2;
                        break;
                }
            } else {
                syntaxError(t.string + " is not a declared variable");
            }

            if (tokenType == comma) {
                scanner();
            }
            else break;
        }
        check(semicolon);
    }

    // PRINT = "PRINT" string|variable { "," string|variable } ";"
    public static void print() throws IOException {
        //error handling?
        scanner();
        while (true) {
            if (tokenType == stringV){
                scanner();
                //PRTM
                raf.write(ByteBuffer.allocate(1).put((byte) 41).array());
                SC++;
                //String
                tempInt = t.string.length();
                raf.writeUTF(t.string);
                SC+=tempInt+2;
            } else if (tokenType == variable){
                scanner();

                tempElement = STNVX.stream()
                        .filter(TableElement -> t.string.equals(TableElement.name))
                        .findAny()
                        .orElse(null);

                if (tempElement != null){
                    // Array idx
                    if (t.array){
                        if (t.varIndex){
                            //PUSHI int variable value
                            raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                            raf.writeShort(t.index);
                            SC += 3;
                        } else {
                            //PUSHKI token index
                            raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                            raf.writeInt(t.index);
                            SC += 5;
                        }
                        //POPIDX to index register in VM
                        raf.write(ByteBuffer.allocate(1).put((byte) 34).array());
                        SC ++;
                    }

                    switch (tempElement.type) {
                        case intV:
                            //PRTI
                            raf.write(ByteBuffer.allocate(1).put((byte) 42).array());
                            SC++;
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                            break;
                        case doubleV:
                            //PRTD
                            raf.write(ByteBuffer.allocate(1).put((byte) 43).array());
                            SC++;
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                            break;
                        case stringV:
                            //PRTS
                            raf.write(ByteBuffer.allocate(1).put((byte) 44).array());
                            SC++;
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                            break;
                        case intAV:
                            //PRTAI
                            raf.write(ByteBuffer.allocate(1).put((byte) 45).array());
                            SC++;
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                            break;
                        case doubleAV:
                            //PRTAD
                            raf.write(ByteBuffer.allocate(1).put((byte) 46).array());
                            SC++;
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                            break;
                        case stringAV:
                            //PRTAS
                            raf.write(ByteBuffer.allocate(1).put((byte) 47).array());
                            SC++;
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                            break;
                    }
                }else {
                    syntaxError(t.string + " is not a declared variable");
                }
            } else syntaxError(t.string + " has invalid value to print");

            if (tokenType == comma){
                scanner();
            } else break;
        }
        check(semicolon);
    }

    // PRINTLN = "PRINTLN" string|variable { "," string|variable } ";"
    public static void println() throws IOException {
        scanner();
        while (true) {
            if (tokenType == stringV){
                scanner();
                //PRTM
                raf.write(ByteBuffer.allocate(1).put((byte) 41).array());
                SC++;
                //String
                tempInt = t.string.length();
                raf.writeUTF(t.string);
                SC+=tempInt+2;
            } else if (tokenType == variable){
                scanner();
                tempElement = STNVX.stream()
                        .filter(TableElement -> t.string.equals(TableElement.name))
                        .findAny()
                        .orElse(null);

                if (tempElement != null){

                    // Array idx
                    if (t.array){
                        if (t.varIndex){
                            //PUSHI int variable value
                            raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                            raf.writeShort(t.index);
                            SC += 3;
                        } else {
                            //PUSHKI token index
                            raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                            raf.writeInt(t.index);
                            SC += 5;
                        }
                        //POPIDX to index register in VM
                        raf.write(ByteBuffer.allocate(1).put((byte) 34).array());
                        SC ++;
                    }

                    switch (tempElement.type) {
                        case intV:
                            //PRTI
                            raf.write(ByteBuffer.allocate(1).put((byte) 42).array());
                            SC++;
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                            break;
                        case doubleV:
                            //PRTD
                            raf.write(ByteBuffer.allocate(1).put((byte) 43).array());
                            SC++;
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                            break;
                        case stringV:
                            //PRTS
                            raf.write(ByteBuffer.allocate(1).put((byte) 44).array());
                            SC++;
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                            break;
                        case intAV:
                            //PRTAI
                            raf.write(ByteBuffer.allocate(1).put((byte) 45).array());
                            SC++;
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                            break;
                        case doubleAV:
                            //PRTAD
                            raf.write(ByteBuffer.allocate(1).put((byte) 46).array());
                            SC++;
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                            break;
                        case stringAV:
                            //PRTAS
                            raf.write(ByteBuffer.allocate(1).put((byte) 47).array());
                            SC++;
                            raf.writeShort(tempElement.dir);
                            SC+=2;
                            break;
                    }
                } else {
                    syntaxError(t.string + " is not a declared variable");
                }
            } else syntaxError(t.string + " has invalid value to print");

            //NL
            raf.write(ByteBuffer.allocate(1).put((byte) 48).array());
            SC++;

            if (tokenType == comma){
                scanner();
            } else break;
        }
        check(semicolon);
    }

    // ASSIGNMENT = variable "=" AROP ";"
    public static void assignment() throws IOException {
        scanner();
        Token assignedToken = new Token(t);
        check(assign);
        // AROP leaves the result of all operations in the top of the stack
        arop();
        check(semicolon);

        // assign value in stack to assignedToken
        tempElement = STNVX.stream()
                .filter(TableElement -> assignedToken.string.equals(TableElement.name))
                .findAny()
                .orElse(null);

        if (tempElement != null){

            // Array idx
            if (assignedToken.array){
                if (assignedToken.varIndex){
                    //PUSHI int variable value
                    raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                    raf.writeShort(assignedToken.index);
                    SC += 3;
                } else {
                    //PUSHKI token index
                    raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                    raf.writeInt(assignedToken.index);
                    SC += 5;
                }
                //POPIDX to index register in VM
                raf.write(ByteBuffer.allocate(1).put((byte) 34).array());
                SC ++;
            }

            switch (tempElement.type) {
                case intV:
                    //POPI
                    raf.write(ByteBuffer.allocate(1).put((byte) 28).array());
                    SC++;
                    raf.writeShort(tempElement.dir);
                    SC+=2;
                    break;
                case doubleV:
                    //POPD
                    raf.write(ByteBuffer.allocate(1).put((byte) 29).array());
                    SC++;
                    raf.writeShort(tempElement.dir);
                    SC+=2;
                    break;
                case stringV:
                    //POPS
                    raf.write(ByteBuffer.allocate(1).put((byte) 30).array());
                    SC++;
                    raf.writeShort(tempElement.dir);
                    SC+=2;
                    break;
                case intAV:
                    //POPAI
                    raf.write(ByteBuffer.allocate(1).put((byte) 31).array());
                    SC++;
                    raf.writeShort(tempElement.dir);
                    SC+=2;
                    break;
                case doubleAV:
                    //POPAD
                    raf.write(ByteBuffer.allocate(1).put((byte) 32).array());
                    SC++;
                    raf.writeShort(tempElement.dir);
                    SC+=2;
                    break;
                case stringAV:
                    //POPAS
                    raf.write(ByteBuffer.allocate(1).put((byte) 33).array());
                    SC++;
                    raf.writeShort(tempElement.dir);
                    SC+=2;
                    break;
            }
        } else {
            syntaxError(assignedToken.string + " is not a declared variable");
        }
    }

    // AROP = ["+" | "-"] TERM { ("+"|"-") TERM }
    public static void arop() throws IOException {
        //error handling
        int sign = 1;
        int sign2;
        if (!AROPFirst.contains(tokenType)) {
            syntaxError(next.string + " cannot be used in arithmetic operation");
            do scanner(); while (tokenType != eof && tokenType != semicolon);
            errDist = 0;
            if (tokenType == semicolon) {
                scanner();
                return;
            }
        }

        if (tokenType == plus || tokenType == minus) {
            if (tokenType == minus) {
                sign = -1;
            }
                scanner();
        }

        //term puts a term to sum or subtract on stack
        term();

        if (sign == -1){
            //PUSHKI
            raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
            SC++;
            raf.writeInt(-1);
            SC+=4;
            //MULT
            raf.write(ByteBuffer.allocate(1).put((byte) 3).array());
            SC++;
        }

        while (tokenType == plus || tokenType == minus){
            scanner();
            sign2 = t.kind;
            //term puts a term to sum or subtract on stack
            term();
            if (sign2 == plus) {
                raf.write(ByteBuffer.allocate(1).put((byte) 1).array());
                SC++;
            } else {
                raf.write(ByteBuffer.allocate(1).put((byte) 2).array());
                SC++;
            }
        }
    }

    // TERM = FACTOR {("*"|"/") FACTOR}
    public static void term() throws IOException {
        int sign;
        //factor puts a factor to multiply or divide on stack
        factor();
        while  (tokenType == times || tokenType == slash || tokenType == rem) {
            scanner();
            sign = t.kind;

            //factor puts a factor to multiply or divide on stack
            factor();
            if (sign == times) {
                raf.write(ByteBuffer.allocate(1).put((byte) 3).array());
                SC++;
            } else if (sign == slash){
                raf.write(ByteBuffer.allocate(1).put((byte) 4).array());
                SC++;
            }
            // if modulus
            else {
                raf.write(ByteBuffer.allocate(1).put((byte) 5).array());
                SC++;
            }
        }
    }

    // FACTOR = "(" AROP ")" | num | variable |string
    public static void factor() throws IOException {
        //error handling
        if (!factorFirst.contains(tokenType)){
            syntaxError(next.string + " cannot be used arithmetic operation");
            do scanner(); while (tokenType != eof && tokenType != semicolon);
            errDist = 0;
            if (tokenType == semicolon) {
                scanner();
                return;
            }
        }

        if (tokenType == lpar){
            scanner();
            arop();
            check(rpar);
        } else if (tokenType == intV || tokenType == doubleV || tokenType == stringV) {
            scanner();
            switch (t.kind){
                case intV:
                    //PUSHKI
                    raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                    SC++;
                    raf.writeInt(t.intValue);
                    SC+=4;
                    break;
                case doubleV:
                    //PUSHKD
                    raf.write(ByteBuffer.allocate(1).put((byte) 26).array());
                    SC++;
                    raf.writeDouble(t.doubleValue);
                    SC+=8;
                    break;
                case stringV:
                    //PUSHKS
                    raf.write(ByteBuffer.allocate(1).put((byte) 27).array());
                    SC++;
                    tempInt = t.string.length();
                    raf.writeUTF(t.string);
                    SC+=tempInt+2;
                    break;
            }
        } else if (tokenType == variable) {
            scanner();
            tempElement = STNVX.stream()
                    .filter(TableElement -> t.string.equals(TableElement.name))
                    .findAny()
                    .orElse(null);

            if (tempElement != null){

                // Array idx
                if (t.array){
                    if (t.varIndex){
                        //PUSHI int variable value
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        raf.writeShort(t.index);
                        SC += 3;
                    } else {
                        //PUSHKI token index
                        raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                        raf.writeInt(t.index);
                        SC += 5;
                    }
                    //POPIDX to index register in VM
                    raf.write(ByteBuffer.allocate(1).put((byte) 34).array());
                    SC ++;
                }

                switch (tempElement.type) {
                    case intV:
                        //PUSHI
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC+=2;
                        break;
                    case doubleV:
                        //PUSHD
                        raf.write(ByteBuffer.allocate(1).put((byte) 20).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC+=2;
                        break;
                    case stringV:
                        //PUSHS
                        raf.write(ByteBuffer.allocate(1).put((byte) 21).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC+=2;
                        break;
                    case intAV:
                        //PUSHAI
                        raf.write(ByteBuffer.allocate(1).put((byte) 22).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC+=2;
                        break;
                    case doubleAV:
                        //PUSHAD
                        raf.write(ByteBuffer.allocate(1).put((byte) 23).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC+=2;
                        break;
                    case stringAV:
                        //PUSHAS
                        raf.write(ByteBuffer.allocate(1).put((byte) 24).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC+=2;
                        break;
                }
            } else {
                syntaxError(t.string + " is not a declared variable");
            }
        } else syntaxError(t.string + " has invalid value for arithmetic operation");
    }

    //IF = "IF" CONDITION "{" {INSTRUCTION} "}" ["ELSE" "{" {INSTRUCTION} "}"]
    public static void ifState() throws IOException {
        //error handling?
        scanner();
        int startIF;
        int endIF;
        int startElse;
        int endElse;

        //Puts a true or false value at the top of the stack
        condition();

        //JMPF in case condition does not hold
        raf.write(ByteBuffer.allocate(1).put((byte) 16).array());
        SC++;
        startIF = SC;
        raf.writeShort(0);
        SC+=2;

        //Instructions ran in if
        check(lbrace);
        while (tokenType != eof && tokenType != END_ && tokenType != rbrace) {
            instruction();
        }
        check(rbrace);

        if (tokenType == ELSE_){
            scanner();

            //if the IF clause runs, immediately jumps all the else code in case there is
            raf.write(ByteBuffer.allocate(1).put((byte) 14).array());
            SC++;
            endIF = SC;
            raf.writeShort(0);
            SC+=2;

            //Instructions ran in else
            startElse = SC;
            check(lbrace);
            while (tokenType != eof && tokenType != END_ && tokenType != rbrace) {
                instruction();
            }
            check(rbrace);
            endElse = SC;

            //Filling Jumps
            raf.seek(startIF + 12);
            raf.writeShort(startElse);
            raf.seek(endIF + 12);
            raf.writeShort(endElse);
            raf.seek(endElse + 12);
        } else {
            //Filling Jumps
            endIF = SC;
            raf.seek(startIF + 12);
            raf.writeShort(endIF);
            raf.seek(endIF + 12);
        }
    }

    // CONDITION = ( (num | string | variable ) ("="|"!="|"<="|">="|"<"|">") (num | string | variable)
    //             ) [("AND"|"OR") CONDITION2]
    public static void condition() throws IOException {
        //error handling
        int logOP = 0;
        int boolOP;
        if (!condFirst.contains(tokenType)){
            syntaxError(next.string + " is invalid value for condition");
            do scanner(); while (tokenType != eof && tokenType != lbrace && tokenType != rpar);
            errDist = 0;
        }
        if (tokenType == intV || tokenType == doubleV || tokenType == stringV){
            scanner();
            switch (t.kind){
                case intV:
                    //PUSHKI
                    raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                    SC++;
                    raf.writeInt(t.intValue);
                    SC+=4;
                    break;
                case doubleV:
                    //PUSHKD
                    raf.write(ByteBuffer.allocate(1).put((byte) 26).array());
                    SC++;
                    raf.writeDouble(t.doubleValue);
                    SC+=8;
                    break;
                case stringV:
                    //PUSHKS
                    raf.write(ByteBuffer.allocate(1).put((byte) 27).array());
                    SC++;
                    tempInt = t.string.length();
                    raf.writeUTF(t.string);
                    SC+=tempInt+2;
                    break;
            }
        } else if (tokenType == variable){
            scanner();
            tempElement = STNVX.stream()
                    .filter(TableElement -> t.string.equals(TableElement.name))
                    .findAny()
                    .orElse(null);

            if (tempElement != null){

                // Array idx
                if (t.array){
                    if (t.varIndex){
                        //PUSHI int variable value
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        raf.writeShort(t.index);
                        SC += 3;
                    } else {
                        //PUSHKI token index
                        raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                        raf.writeInt(t.index);
                        SC += 5;
                    }
                    //POPIDX to index register in VM
                    raf.write(ByteBuffer.allocate(1).put((byte) 34).array());
                    SC ++;
                }

                switch (tempElement.type) {
                    case intV:
                        //PUSHI
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case doubleV:
                        //PUSHD
                        raf.write(ByteBuffer.allocate(1).put((byte) 20).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case stringV:
                        //PUSHS
                        raf.write(ByteBuffer.allocate(1).put((byte) 21).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case intAV:
                        //PUSHAI
                        raf.write(ByteBuffer.allocate(1).put((byte) 22).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case doubleAV:
                        //PUSHAD
                        raf.write(ByteBuffer.allocate(1).put((byte) 23).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case stringAV:
                        //PUSHAS
                        raf.write(ByteBuffer.allocate(1).put((byte) 24).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                }
            } else {
                syntaxError(t.string + " is not a declared variable");
            }
        } else syntaxError(t.string + " is invalid value for conditional evaluation");

        //Checking operator kind and saving it
        if (LogOp.contains(tokenType)){
            scanner();
            logOP = t.kind;
        } else syntaxError(next.string + " is invalid logic operator");

        if (tokenType == intV || tokenType == doubleV || tokenType == stringV){
            scanner();
            switch (t.kind){
                case intV:
                    //PUSHKI
                    raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                    SC++;
                    raf.writeInt(t.intValue);
                    SC+=4;
                    break;
                case doubleV:
                    //PUSHKD
                    raf.write(ByteBuffer.allocate(1).put((byte) 26).array());
                    SC++;
                    raf.writeDouble(t.doubleValue);
                    SC+=8;
                    break;
                case stringV:
                    //PUSHKS
                    raf.write(ByteBuffer.allocate(1).put((byte) 27).array());
                    SC++;
                    tempInt = t.string.length();
                    raf.writeUTF(t.string);
                    SC+=tempInt+2;
                    break;
            }
        } else if (tokenType == variable){
            scanner();
            tempElement = STNVX.stream()
                    .filter(TableElement -> t.string.equals(TableElement.name))
                    .findAny()
                    .orElse(null);

            if (tempElement != null){
                // Array idx
                if (t.array){
                    if (t.varIndex){
                        //PUSHI int variable value
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        raf.writeShort(t.index);
                        SC += 3;
                    } else {
                        //PUSHKI token index
                        raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                        raf.writeInt(t.index);
                        SC += 5;
                    }
                    //POPIDX to index register in VM
                    raf.write(ByteBuffer.allocate(1).put((byte) 34).array());
                    SC ++;
                }

                switch (tempElement.type) {
                    case intV:
                        //PUSHI
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case doubleV:
                        //PUSHD
                        raf.write(ByteBuffer.allocate(1).put((byte) 20).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case stringV:
                        //PUSHS
                        raf.write(ByteBuffer.allocate(1).put((byte) 21).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case intAV:
                        //PUSHAI
                        raf.write(ByteBuffer.allocate(1).put((byte) 22).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case doubleAV:
                        //PUSHAD
                        raf.write(ByteBuffer.allocate(1).put((byte) 23).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case stringAV:
                        //PUSHAS
                        raf.write(ByteBuffer.allocate(1).put((byte) 24).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                }
            } else {
                syntaxError(t.string + " is not a declared variable");
            }
        } else syntaxError(t.string + " is invalid value for conditional evaluation");
        //Doing logical operation
        switch (logOP){
            case eql:
                //CMPEQ
                raf.write(ByteBuffer.allocate(1).put((byte) 8).array());
                SC++;
                break;
            case neq:
                //CMPNE
                raf.write(ByteBuffer.allocate(1).put((byte) 9).array());
                SC++;
                break;
            case lss:
                //CMPLT
                raf.write(ByteBuffer.allocate(1).put((byte) 10).array());
                SC++;
                break;
            case leq:
                //CMPLE
                raf.write(ByteBuffer.allocate(1).put((byte) 11).array());
                SC++;
                break;
            case gtr:
                //CMPGT
                raf.write(ByteBuffer.allocate(1).put((byte) 12).array());
                SC++;
                break;
            case geq:
                //CMPGE
                raf.write(ByteBuffer.allocate(1).put((byte) 13).array());
                SC++;
                break;
        }
        if (tokenType == AND_ || tokenType == OR_){
            scanner();
            boolOP = t.kind;
            condition2();
            if (boolOP == AND_){
                //MULT 0 * 0 = 0 , 0 * 1 = 0, 1 * 0 = 0, 1 * 1 = 1;
                raf.write(ByteBuffer.allocate(1).put((byte) 3).array());
                SC++;
            } else {
                //ADD 0 + 0 = 0 , 0 + 1 = 1, 1 + 0 = 1, 1 + 1 = 2; if res > 0 then true
                raf.write(ByteBuffer.allocate(1).put((byte) 1).array());
                SC++;
            }
        }
    }

    // CONDITION2 = ["NOT"] (num | string | variable) ("="|"!="|"<="|">="|"<"|">") (num | string | variable)
    public static void condition2() throws IOException{
        //error handling
        int logOP = 0;
        int boolOP;
        if (!condFirst.contains(tokenType)){
            syntaxError(next.string + " is invalid value for condition");
            do scanner(); while (tokenType != eof && tokenType != lbrace && tokenType != rpar);
            errDist = 0;
        }
        if (tokenType == intV || tokenType == doubleV || tokenType == stringV){
            scanner();
            switch (t.kind){
                case intV:
                    //PUSHKI
                    raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                    SC++;
                    raf.writeInt(t.intValue);
                    SC+=4;
                    break;
                case doubleV:
                    //PUSHKD
                    raf.write(ByteBuffer.allocate(1).put((byte) 26).array());
                    SC++;
                    raf.writeDouble(t.doubleValue);
                    SC+=8;
                    break;
                case stringV:
                    //PUSHKS
                    raf.write(ByteBuffer.allocate(1).put((byte) 27).array());
                    SC++;
                    tempInt = t.string.length();
                    raf.writeUTF(t.string);
                    SC+=tempInt+2;
                    break;
            }
        } else if (tokenType == variable){
            scanner();
            tempElement = STNVX.stream()
                    .filter(TableElement -> t.string.equals(TableElement.name))
                    .findAny()
                    .orElse(null);

            if (tempElement != null){
                // Array idx
                if (t.array){
                    if (t.varIndex){
                        //PUSHI int variable value
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        raf.writeShort(t.index);
                        SC += 3;
                    } else {
                        //PUSHKI token index
                        raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                        raf.writeInt(t.index);
                        SC += 5;
                    }
                    //POPIDX to index register in VM
                    raf.write(ByteBuffer.allocate(1).put((byte) 34).array());
                    SC ++;
                }

                switch (tempElement.type) {
                    case intV:
                        //PUSHI
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case doubleV:
                        //PUSHD
                        raf.write(ByteBuffer.allocate(1).put((byte) 20).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case stringV:
                        //PUSHS
                        raf.write(ByteBuffer.allocate(1).put((byte) 21).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case intAV:
                        //PUSHAI
                        raf.write(ByteBuffer.allocate(1).put((byte) 22).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case doubleAV:
                        //PUSHAD
                        raf.write(ByteBuffer.allocate(1).put((byte) 23).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case stringAV:
                        //PUSHAS
                        raf.write(ByteBuffer.allocate(1).put((byte) 24).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                }
            } else {
                syntaxError(t.string + " is not a declared variable");
            }

        } else syntaxError(t.string + " is invalid value for a conditional evaluation");

        //Checking operator kind and saving it
        if (LogOp.contains(tokenType)){
            scanner();
            logOP = t.kind;
        } else syntaxError(next.string + "is invalid logic operator");

        if (tokenType == intV || tokenType == doubleV || tokenType == stringV){
            scanner();
            switch (t.kind){
                case intV:
                    //PUSHKI
                    raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                    SC++;
                    raf.writeInt(t.intValue);
                    SC+=4;
                    break;
                case doubleV:
                    //PUSHKD
                    raf.write(ByteBuffer.allocate(1).put((byte) 26).array());
                    SC++;
                    raf.writeDouble(t.doubleValue);
                    SC+=8;
                    break;
                case stringV:
                    //PUSHKS
                    raf.write(ByteBuffer.allocate(1).put((byte) 27).array());
                    SC++;
                    tempInt = t.string.length();
                    raf.writeUTF(t.string);
                    SC+=tempInt+2;
                    break;
            }
        } else if (tokenType == variable){
            scanner();
            tempElement = STNVX.stream()
                    .filter(TableElement -> t.string.equals(TableElement.name))
                    .findAny()
                    .orElse(null);

            if (tempElement != null){
                // Array idx
                if (t.array){
                    if (t.varIndex){
                        //PUSHI int variable value
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        raf.writeShort(t.index);
                        SC += 3;
                    } else {
                        //PUSHKI token index
                        raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                        raf.writeInt(t.index);
                        SC += 5;
                    }
                    //POPIDX to index register in VM
                    raf.write(ByteBuffer.allocate(1).put((byte) 34).array());
                    SC ++;
                }

                switch (tempElement.type) {
                    case intV:
                        //PUSHI
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case doubleV:
                        //PUSHD
                        raf.write(ByteBuffer.allocate(1).put((byte) 20).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case stringV:
                        //PUSHS
                        raf.write(ByteBuffer.allocate(1).put((byte) 21).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case intAV:
                        //PUSHAI
                        raf.write(ByteBuffer.allocate(1).put((byte) 22).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case doubleAV:
                        //PUSHAD
                        raf.write(ByteBuffer.allocate(1).put((byte) 23).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case stringAV:
                        //PUSHAS
                        raf.write(ByteBuffer.allocate(1).put((byte) 24).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                }
            } else {
                syntaxError(t.string + " is not a declared variable");
            }
        } else syntaxError(t.string + " is invalid value for a conditional evaluation");

        //Doing logical operation
        switch (logOP){
            case eql:
                //CMPEQ
                raf.write(ByteBuffer.allocate(1).put((byte) 8).array());
                SC++;
                break;
            case neq:
                //CMPNE
                raf.write(ByteBuffer.allocate(1).put((byte) 9).array());
                SC++;
                break;
            case lss:
                //CMPLT
                raf.write(ByteBuffer.allocate(1).put((byte) 10).array());
                SC++;
                break;
            case leq:
                //CMPLE
                raf.write(ByteBuffer.allocate(1).put((byte) 11).array());
                SC++;
                break;
            case gtr:
                //CMPGT
                raf.write(ByteBuffer.allocate(1).put((byte) 12).array());
                SC++;
                break;
            case geq:
                //CMPGE
                raf.write(ByteBuffer.allocate(1).put((byte) 13).array());
                SC++;
                break;
        }
    }

    // FOR = "FOR" "(" (num | variable) ":" (num | variable) ")" "{" {INSTRUCTION} "}"
    public static void forLoop() throws IOException {
        //error handling?
        scanner();
        int forStart;
        int forEnd;
        int conditional;
        Token tempVariable = new Token();
        TableElement countVariable = new TableElement();

        conditional = SC;

        check(lpar);
        if (tokenType == variable){
            scanner();
            tempElement = STNVX.stream()
                    .filter(TableElement -> t.string.equals(TableElement.name))
                    .findAny()
                    .orElse(null);

            if (tempElement != null){
                tempVariable = new Token(t);
                countVariable = new TableElement(tempElement);

                // Array idx
                if (t.array){
                    if (t.varIndex){
                        //PUSHI int variable value
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        raf.writeShort(t.index);
                        SC += 3;
                    } else {
                        //PUSHKI token index
                        raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                        raf.writeInt(t.index);
                        SC += 5;
                    }
                    //POPIDX to index register in VM
                    raf.write(ByteBuffer.allocate(1).put((byte) 34).array());
                    SC ++;
                }

                switch (tempElement.type) {
                    case intV:
                        //PUSHI
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case intAV:
                        //PUSHAI
                        raf.write(ByteBuffer.allocate(1).put((byte) 22).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case doubleV:
                    case stringV:
                    case doubleAV:
                    case stringAV:
                        syntaxError("Only int types are allowed in a for loop");
                        break;
                }
            } else {
                syntaxError(t.string + " is not a declared variable");
            }
        } else syntaxError("First operand in for loop must be an int variable");

        check(colon);

        if (tokenType == intV || tokenType == doubleV || tokenType == stringV){
            scanner();
            switch (t.kind){
                case intV:
                    //PUSHKI
                    raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                    SC++;
                    raf.writeInt(t.intValue);
                    SC+=4;
                    break;
                case doubleV:
                case stringV:
                    syntaxError("Only int type are allowed in a for loop");
                    break;
            }
        } else if (tokenType == variable){
            scanner();
            tempElement = STNVX.stream()
                    .filter(TableElement -> t.string.equals(TableElement.name))
                    .findAny()
                    .orElse(null);

            if (tempElement != null){
                // Array idx
                if (t.array){
                    if (t.varIndex){
                        //PUSHI int variable value
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        raf.writeShort(t.index);
                        SC += 3;
                    } else {
                        //PUSHKI token index
                        raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                        raf.writeInt(t.index);
                        SC += 5;
                    }
                    //POPIDX to index register in VM
                    raf.write(ByteBuffer.allocate(1).put((byte) 34).array());
                    SC ++;
                }

                switch (tempElement.type) {
                    case intV:
                        //PUSHI
                        raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case intAV:
                        //PUSHAI
                        raf.write(ByteBuffer.allocate(1).put((byte) 22).array());
                        SC++;
                        raf.writeShort(tempElement.dir);
                        SC += 2;
                        break;
                    case doubleV:
                    case stringV:
                    case doubleAV:
                    case stringAV:
                        syntaxError("Only int type are allowed in a for loop");
                        break;
                }
            } else {
                syntaxError(t.string + " is not a declared variable");
            }
        } else syntaxError(t.string + " is invalid value for a conditional evaluation");
        check(rpar);

        //Compare first is lesser than the second
        //CMPLT
        raf.write(ByteBuffer.allocate(1).put((byte) 10).array());
        SC++;

        //JMPF in case condition does not hold
        raf.write(ByteBuffer.allocate(1).put((byte) 16).array());
        SC++;
        forStart = SC;
        raf.writeShort(0);
        SC+=2;

        // for instructions
        check(lbrace);
        while (tokenType != eof && tokenType != END_ && tokenType != rbrace) {
            instruction();
        }
        check(rbrace);

        //Increment variableCount value;
        // Array idx
        if (tempVariable.array){
            if (tempVariable.varIndex){
                //PUSHI int variable value
                raf.write(ByteBuffer.allocate(1).put((byte) 19).array());
                raf.writeShort(tempVariable.index);
                SC += 3;
            } else {
                //PUSHKI token index
                raf.write(ByteBuffer.allocate(1).put((byte) 25).array());
                raf.writeInt(tempVariable.index);
                SC += 5;
            }
            //POPIDX to index register in VM
            raf.write(ByteBuffer.allocate(1).put((byte) 34).array());
            SC ++;
        }

        //INC
        raf.write(ByteBuffer.allocate(1).put((byte) 6).array());
        raf.writeShort(countVariable.dir);
        SC += 3;

        //Force jump to for condition
        raf.write(ByteBuffer.allocate(1).put((byte) 14).array());
        raf.writeShort(conditional);
        SC += 3;

        forEnd = SC;

        //Filling Jumps
        raf.seek(forStart + 12);
        raf.writeShort(forEnd);
        raf.seek(forEnd + 12);
    }

    // WHILE = "WHILE" "(" CONDITION ")" "{" {INSTRUCTION} "}"
    public static void whileLoop() throws IOException {
        //error handling?
        scanner();
        int whileStart;
        int whileEnd;
        int conditional;

        conditional = SC;

        check(lpar);
        condition();
        check(rpar);

        //JMPF in case condition does not hold
        raf.write(ByteBuffer.allocate(1).put((byte) 16).array());
        SC++;
        whileStart = SC;
        raf.writeShort(0);
        SC+=2;

        // While instructions
        check(lbrace);
        while (tokenType != eof && tokenType != END_ && tokenType != rbrace) {
            instruction();
        }
        check(rbrace);

        //Force a jump to while condition
        raf.write(ByteBuffer.allocate(1).put((byte) 14).array());
        raf.writeShort(conditional);
        SC += 3;

        whileEnd = SC;

        //Filling Jumps
        raf.seek(whileStart + 12);
        raf.writeShort(whileEnd);
        raf.seek(whileEnd + 12);
    }

    public static void parser(){
        errors = 0;
        errDist = 3;
        try {
            raf.seek(12);
            scanner();
            // Program start
            cflat();
            // program end

            //if (tokenType != eof) syntaxError("The file ended before the compiler");

            raf.seek(0);

            //Verifying TSN constitution before running the VM
            if (errors > 0) raf.write("NOTTSN".getBytes(StandardCharsets.UTF_8));
            else raf.write("ICCTSN".getBytes(StandardCharsets.UTF_8));
            raf.writeShort(SC);
            raf.writeShort(SD);
            raf.writeShort(VS);
            raf.close();

            //STNV File
            RandomAccessFile rafv = new RandomAccessFile(gen + ".stnv","rw");
            rafv.seek(0);
            rafv.write(ByteBuffer.allocate(1).put((byte) STNVX.size()).array());

            for (TableElement variable : STNVX){
                rafv.write(ByteBuffer.allocate(30).put(variable.name.getBytes()).array());
                rafv.writeShort(variable.dir);
                rafv.writeByte(variable.type);
                rafv.writeShort(variable.elementInt);
                rafv.writeShort(variable.vs);
            }
            rafv.close();
        } catch (FileNotFoundException E){
            System.out.println("-- cannot find input file");
        } catch (IOException IO){
            System.out.println("-- cannot open input file");
        }
    }

    public static void main(String[] args) {
        Token t;
        if (args.length > 0) {
            source = args[0];
            System.out.println("");
            System.out.println("Input Path: " + args[0]);
            File f = new File(source);
            gen = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf("\\")+1);
            System.out.println("Input file: " + gen);
            gen = gen.split("\\.")[0];
            try {
                System.out.println("---------------------Cleaning cache---------------------\n");
                File file = new File(gen + ".stn");
                File file2 = new File(gen + ".stnv");
                if (file.delete()) System.out.println("Old file stn deleted");;
                if (file2.delete()) System.out.println("Old file stnv deleted");

                System.out.println("---------------------Cache cleaned---------------------\n");
                Scanner.init(new InputStreamReader(new FileInputStream(source)));
                System.out.println("---------------------Compiling program---------------------\n");
                try {
                    raf = new RandomAccessFile(new File(gen + ".stn"), "rw");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                parser();
                if (!err){
                    System.out.println("---------------------Program Compiled successfully---------------------");
                    System.out.println(file.getName() + " created");
                    System.out.println(file2.getName() + " created\n\n");
                } else if (errors > 0) System.out.println(errors + " errors found\nFix errors to run program");
                else System.out.println("Program stopped");
            } catch (IOException e) {
                System.out.println("-- Cannot open input file " + source);
            }
        } else System.out.println("-- Syntax: Compiler <inputfilePath>");
    }
}
