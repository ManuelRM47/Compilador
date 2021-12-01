package DisASSembler;

import CFlat.Token;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DisASSembler {
    public static int pc = 0;
    public static String tempString, stn, stnv, gen, source;
    public static int tempInt = 0;
    public static double tempDouble = 0;
    public static String name;
    public static int dir;
    public static int type;
    public static int elemenNum;
    public static int vs;
    public static boolean err = false;
    public static byte[] magicNumbers = new byte[6];
    public static byte[] NameBytes = new byte[30];
    public static TableElement tempElement;

    public static List<TableElement> variableTable = new  ArrayList<TableElement>();

    public static final int
            NOP      = 0,
            ADD      = 1,
            SUB      = 2,
            MULT     = 3,
            DIV      = 4,
            MOD      = 5,
            INC      = 6,
            DEC      = 7,
            CMPEQ    = 8,
            CMPNE    = 9,
            CMPLT    = 10,
            CMPLE    = 11,
            CMPGT    = 12,
            CMPGE    = 13,
            JMP      = 14,
            JMPT     = 15,
            JMPF     = 16,
            SETIDX   = 17,
            SETIDXK  = 18,
            PUSHI    = 19,
            PUSHD    = 20,
            PUSHS    = 21,
            PUSHAI   = 22,
            PUSHAD   = 23,
            PUSHAS   = 24,
            PUSHKI   = 25,
            PUSHKD   = 26,
            PUSHKS   = 27,
            POPI     = 28,
            POPD     = 29,
            POPS     = 30,
            POPAI    = 31,
            POPAD    = 32,
            POPAS    = 33,
            POPIDX   = 34,
            READI    = 35,
            READD    = 36,
            READS    = 37,
            READAI   = 38,
            READAD   = 39,
            READAS   = 40,
            PRTM     = 41,
            PRTI     = 42,
            PRTD     = 43,
            PRTS     = 44,
            PRTAI    = 45,
            PRTAD    = 46,
            PRTAS    = 47,
            NL       = 48,
            HALT     = 49,
            DEFI     = 26,
            DEFD     = 27,
            DEFS     = 28,
            DEFAI    = 29,
            DEFAD    = 30,
            DEFAS    = 31,
            intV     = 44,
            doubleV  = 45,
            stringV  = 46,
            intAV    = 47,
            doubleAV = 48,
            stringAV = 49;

    public static void disassembler(){
        try {
            program2();
            if (err) System.out.println("Disassembled file could not be generated");
            else System.out.println(gen + ".txt file generated");
        } catch (Exception E){
            System.out.println("-- cannot open input file");
        }
    }

    private static void program2() {
        try {
            //STN File Reader
            RandomAccessFile raf = new RandomAccessFile(new File(stn), "r");
            RandomAccessFile rafv = new RandomAccessFile(new File(stnv), "r");
            raf.seek(0);
            rafv.seek(0);
            File f = new File(gen + ".txt");
            f.delete();
            //ASE File Writer
            FileOutputStream outputStream = new FileOutputStream(f);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
            BufferedWriter bw = new BufferedWriter(outputStreamWriter);

            raf.read(magicNumbers,0,6);
            tempString = new String(magicNumbers);

            if (!tempString.equals("ICCTSN")){
                System.out.println("File magic numbers are not valid for this Disassembler");
                err = true;
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
                System.out.println("STNV finished reading");
            }

            for (TableElement variable : variableTable){
                switch (variable.type){
                    case DEFI:
                    case intV:
                        bw.write("DEFI ");
                        bw.write(variable.name.trim());
                        bw.newLine();
                        break;
                    case DEFD:
                    case doubleV:
                        bw.write("DEFD ");
                        bw.write(variable.name.trim());
                        bw.newLine();
                        break;
                    case DEFS:
                    case stringV:
                        bw.write("DEFS ");
                        bw.write(variable.name.trim());
                        bw.newLine();
                        break;
                    case DEFAI:
                    case intAV:
                        bw.write("DEFAI ");
                        bw.write(variable.name.trim());
                        bw.write(",");
                        bw.write(Integer.toString(variable.elementInt));
                        bw.newLine();
                        break;
                    case DEFAD:
                    case doubleAV:
                        bw.write("DEFAD ");
                        bw.write(variable.name.trim());
                        bw.write(",");
                        bw.write(Integer.toString(variable.elementInt));
                        bw.newLine();
                        break;
                    case DEFAS:
                    case stringAV:
                        bw.write("DEFAS ");
                        bw.write(variable.name.trim());
                        bw.write(",");
                        bw.write(Integer.toString(variable.elementInt));
                        bw.newLine();
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
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("NOP");
                            bw.newLine();
                            break;
                        case ADD:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("ADD");
                            bw.newLine();
                            break;
                        case SUB:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("SUB");
                            bw.newLine();
                            break;
                        case MULT:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("MULT");
                            bw.newLine();
                            break;
                        case DIV:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("DIV");
                            bw.newLine();
                            break;
                        case MOD:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("MOD");
                            bw.newLine();
                            break;
                        case INC:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("INC ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc+=2;
                            bw.newLine();
                            break;
                        case DEC:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("DEC ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc += 2;
                            bw.newLine();
                            break;
                        case CMPEQ:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("CMPEQ");
                            bw.newLine();
                            break;
                        case CMPNE:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("CMPNE");
                            bw.newLine();
                            break;
                        case CMPLT:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("CMPLT");
                            bw.newLine();
                            break;
                        case CMPLE:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("CMPLE");
                            bw.newLine();
                            break;
                        case CMPGT:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("CMPGT");
                            bw.newLine();
                            break;
                        case CMPGE:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("CMPGE");
                            bw.newLine();
                            break;
                            // Que hacer con labels
                        case JMP:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("JMP ");
                            dir = raf.readShort();
                            bw.write("e"+Integer.toString(dir));
                            pc+=2;
                            bw.newLine();
                            break;
                        case JMPT:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("JMPT ");
                            dir = raf.readShort();
                            bw.write("e"+Integer.toString(dir));
                            pc+=2;
                            bw.newLine();
                            break;
                        case JMPF:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("JMPF ");
                            dir = raf.readShort();
                            bw.write("e"+Integer.toString(dir));
                            pc+=2;
                            bw.newLine();
                            break;
                        case SETIDX:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("SETIDX ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc+=2;
                            bw.newLine();
                            break;
                        case SETIDXK:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("SETIDXK ");
                            dir = raf.readInt();
                            bw.write(Integer.toString(dir));
                            pc+=4;
                            bw.newLine();
                            break;
                        case PUSHI:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PUSHI ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc+=2;
                            bw.newLine();
                            break;
                        case PUSHD:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PUSHD ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc += 2;
                            bw.newLine();
                            break;
                        case PUSHS:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PUSHS ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc+=2;
                            bw.newLine();
                            break;
                        case PUSHAI:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PUSHAI ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc+=2;
                            bw.newLine();
                            break;
                        case PUSHAD:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PUSHAD ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc+=2;
                            bw.newLine();
                            break;
                        case PUSHAS:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PUSHAS ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc+=2;
                            bw.newLine();
                            break;
                        case PUSHKI:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PUSHKI ");
                            dir = raf.readInt();
                            bw.write(Integer.toString(dir));
                            pc+=4;
                            bw.newLine();
                            break;
                        case PUSHKD:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PUSHKD ");
                            tempDouble = raf.readDouble();
                            bw.write(Double.toString(tempDouble));
                            pc+=8;
                            bw.newLine();
                            break;
                            //que hacer con strings
                        case PUSHKS:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PUSHKS ");
                            tempString =  raf.readUTF();
                            bw.write('"' + tempString +'"');
                            pc += tempString.length() + 2;
                            bw.newLine();
                            break;
                        case POPI:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("POPI ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc +=2;
                            bw.newLine();
                            break;
                        case POPD:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("POPD ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc +=2;
                            bw.newLine();
                            break;
                        case POPS:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("POPS ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc +=2;
                            bw.newLine();
                            break;
                        case POPAI:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("POPAI ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc +=2;
                            bw.newLine();
                            break;
                        case POPAD:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("POPAD ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc +=2;
                            bw.newLine();
                            break;
                        case POPAS:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("POPAS ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc +=2;
                            bw.newLine();
                            break;
                        case POPIDX:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("POPIDX ");
                            bw.newLine();
                            break;
                        case READI:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("READI ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc +=2;
                            bw.newLine();
                            break;
                        case READD:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("READD ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc +=2;
                            bw.newLine();
                            break;
                        case READS:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("READS ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc +=2;
                            bw.newLine();
                            break;
                        case READAI:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("READAI ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc +=2;
                            bw.newLine();
                            break;
                        case READAD:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("READAD ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc +=2;
                            bw.newLine();
                            break;
                        case READAS:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("READAS ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc +=2;
                            bw.newLine();
                            break;
                        case PRTM:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PRTM ");
                            tempString = raf.readUTF();
                            bw.write('"' + tempString +'"');
                            pc += tempString.length() + 2;
                            bw.newLine();
                            break;
                        case PRTI:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PRTI ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc += 2;
                            bw.newLine();
                            break;
                        case PRTD:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PRTD ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc += 2;
                            bw.newLine();
                            break;
                        case PRTS:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PRTS ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc += 2;
                            bw.newLine();
                            break;
                        case PRTAI:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PRTAI ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc += 2;
                            bw.newLine();
                            break;
                        case PRTAD:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PRTAD ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc += 2;
                            bw.newLine();
                            break;
                        case PRTAS:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("PRTAS ");
                            dir = raf.readShort();
                            tempElement = variableTable.stream()
                                    .filter(TableElement -> (dir == TableElement.dir))
                                    .findAny()
                                    .orElse(null);
                            bw.write(tempElement.name.trim());
                            pc += 2;
                            bw.newLine();
                            break;
                        case NL:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("NL ");
                            bw.newLine();
                            break;
                        case HALT:
                            bw.write("e" + Integer.toString(pc)+ ": ");
                            pc++;
                            bw.write("HALT ");
                            bw.newLine();
                            break;
                        default:
                            System.out.println("Position READ Error");
                            System.exit(1);
                            break;
                    }
                }
            } catch (EOFException e){
                System.out.println("STN finished reading");
            }
            raf.close();
            bw.close();
        } catch (FileNotFoundException e){
            err = true;
            System.out.println(e.getMessage());
        } catch (IOException e){
            err = true;
            System.out.println(e.getMessage());
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
            source = args[0];
            File f2 = new File(source);
            gen = f2.getAbsolutePath().substring(f2.getAbsolutePath().lastIndexOf("\\")+1).split("\\.")[0];;
            System.out.println("---------------------Generating Decompiler---------------------");
            disassembler();
            if (!err) System.out.println("---------------------Program Decompiled successfully---------------------\n\n");
        } else System.out.println("-- Syntax: Disassembler <stnFilePath> <stnvFilePath>");
    }
}
