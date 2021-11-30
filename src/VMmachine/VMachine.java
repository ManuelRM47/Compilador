package VMmachine;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class VMachine {

    //Temps and counters
    public static int pc = 0;
    public static int tsc = 0;
    public static int tsd = 0;
    public static int tvs = 0;
    public static String tempString, stn, stnv;
    public static int tempInt;
    public static double tempDouble;
    public static short tempDir;
    public static int index;


    //Variable elements
    public static String name;
    public static int dir;
    public static int type;
    public static int elemenNum;
    public static int vs;

    //Miscellaneous
    public static byte[] magicNumbers = new byte[6];
    public static byte[] NameBytes = new byte[30];
    private static Scanner sc = new Scanner(System.in);

    //Tables and vectors
    public static TableElement tempElement;

    public static List<TableElement> variableTable = new ArrayList<TableElement>();
    public static String[] vString;

    //VM Stack
    public static LinkedStack stack = new LinkedStack();

    //instructions
    public static final int
            NOP = 0,
            ADD = 1,
            SUB = 2,
            MULT = 3,
            DIV = 4,
            MOD = 5,
            INC = 6,
            DEC = 7,
            CMPEQ = 8,
            CMPNE = 9,
            CMPLT = 10,
            CMPLE = 11,
            CMPGT = 12,
            CMPGE = 13,
            JMP = 14,
            JMPT = 15,
            JMPF = 16,
            SETIDX = 17,
            SETIDXK = 18,
            PUSHI = 19,
            PUSHD = 20,
            PUSHS = 21,
            PUSHAI = 22,
            PUSHAD = 23,
            PUSHAS = 24,
            PUSHKI = 25,
            PUSHKD = 26,
            PUSHKS = 27,
            POPI = 28,
            POPD = 29,
            POPS = 30,
            POPAI = 31,
            POPAD = 32,
            POPAS = 33,
            POPIDX = 34,
            READI = 35,
            READD = 36,
            READS = 37,
            READAI = 38,
            READAD = 39,
            READAS = 40,
            PRTM = 41,
            PRTI = 42,
            PRTD = 43,
            PRTS = 44,
            PRTAI = 45,
            PRTAD = 46,
            PRTAS = 47,
            NL = 48,
            HALT = 49,
            DEFI     = 26,
            DEFD     = 27,
            DEFS     = 28,
            DEFAI    = 29,
            DEFAD    = 30,
            DEFAS    = 31,
            intV      = 44,
            doubleV   = 45,
            stringV   = 46,
            intAV      = 47,
            doubleAV   = 48,
            stringAV   = 49;

    //main
    public static void runTime(){
        try {
            program();
        } catch (Exception E){
            System.out.println(E.getMessage());
            System.exit(1);
            //System.out.println("-- cannot open input file");
        }
    }

    //Virtual Machine
    public static void program(){
        try {
            //STN File Reader
            RandomAccessFile raf = new RandomAccessFile(new File(stn), "r");
            RandomAccessFile rafv = new RandomAccessFile(new File(stnv), "r");
            raf.seek(0);
            rafv.seek(0);
            //ASE File Writer
            RandomAccessFile SD = new RandomAccessFile(new File("SD.bin"), "rw");

            raf.read(magicNumbers,0,6);
            tempString = new String(magicNumbers);

            if (!tempString.equals("ICCTSN")){
                System.out.println("File magic numbers are not valid for this Virtual Machine");
                return;
            }
            //STNV READ
            try {
                tempInt = rafv.readByte();
                while (true){
                    rafv.read(NameBytes,0,30);
                    name = new String(NameBytes);
                    dir = rafv.readShort();
                    type = rafv.readByte();
                    elemenNum = rafv.readShort();
                    vs = rafv.readShort();
                    variableTable.add(new TableElement(name,dir,type,elemenNum,vs));
                }
            } catch (EOFException e){
                rafv.close();
            }

            //SD CREATION
            raf.seek(6);
            tsc = raf.readShort();
            tsd = raf.readShort();
            tvs = raf.readShort();
            vString = new String[tvs];
            SD.seek(0);
            for (TableElement variable : variableTable){
                switch (variable.type){
                    case DEFI:
                    case intV:
                        SD.writeInt(0);
                        break;
                    case DEFD:
                    case doubleV:
                        SD.writeDouble(0.0);
                        break;
                    case DEFS:
                    case stringV:
                        SD.writeShort(variable.vs);
                        break;
                    case DEFAI:
                    case intAV:
                        for (int i = 0; i < variable.elementInt; i++) {
                            SD.writeInt(0);
                        }
                        break;
                    case DEFAD:
                    case doubleAV:
                        for (int i = 0; i < variable.elementInt; i++) {
                            SD.writeDouble(0.0);
                        }
                        break;
                    case DEFAS:
                    case stringAV:
                        int counter = variable.vs;
                        for (int i = 0; i < variable.elementInt; i++) {
                            SD.writeShort(counter++);
                        }
                        break;
                }
            }

            //STN READ
            try {
                raf.seek(12);
                while(true)
                {
                    tempInt = raf.readByte();
                    switch (tempInt){
                        case NOP:
                            pc++;
                            break;
                        case ADD:
                            pc++;
                            add();
                            break;
                        case SUB:
                            pc++;
                            sub();
                            break;
                        case MULT:
                            pc++;
                            mult();
                            break;
                        case DIV:
                            pc++;
                            div();
                            break;
                        case MOD:
                            mod();
                            pc++;
                            break;
                        case INC:
                            pc++;
                            dir = raf.readShort();
                            SD.seek(dir);
                            tempInt = SD.readInt();
                            pc+=2;
                            stack.push(tempInt);
                            stack.push(new Integer(1));
                            add();
                            tempInt = (Integer) stack.pop();
                            SD.seek(dir);
                            SD.writeInt(tempInt);
                            break;
                        case DEC:
                            pc++;
                            dir = raf.readShort();
                            SD.seek(dir);
                            tempInt = SD.readInt();
                            pc+=2;
                            stack.push(tempInt);
                            stack.push(new Integer(1));
                            sub();
                            tempInt = (Integer) stack.pop();
                            SD.seek(dir);
                            SD.writeInt(tempInt);
                            break;
                        case CMPEQ:
                            pc++;
                            CMPEQ();
                            break;
                        case CMPNE:
                            CMPNE();
                            pc++;
                            break;
                        case CMPLT:
                            CMPLT();
                            pc++;
                            break;
                        case CMPLE:
                            CMPLE();
                            pc++;
                            break;
                        case CMPGT:
                            CMPGT();
                            pc++;
                            break;
                        case CMPGE:
                            CMPGE();
                            pc++;
                            break;
                        case JMP:
                            pc++;
                            dir = raf.readShort();
                            pc = dir;
                            raf.seek(dir+12);
                            break;
                        case JMPT:
                            pc++;
                            if ((boolean) stack.pop())
                            {
                                dir = raf.readShort();
                                pc = dir;
                                raf.seek(dir+12);
                            } else {
                                raf.readShort();
                                pc+=2;
                            }
                            break;
                        case JMPF:
                            pc++;
                            if (!((boolean) stack.pop()))
                            {
                                dir = raf.readShort();
                                pc = dir;
                                raf.seek(dir+12);
                            } else {
                                raf.readShort();
                                pc+=2;
                            }
                            break;
                        case SETIDX:
                            pc++;
                            dir = raf.readShort();
                            pc+=2;
                            SD.seek(dir);
                            index = SD.readInt();
                            break;
                        case SETIDXK:
                            pc++;
                            index = raf.readInt();
                            pc+=4;
                            break;
                        case PUSHI:
                            pc++;
                            dir = raf.readShort();
                            pc+=2;
                            SD.seek(dir);
                            tempInt = SD.readInt();
                            stack.push(tempInt);
                            break;
                        case PUSHD:
                            pc++;
                            dir = raf.readShort();
                            pc+=2;
                            SD.seek(dir);
                            tempDouble = SD.readDouble();
                            stack.push(tempDouble);
                            break;
                        case PUSHS:
                            pc++;
                            dir = raf.readShort();
                            pc+=2;
                            SD.seek(dir);
                            vs = SD.readShort();
                            stack.push(vString[vs]);
                            break;
                        case PUSHAI:
                            pc++;
                            dir = raf.readShort();
                            pc+=2;
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            if ( index < tempElement.elementInt ) {
                                SD.seek(dir + (4L * index));
                                tempInt = SD.readInt();
                            } else throw new RuntimeException("Error in line "+ pc +": Array index out of bounds");
                            stack.push(tempInt);
                            break;
                        case PUSHAD:
                            pc++;
                            dir = raf.readShort();
                            pc+=2;
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            if ( index < tempElement.elementInt ) {
                                SD.seek(dir + (8L * index));
                                tempDouble = SD.readInt();
                            } else throw new RuntimeException("Error in line "+ pc +": Array index out of bounds");
                            stack.push(tempDouble);
                            break;
                        case PUSHAS:
                            pc++;
                            dir = raf.readShort();
                            pc+=2;
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            if ( index < tempElement.elementInt ) {
                                SD.seek(dir + (2L * index));
                                vs = SD.readShort();
                            } else throw new RuntimeException("Error in line "+ pc +": Array index out of bounds");
                            stack.push(vString[vs]);
                            break;
                        case PUSHKI:
                            pc++;
                            tempInt = raf.readInt();
                            pc+=4;
                            stack.push(tempInt);
                            break;
                        case PUSHKD:
                            pc++;
                            tempDouble = raf.readDouble();
                            pc+=8;
                            stack.push(tempDouble);
                            break;
                        case PUSHKS:
                            pc++;
                            tempString =  raf.readUTF();
                            pc += tempString.length() + 2;
                            stack.push(tempString);
                            break;
                        case POPI:
                            pc++;
                            dir = raf.readShort();
                            pc +=2;
                            tempInt = (int) stack.pop();
                            SD.seek(dir);
                            SD.writeInt(tempInt);
                            break;
                        case POPD:
                            pc++;
                            dir = raf.readShort();
                            pc +=2;
                            tempDouble = (double) stack.pop();
                            SD.seek(dir);
                            SD.writeDouble(tempDouble);
                            break;
                        case POPS:
                            pc++;
                            dir = raf.readShort();
                            pc +=2;
                            tempString = (String) stack.pop();
                            SD.seek(dir);
                            vs = SD.readShort();
                            vString[vs] = tempString;
                            break;
                        case POPAI:
                            pc++;
                            dir = raf.readShort();
                            pc +=2;
                            tempInt = (int) stack.pop();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            if ( index < tempElement.elementInt ) {
                                SD.seek(dir + (4L * index));
                                SD.writeInt(tempInt);
                            } else throw new RuntimeException("Error in line "+ pc +": Array index out of bounds");
                            break;
                        case POPAD:
                            pc++;
                            dir = raf.readShort();
                            pc +=2;
                            tempDouble = (double) stack.pop();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            if ( index < tempElement.elementInt ) {
                                SD.seek(dir + (8L * index));
                                SD.writeDouble(tempDouble);
                            } else throw new RuntimeException("Error in line "+ pc +": Array index out of bounds");
                            break;
                        case POPAS:
                            pc++;
                            dir = raf.readShort();
                            pc +=2;
                            tempString = (String) stack.pop();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            if ( index < tempElement.elementInt ) {
                                SD.seek(dir + (2L * index));
                                vs = SD.readShort();
                                vString[vs] = tempString;
                            } else throw new RuntimeException("Error in line "+ pc +": Array index out of bounds");
                            break;
                        case POPIDX:
                            pc++;
                            index = (int) stack.pop();
                            break;
                        case READI:
                            pc++;
                            dir = raf.readShort();
                            pc +=2;
                            //System.out.println("Reading next int:");
                            tempInt = sc.nextInt();
                            SD.seek(dir);
                            SD.writeInt(tempInt);
                            break;
                        case READD:
                            pc++;
                            dir = raf.readShort();
                            pc +=2;
                            //System.out.println("Reading next double:");
                            tempDouble = sc.nextDouble();
                            SD.seek(dir);
                            SD.writeDouble(tempDouble);
                            break;
                        case READS:
                            pc++;
                            dir = raf.readShort();
                            pc +=2;
                            //System.out.println("Reading next string:");
                            tempString = sc.nextLine();
                            SD.seek(dir);
                            vs = SD.readShort();
                            vString[vs] = tempString;
                            break;
                        case READAI:
                            pc++;
                            dir = raf.readShort();
                            pc +=2;
                            //System.out.println("Reading next int:");
                            tempInt = sc.nextInt();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            if ( index < tempElement.elementInt ) {
                                SD.seek(dir + (4L * index));
                                SD.writeInt(tempInt);
                            } else throw new RuntimeException("Error in line "+ pc +": Array index out of bounds");
                            break;
                        case READAD:
                            pc++;
                            dir = raf.readShort();
                            pc +=2;
                            //System.out.println("Reading next double:");
                            tempDouble = sc.nextDouble();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            if ( index < tempElement.elementInt ) {
                                SD.seek(dir + (8L * index));
                                SD.writeDouble(tempDouble);
                            } else throw new RuntimeException("Error in line "+ pc +": Array index out of bounds");
                            break;
                        case READAS:
                            pc++;
                            dir = raf.readShort();
                            pc +=2;
                            //System.out.println("Reading next string:");
                            tempString = sc.nextLine();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            if ( index < tempElement.elementInt ) {
                                SD.seek(dir + (2L * index));
                                vs = SD.readShort();
                                vString[vs] = tempString;
                            } else throw new RuntimeException("Error in line "+ pc +": Array index out of bounds");
                            break;
                        case PRTM:
                            pc++;
                            tempString = raf.readUTF();
                            pc += tempString.length() + 2;
                            System.out.print(tempString);
                            break;
                        case PRTI:
                            pc++;
                            dir = raf.readShort();
                            pc += 2;
                            SD.seek(dir);
                            tempInt = SD.readInt();
                            System.out.print(tempInt);
                            break;
                        case PRTD:
                            pc++;
                            dir = raf.readShort();
                            pc += 2;
                            SD.seek(dir);
                            tempDouble = SD.readDouble();
                            System.out.print(tempDouble);
                            break;
                        case PRTS:
                            pc++;
                            dir = raf.readShort();
                            pc += 2;
                            SD.seek(dir);
                            vs = SD.readShort();
                            System.out.print(vString[vs]);
                            break;
                        case PRTAI:
                            pc++;
                            dir = raf.readShort();
                            pc += 2;
                            SD.seek(dir);
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            if ( index < tempElement.elementInt ) {
                                SD.seek(dir + (4L * index));
                                tempInt = SD.readInt();
                            } else throw new RuntimeException("Error in line "+ pc +": Array index out of bounds");
                            System.out.print(tempInt);
                            break;
                        case PRTAD:
                            pc++;
                            dir = raf.readShort();
                            pc += 2;
                            SD.seek(dir);
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            if ( index < tempElement.elementInt ) {
                                SD.seek(dir + (8L * index));
                                tempDouble = SD.readInt();
                            } else throw new RuntimeException("Error in line "+ pc +": Array index out of bounds");
                            System.out.print(tempDouble);
                            break;
                        case PRTAS:
                            pc++;
                            dir = raf.readShort();
                            pc += 2;
                            SD.seek(dir);
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            if ( index < tempElement.elementInt ) {
                                SD.seek(dir + (2L * index));
                                vs = SD.readShort();
                            } else throw new RuntimeException("Error in line "+ pc +": Array index out of bounds");
                            System.out.print(vString[vs]);
                            break;
                        case NL:
                            pc++;
                            System.out.println("");
                            break;
                        case HALT:
                            pc++;
                            throw new EOFException("Program Ended");
                        default:
                            System.out.println("Position READ Error");
                            break;
                    }
                }
            } catch (EOFException e){
                raf.close();
                SD.close();
            }
        } catch (FileNotFoundException e){
            System.out.println("-- Cannot find input file");
            System.exit(1);
        } catch (IOException e){
            System.out.println("-- Cannot open input file");
            System.exit(1);
        } catch (ClassCastException cce){
            System.err.println("Error in line "+ pc + ": Invalid value for declared variable");
            System.err.println(cce.getMessage().replace("java.lang.",""));
            System.exit(1);
        } catch (RuntimeException rte){
            System.err.println(rte.getMessage());
            System.err.println("Check file dass.txt for debugging");
            System.exit(1);
        }
    }

    public static void add(){
        Object sumando = stack.pop();
        Object sumando2 = stack.pop();

        if(sumando instanceof Integer && sumando2 instanceof Integer){
            //object conversion
            stack.push(((Integer) sumando + (Integer) sumando2));
        } else if(sumando instanceof Integer && sumando2 instanceof String){
            stack.push(((Integer) sumando + (String) sumando2));
        } else if(sumando instanceof Integer && sumando2 instanceof Double){
            stack.push(((Integer) sumando + (Double) sumando2));
        } else if(sumando instanceof String && sumando2 instanceof String){
            stack.push(((String) sumando + (String) sumando2));
        } else if(sumando instanceof String && sumando2 instanceof Integer){
            stack.push(((String) sumando + (Integer) sumando2));
        } else if(sumando instanceof String && sumando2 instanceof Double){
            stack.push(((String) sumando + (Double) sumando2));
        } else if(sumando instanceof Double && sumando2 instanceof Double){
            stack.push(((Double) sumando + (Double) sumando2));
        } else if(sumando instanceof Double && sumando2 instanceof Integer){
            stack.push(((Double) sumando + (Integer) sumando2));
        } else if(sumando instanceof Double && sumando2 instanceof String){
            stack.push(((Double) sumando + (String) sumando2));
        } else if(sumando instanceof Boolean && sumando2 instanceof Boolean){
            stack.push(((Boolean) sumando | (Boolean) sumando2));
        } else {
            throw new RuntimeException("Error in line "+ pc + ": Data type is not supported with this operation");
        }
    }

    public static void sub(){
        Object sustraendo = stack.pop();
        Object minuendo = stack.pop();

        if(minuendo instanceof Integer && sustraendo instanceof Integer){
            //object conversion
            stack.push(((Integer) minuendo - (Integer) sustraendo));
        } else if(minuendo instanceof Integer && sustraendo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in substract operation");
        } else if(minuendo instanceof Integer && sustraendo instanceof Double){
            stack.push(((Integer) minuendo - (Double) sustraendo));
        } else if(minuendo instanceof String && sustraendo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in substract operation");
        } else if(minuendo instanceof String && sustraendo instanceof Integer){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in substract operation");
        } else if(minuendo instanceof String && sustraendo instanceof Double){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in substract operation");
        } else if(minuendo instanceof Double && sustraendo instanceof Double){
            stack.push(((Double) minuendo - (Double) sustraendo));
        } else if(minuendo instanceof Double && sustraendo instanceof Integer){
            stack.push(((Double) minuendo - (Integer) sustraendo));
        } else if(minuendo instanceof Double && sustraendo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in substract operation");
        } else {
            throw new RuntimeException("Error in line "+ pc + ": Data type is not supported with this operation");
        }
    }

    public static void mult(){
        Object factor1 = stack.pop();
        Object factor2 = stack.pop();

        if(factor2 instanceof Integer && factor1 instanceof Integer){
            //object conversion
            stack.push(((Integer) factor2 * (Integer) factor1));
        } else if(factor2 instanceof Integer && factor1 instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in multiplication operation");
        } else if(factor2 instanceof Integer && factor1 instanceof Double){
            stack.push(((Integer) factor2 * (Double) factor1));
        } else if(factor2 instanceof String && factor1 instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in multiplication operation");
        } else if(factor2 instanceof String && factor1 instanceof Integer){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in multiplication operation");
        } else if(factor2 instanceof String && factor1 instanceof Double){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in multiplication operation");
        } else if(factor2 instanceof Double && factor1 instanceof Double){
            stack.push(((Double) factor2 * (Double) factor1));
        } else if(factor2 instanceof Double && factor1 instanceof Integer){
            stack.push(((Double) factor2 * (Integer) factor1));
        } else if(factor2 instanceof Double && factor1 instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in multiplication operation");
        } else if(factor2 instanceof Boolean && factor1 instanceof Boolean){
            stack.push(((Boolean) factor2 & (Boolean) factor1));
        } else {
            throw new RuntimeException("Error in line "+ pc + ": Data type is not supported with this operation");
        }
    }

    public static void div(){
        Object divisor = stack.pop();
        Object dividendo = stack.pop();

        if(dividendo instanceof Integer && divisor instanceof Integer){
            //object conversion
            if ((Integer) divisor == 0) throw new RuntimeException("Error in line "+ pc +": Cannot divide by 0");
            else stack.push(((Integer) dividendo / (Integer) divisor));
        } else if(dividendo instanceof Integer && divisor instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in division operation");
        } else if(dividendo instanceof Integer && divisor instanceof Double){
            if ((Double) divisor == 0) throw new RuntimeException("Error in line "+ pc +": Cannot divide by 0");
            else stack.push(((Integer) dividendo / (Double) divisor));
        } else if(dividendo instanceof String && divisor instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in division operation");
        } else if(dividendo instanceof String && divisor instanceof Integer){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in division operation");
        } else if(dividendo instanceof String && divisor instanceof Double){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in division operation");
        } else if(dividendo instanceof Double && divisor instanceof Double){
            if ((Double) divisor == 0) throw new RuntimeException("Error in line "+ pc +": Cannot divide by 0");
            else stack.push(((Double) dividendo / (Double) divisor));
        } else if(dividendo instanceof Double && divisor instanceof Integer){
            if ((Integer) divisor == 0) throw new RuntimeException("Error in line "+ pc +": Cannot divide by 0");
            else stack.push(((Double) dividendo / (Integer) divisor));
        } else if(dividendo instanceof Double && divisor instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in division operation");
        } else {
            throw new RuntimeException("Error in line "+ pc + ": Data type is not supported with this operation");
        }
    }

    public static void mod(){
        Object divisor = stack.pop();
        Object dividendo = stack.pop();

        if(dividendo instanceof Integer && divisor instanceof Integer){
            //object conversion
            if ((Integer) divisor == 0) throw new RuntimeException("Error in line "+ pc +": Cannot module by 0");
            else stack.push(((Integer) dividendo % (Integer) divisor));
        } else if(dividendo instanceof Integer && divisor instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in modulo operation");
        } else if(dividendo instanceof Integer && divisor instanceof Double){
            if ((Double) divisor == 0) throw new RuntimeException("Error in line "+ pc +": Cannot module by 0");
            else stack.push(((Integer) dividendo % (Double) divisor));
        } else if(dividendo instanceof String && divisor instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in modulo operation");
        } else if(dividendo instanceof String && divisor instanceof Integer){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in modulo operation");
        } else if(dividendo instanceof String && divisor instanceof Double){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in modulo operation");
        } else if(dividendo instanceof Double && divisor instanceof Double){
            if ((Double) divisor == 0) throw new RuntimeException("Error in line "+ pc +": Cannot module by 0");
            else stack.push(((Double) dividendo % (Double) divisor));
        } else if(dividendo instanceof Double && divisor instanceof Integer){
            if ((Integer) divisor == 0) throw new RuntimeException("Error in line "+ pc +": Cannot module by 0");
            else stack.push(((Double) dividendo % (Integer) divisor));
        } else if(dividendo instanceof Double && divisor instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in modulo operation");
        } else {
            throw new RuntimeException("Error in line "+ pc + ": Data type is not supported with this operation");
        }
    }

    public static void CMPEQ(){
        Object segundo = stack.pop();
        Object primero = stack.pop();

        if(primero instanceof Integer && segundo instanceof Integer){
            //object conversion
            stack.push(((int) primero) == ((int) segundo));
        } else if(primero instanceof Integer && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof Integer && segundo instanceof Double){
            stack.push(((int) primero == (double) segundo));
        } else if(primero instanceof String && segundo instanceof String){
            stack.push(((String) primero).equals((String) segundo));
        } else if(primero instanceof String && segundo instanceof Integer){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof String && segundo instanceof Double){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof Double && segundo instanceof Double){
            stack.push(((double) primero == (double) segundo));
        } else if(primero instanceof Double && segundo instanceof Integer){
            stack.push(((double) primero == (int) segundo));
        } else if(primero instanceof Double && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else {
            throw new RuntimeException("Error in line "+ pc + ": Data type is not supported with this operation");
        }
    }

    public static void CMPNE(){
        Object segundo = stack.pop();
        Object primero = stack.pop();

        if(primero instanceof Integer && segundo instanceof Integer){
            //object conversion
            stack.push(((int) primero) != ((int) segundo));
        } else if(primero instanceof Integer && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof Integer && segundo instanceof Double){
            stack.push(((int) primero != (double) segundo));
        } else if(primero instanceof String && segundo instanceof String){
            stack.push(!((String) primero).equals((String) segundo));
        } else if(primero instanceof String && segundo instanceof Integer){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof String && segundo instanceof Double){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof Double && segundo instanceof Double){
            stack.push(((double) primero != (double) segundo));
        } else if(primero instanceof Double && segundo instanceof Integer){
            stack.push(((double) primero != (int) segundo));
        } else if(primero instanceof Double && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else {
            throw new RuntimeException("Error in line "+ pc + ": Data type is not supported with this operation");
        }
    }

    public static void CMPLT(){
        Object segundo = stack.pop();
        Object primero = stack.pop();

        if(primero instanceof Integer && segundo instanceof Integer){
            //object conversion
            stack.push(((int) primero) < ((int) segundo));
        } else if(primero instanceof Integer && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof Integer && segundo instanceof Double){
            stack.push(((int) primero < (double) segundo));
        } else if(primero instanceof String && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof String && segundo instanceof Integer){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof String && segundo instanceof Double){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof Double && segundo instanceof Double){
            stack.push(((double) primero < (double) segundo));
        } else if(primero instanceof Double && segundo instanceof Integer){
            stack.push(((double) primero < (int) segundo));
        } else if(primero instanceof Double && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else {
            throw new RuntimeException("Error in line "+ pc + ": Data type is not supported with this operation");
        }
    }

    public static void CMPLE(){
        Object segundo = stack.pop();
        Object primero = stack.pop();

        if(primero instanceof Integer && segundo instanceof Integer){
            //object conversion
            stack.push(((int) primero) <= ((int) segundo));
        } else if(primero instanceof Integer && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof Integer && segundo instanceof Double){
            stack.push(((int) primero <= (double) segundo));
        } else if(primero instanceof String && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof String && segundo instanceof Integer){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof String && segundo instanceof Double){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof Double && segundo instanceof Double){
            stack.push(((double) primero <= (double) segundo));
        } else if(primero instanceof Double && segundo instanceof Integer){
            stack.push(((double) primero <= (int) segundo));
        } else if(primero instanceof Double && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else {
            throw new RuntimeException("Error in line "+ pc + ": Data type is not supported with this operation");
        }
    }

    public static void CMPGT(){
        Object segundo = stack.pop();
        Object primero = stack.pop();

        if(primero instanceof Integer && segundo instanceof Integer){
            //object conversion
            stack.push(((int) primero) > ((int) segundo));
        } else if(primero instanceof Integer && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof Integer && segundo instanceof Double){
            stack.push(((int) primero > (double) segundo));
        } else if(primero instanceof String && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof String && segundo instanceof Integer){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof String && segundo instanceof Double){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof Double && segundo instanceof Double){
            stack.push(((double) primero > (double) segundo));
        } else if(primero instanceof Double && segundo instanceof Integer){
            stack.push(((double) primero > (int) segundo));
        } else if(primero instanceof Double && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else {
            throw new RuntimeException("Error in line "+ pc + ": Data type is not supported with this operation");
        }
    }

    public static void CMPGE(){
        Object segundo = stack.pop();
        Object primero = stack.pop();

        if(primero instanceof Integer && segundo instanceof Integer){
            //object conversion
            stack.push(((int) primero) >= ((int) segundo));
        } else if(primero instanceof Integer && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof Integer && segundo instanceof Double){
            stack.push(((int) primero >= (double) segundo));
        } else if(primero instanceof String && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof String && segundo instanceof Integer){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof String && segundo instanceof Double){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else if(primero instanceof Double && segundo instanceof Double){
            stack.push(((double) primero >= (double) segundo));
        } else if(primero instanceof Double && segundo instanceof Integer){
            stack.push(((double) primero >= (int) segundo));
        } else if(primero instanceof Double && segundo instanceof String){
            throw new RuntimeException("Error in line "+ pc +": String type cannot be used in comparison operation");
        } else {
            throw new RuntimeException("Error in line "+ pc + ": Data type is not supported with this operation");
        }
    }

    public static void main(String[] args) {
        Token t;
        if (args.length > 1) {
            stn = args[0];
            stnv = args[1];
            System.out.println("");
            System.out.println("stn file Path: " + args[0]);
            System.out.println("stnv file Path: " + args[1]);
            System.out.println("");
            File f = new File("SD.bin");
            if (f.delete()) System.out.println("Old SD.bin file deleted");
            System.out.println("---------------------Running Virtual Machine---------------------");
            runTime();
            System.out.println("\n---------------------Program run ended ---------------------\n\n");
        } else System.out.println("-- Syntax: VMachine <stnFilePath> <stnvFilePath>");
    }
}


