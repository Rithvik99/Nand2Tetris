import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Assembler 
{

    public static HashMap<String,Integer> symMap = new HashMap<String, Integer>();
    public static HashMap<String,String> compMapA = new HashMap<String, String>();
    public static HashMap<String,String> comMapM = new HashMap<String, String>();
    public static HashMap<String,String> destMap = new HashMap<String, String>();
    public static HashMap<String,String> jumpMap = new HashMap<String, String>();

    static 
    {
        //adding all the pre-defined symbols to the symbols map
        symMap.put("R0",0);
        symMap.put("R1",1);
        symMap.put("R2",2);
        symMap.put("R3",3);
        symMap.put("R4",4);
        symMap.put("R5",5);
        symMap.put("R6",6);
        symMap.put("R7",7);
        symMap.put("R8",8);
        symMap.put("R9",9);
        symMap.put("R10",10);
        symMap.put("R11",11);
        symMap.put("R12",12);
        symMap.put("R13",13);
        symMap.put("R14",14);
        symMap.put("R15",15);
        symMap.put("SCREEN",16384);
        symMap.put("KBD",24576);
        symMap.put("SP",0);
        symMap.put("LCL",1);
        symMap.put("ARG",2);
        symMap.put("THIS",3);
        symMap.put("THAT",4);

        //for c instructions comp=dest;jmp

        //adding all the computations that involves A to computations map, generally a=0;
        compMapA.put("0","101010");
        compMapA.put("1","111111");
        compMapA.put("-1","111010");
        compMapA.put("D","001100");
        compMapA.put("A","110000");
        compMapA.put("!D","001101");
        compMapA.put("!A","110001");
        compMapA.put("-D","001111");
        compMapA.put("-A","110011");
        compMapA.put("D+1","011111");
        compMapA.put("A+1","110111");
        compMapA.put("D-1","001110");
        compMapA.put("A-1","110010");
        compMapA.put("D+A","000010");
        compMapA.put("D-A","010011");
        compMapA.put("A-D","000111");
        compMapA.put("D&A","000000");
        compMapA.put("D|A","010101");

        //adding all the computations that involves M to computations map, generally a=1;
        comMapM.put("M","110000");
        comMapM.put("!M","110001");
        comMapM.put("-M","110011");
        comMapM.put("M+1","110111");
        comMapM.put("M-1","110010");
        comMapM.put("D+M","000010");
        comMapM.put("D-M","010011");
        comMapM.put("M-D","000111");
        comMapM.put("D&M","000000");
        comMapM.put("D|M","010101");

        //adding all the possible destinations in C instruction to destinations map
        destMap.put("","000");
        destMap.put("M","001");
        destMap.put("D","010");
        destMap.put("MD","011");
        destMap.put("A","100");
        destMap.put("AM","101");
        destMap.put("AD","110");
        destMap.put("AMD","111");

        //adding all the possible jumps in C instruction to jumps map
        jumpMap.put("","000");
        jumpMap.put("JGT","001");
        jumpMap.put("JEQ","010");
        jumpMap.put("JGE","011");
        jumpMap.put("JLT","100");
        jumpMap.put("JNE","101");
        jumpMap.put("JLE","110");
        jumpMap.put("JMP","111");

    }

    public static HashMap<String,Integer> findSymbols(String codes)
    {
        Scanner sc = new Scanner(codes);

        String line = "";
        int pc = 0;

        //patter for symbols generally in () with all caps
        Pattern p = Pattern.compile("^\\([^0-9][0-9A-Za-z\\_\\:\\.\\$]+\\)$");

        Matcher m =null;
        
        //mapping from symbol to its loaction
        HashMap<String,Integer> symbol = new HashMap<String, Integer>();

        while (sc.hasNextLine())
        {
            line = sc.nextLine();

            m = p.matcher(line);

            //if a match is found 
            if (m.find())
            {
                //adding symbol to the map with match as the next line number
                symbol.put(m.group().substring(1,m.group().length()-1), pc);
            }
            else
            {
                //incrementing the line number in case of no match
                pc++;
            }
        }

        return symbol;
    }


    public static String asmToHack(String codes)
    {
        Scanner sc = new Scanner(codes);

        int addressDec = 0; //value in dec
        int pc = 0,lineNumber = 0;
        int startAddress = 16;//start address for variable
        int temp = 0;//temp var
        int flag1 = -1; //flag for "="
        int flag2 = -1; //flag for ";"

        String line = ""; //line read from Scanner
        String varName = ""; //value name of @value
        String value = ""; //for A instruction, 0+value
        String a = ""; //for C instruction, 111+a+comp+dest+jmp
        String dest = "",comp = "",jmp = "",instructions = "";


        Pattern p = Pattern.compile("^[^0-9][0-9A-Za-z\\_\\:\\.\\$]+");//pattern of references

        Pattern pL = Pattern.compile("^\\([^0-9][0-9A-Za-z\\_\\:\\.\\$]+\\)$");//pattern of variable names

        HashMap<String,Integer> symbol = findSymbols(codes);

        HashMap<String,Integer> symbols = new HashMap<String, Integer>();

        while (sc.hasNextLine())
        {
            lineNumber++;
            line = sc.nextLine();

            if (line.charAt(0) == '@')
            {//line is an A instruction

                varName = line.substring(1); //name after @ in A instruction

                //if this is jump address for next instruction
                if (symbol.containsKey(varName))
                {
                    value = ExtraFunxions.padLeftZero(Integer.toBinaryString(symbol.get(varName)),15);
                }

                else 
                {
                    if (varName.matches("[0-9]+")) 
                    {//if its a value
                        value = ExtraFunxions.padLeftZero(Integer.toBinaryString(Integer.parseInt(varName)), 15);

                    } 
                    
                    else 
                    {//if it is a user defined symbol

                        if (symMap.containsKey(varName))
                        {//if it is already present in the map of symbols
                            value = ExtraFunxions.padLeftZero(Integer.toBinaryString(symMap.get(varName)), 15);
                        }
                        
                        else 
                        {//if it is not found in map of symbols
                            if (p.matcher(varName).find()) 
                            {//if its a variable name

                                if (symbols.containsKey(varName)) 
                                {//if map contains this key then get its value and translate into binary
                                    temp = symbols.get(varName);
                                    value = ExtraFunxions.padLeftZero(Integer.toBinaryString(temp), 15);
                                } 
                                
                                else 
                                {
                                    //adding it to map and assigning address to it.
                                    addressDec = symbols.size() + startAddress;

                                    symbols.put(varName, addressDec);

                                    value = ExtraFunxions.padLeftZero(Integer.toBinaryString(addressDec), 15);
                                }

                            } 
                            
                            else 
                            {
                                throw new IllegalStateException("Illegal user-defined symbol! Line " + lineNumber);
                            }
                        }

                    }
                }

                instructions += "0" + value + "\n";

                pc++;

            }
            else if (pL.matcher(line).find()) 
            {//if it is a reference symbol
                continue;

            }
            
            else 
            {//checking for C instruction

                flag1 = line.indexOf("=");
                flag2 = line.indexOf(";");
                dest = "";
                comp = "";
                jmp = "";

                //C instruction of form dest=comp;jump
                if (flag1 != -1 && flag2 != -1)
                {
                    dest = line.substring(0,flag1);
                    comp = line.substring(flag1 + 1,flag2);
                    jmp = line.substring(flag2 + 1);
                }
                
                //C instruction of form dest=comp
                else if (flag1 != -1 && flag2 == -1){

                    dest = line.substring(0,flag1);
                    comp = line.substring(flag1 + 1);
                }
                
                //C instruction of form comp;jump
                else if (flag1 == -1 && flag2 != -1)
                {
                    comp = line.substring(0,flag2);
                    jmp = line.substring(flag2 + 1);
                }
                
                //C instruction of form dest
                else 
                {
                    dest = line;
                }


                if (destMap.containsKey(dest) && (comMapM.containsKey(comp) || compMapA.containsKey(comp)) && jumpMap.containsKey(jmp))
                {

                    if (compMapA.containsKey(comp))
                    {
                        a = "0";
                        comp = compMapA.get(comp);
                    }

                    else 
                    {
                        a = "1";
                        comp = comMapM.get(comp);
                    }

                    instructions += "111" + a + comp + destMap.get(dest) + jumpMap.get(jmp) + "\n";

                }
                
                else
                {
                    throw new IllegalStateException("Wrong instruction format!Line " + lineNumber);
                }
            }

        }

        return instructions;
    }

    //converting asm file to hack file
    public static void translation(String dir)
    {

        File fIn = new File(dir);

        //if input file is not an .asm file, throw an exception and stop translation
        if (!ExtraFunxions.isAsm(fIn))
        {
            throw new IllegalArgumentException("Wrong file format! Only .asm is accepted!");
        }

        try 
        {
            Scanner sc = new Scanner(fIn);
            String preprocessed = "";

            while (sc.hasNextLine()){

                String line = sc.nextLine();

                line = ExtraFunxions.noSpaces(ExtraFunxions.noComments(line));

                if (line.length() > 0)
                {
                    preprocessed += line + "\n";
                }

            }

            //get rid of last "\n"
            preprocessed = preprocessed.trim();

            String res = asmToHack(preprocessed);

            String fileName = fIn.getName().substring(0,fIn.getName().indexOf("."));

            PrintWriter p = new PrintWriter(new File(fIn.getParentFile().getAbsolutePath() + "/" + fileName + ".hack"));

            p.print(res);

            p.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) 
    {

        if (args.length == 0)
        {
            System.out.println("Usage: Assembler filename");
            return;
        }

        translation(args[0]);
        
    }

}

class ExtraFunxions 
{

    public static String noComments(String strIn){

        int position = strIn.indexOf("//");

        if (position != -1){

            strIn = strIn.substring(0, position);

        }

        return strIn;
    }

    public static String noSpaces(String strIn){
        String res = "";

        if (strIn.length() != 0){

            String[] segs = strIn.split(" ");

            for (String s: segs){
                res += s;
            }
        }

        return res;
    }


    public static boolean isAsm(File fileIn){

        String filename = fileIn.getName();
        int position = filename.lastIndexOf(".");

        if (position != -1) {

            String ext = filename.substring(position);

            if (ext.toLowerCase().equals(".asm")) {
                return true;
            }
        }

        return false;
    }

    public static String padLeftZero(String strIn, int len){

        for (int i = strIn.length(); i < len; i++){
            strIn = "0" + strIn;
        }

        return strIn;
    }

}