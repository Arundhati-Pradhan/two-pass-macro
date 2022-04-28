import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

class Macro {
	ArrayList<String> code = new ArrayList<String>();

	public static void main(String[] args) throws FileNotFoundException {
		Macro main = new Macro();
		File file = new File("macro_input.txt");
		Scanner sc = new Scanner(file);
		String codeCopy = ""; 
		
		while (sc.hasNextLine()) {
			String data = sc.nextLine();
			codeCopy += data + "\n";
			main.code.add(data);
		}
		sc.close();

		PassOneMacroProcessor passOneMacro = new PassOneMacroProcessor();
		try {
			passOneMacro.processMacro(main.code, codeCopy);
		} catch (Exception e) {
			e.printStackTrace();	
		}
	}
}

class PassOneMacroProcessor {
	static int alaHash = 0;
	int nArgs = 0, nCalls = 0, mainFlag = 0, nMntIndex = 0;
	ArrayList<String> ALA = new ArrayList<String>();
	ArrayList<String> ALA_COPY = new ArrayList<String>();
	ArrayList<String> MDT = new ArrayList<String>();
	ArrayList<String> MNT = new ArrayList<String>();
	ArrayList<String> expandedCode = new ArrayList<String>();

	void processMacro(ArrayList<String> code, String codeCopy) throws Exception {
		ArrayList<String> outCode = new ArrayList<String>();
		ArrayList<String> macroDef = new ArrayList<String>();
		String macroName;
		this.mainFlag = 1;

		outCode.addAll(code);	
		macroName = getMacroName(code);
		this.MNT.add(macroName);

		setAla(macroName, code);
		macroDef.addAll(getMacroDef(code));
		for(int i = 0; i < this.nCalls; i++) {
			setMDT(macroDef,this.nArgs);
		}

		expandCode(macroName, code);
		printOutput(macroName, outCode);
	}
	
	ArrayList<String> finalHash(ArrayList<String> expandedCode)	{
		ArrayList<String> exp = new ArrayList<String>();
		Iterator<String> itr = expandedCode.iterator();

		while(itr.hasNext()) {
			String vv = itr.next();
			String vv_array[] = vv.split("\n");

			for (String v: vv_array) {
				if (v.contains("#")) {
					int m = 0;
					String intermediate_val = "";
					while(v.charAt(m) != '#') {
						intermediate_val += (Character.toString(v.charAt(m)));
						m += 1;
					}
					String ala_content = this.ALA.get(
						Integer.parseInt((Character.toString(v.charAt(v.length() - 1)))) - 1
					);

					ala_content = ala_content.replace("b", "");
					intermediate_val = intermediate_val + ala_content;
					exp.add(intermediate_val);
				} else {
					if(!exp.contains(v))
						exp.add(v);
				}

			}
		}
		return(exp);
	}

	void expandCode(String macroName, ArrayList<String> originalCode) {
		ArrayList<String> temp = new ArrayList<String>();
		ArrayList<String> hashedExpCode = new ArrayList<String>();

		this.expandedCode.clear();
		temp.addAll(getMacroDef(originalCode));
		Iterator<String> tempItr = temp.iterator();
		while(tempItr.hasNext()) {
			String itrNext = tempItr.next();
			originalCode.remove(itrNext);
		}

		Iterator<String> itr = originalCode.iterator();
		while(itr.hasNext()) {
			String statement = itr.next();
			if(statement.contains(macroName)) {
				Iterator<String> mdtIter = this.MDT.iterator();
				mdtIter.next();
				while (mdtIter.hasNext()) {
					this.expandedCode.add(mdtIter.next().replace("MEND",""));
				}
			} else {
				this.expandedCode.add(statement.replace("MEND",""));
			}
		}

		hashedExpCode.addAll(finalHash(this.expandedCode));
		temp.addAll(hashedExpCode);
		this.expandedCode.clear();
		this.expandedCode.addAll(temp);
		temp.clear();
	}

	ArrayList<String> getMacroDef(ArrayList<String> code) {
		ArrayList<String> macro = new ArrayList<String>();
		if(code.get(0).equals("MACRO")) {
			Iterator<String> itr = code.iterator();
			String end="";
			while(!end.equals("MEND")) {
				end = itr.next();
				macro.add(end);
			}
		}
		return(macro);
	}

	String extendTo(String str, int extend) {
		for(int i = str.length() - 1; i < extend - 1; i++) {
			str += "b";
		}
		return(str);
	}

	ArrayList<String> getArgumentNames(String itr_val) {
		ArrayList<String> aug_val = new ArrayList<String>();
		String removeMacroName = "";
		int j = 0, flag = 0;

		while (j<itr_val.length()) {
			if (itr_val.charAt(j) == ' ') {
				j++;
				flag = 1;
			}

			if (flag == 1) {
				removeMacroName += (Character.toString(itr_val.charAt(j)));
			}
			j++;
		}
		String aug_array[] = removeMacroName.split(",");
		for(String i: aug_array) {
			this.ALA_COPY.add(i);
			aug_val.add(extendTo(i,8));
		}
		return(aug_val);
	}

	void setAla(String macroName, ArrayList<String> code) {
		ArrayList<String> codeCopy = new ArrayList<>();
		Iterator<String> itr = code.iterator();
		int flag = 0;

		while (itr.hasNext()) {
			String val = itr.next();
			if (val.equals("MEND")) {
				flag = 1;
				val = itr.next();
			}
			if (flag == 1) {
				codeCopy.add(val);
			}
		}
		codeCopy.remove("");
		itr = codeCopy.iterator();
		while(itr.hasNext()) {
			String itr_val = itr.next();
			if (itr_val.contains(macroName)) {
				this.nCalls += 1;
				this.ALA.addAll(getArgumentNames(itr_val));
				if (this.mainFlag == 1) {
					this.mainFlag = 0;
					this.nArgs = this.ALA.size();
				}
			}
		}
	}

	String getMacroName(ArrayList<String> code) {
		Iterator<String> itr = code.iterator();
		while (itr.hasNext()) {
			String statement = itr.next();
			if(statement.trim().equals("MACRO")) {
				String next_stmt = itr.next();
				String name_array[] = next_stmt.split(" ");
				for(String i: name_array) {
					if(!i.contains("&"))
						return i;
				}
			}
		}
		return null;
	}

	String getALAValue(String argument) {
		alaHash++;
		return("#" + Integer.toString(alaHash));
	}

	void setMDT(ArrayList<String> macroDef, int nArgs) {
		Iterator<String> itr = macroDef.iterator();
		itr.next();
		// System.out.println(itr.next());
		this.MDT.add(itr.next() + '\n');
		String macro_hashed = "";
		while(itr.hasNext()) {
			String val = itr.next();
			macro_hashed = macro_hashed + val + "\n";
			if(val.contains("&")) {
				String opr_array[] = val.split(",");
				String argument = opr_array[1];
				macro_hashed = macro_hashed.replace(argument, getALAValue(argument));
			}
			this.MDT.add(macro_hashed);
			macro_hashed = "";
		}
	}

	void printOutput(String macroName, ArrayList<String> args) throws Exception {
		System.out.println("Macro Definition Table (MDT):");
		System.out.println("Statement\t\t\tIndex");
		int mdtc = 0;
		Iterator<String> itrMDT = this.MDT.iterator();
		while (itrMDT.hasNext()) {
			String stm = itrMDT.next();
			for (String s: stm.split(System.lineSeparator())) {	
				System.out.println(s.substring(0, s.length() - 1) + "\t\t\t\t  #" + mdtc);
				mdtc++;	
			}	
		}

		System.out.println("\nMacro Name Table (MNT):");
		System.out.println("Name\t\t\tMDT Index");
		Iterator<String> itrMNT = this.MNT.iterator();
		int temp_index = 0;
		while (itrMNT.hasNext()) {
			String stm = itrMNT.next();
			itrMDT = this.MDT.iterator();
			while (itrMDT.hasNext()) {
				if (itrMDT.next().contains(stm)) {
					break;
				}
				temp_index++;
			}
			System.out.println(stm + "\t\t\t" + temp_index);
		}

		System.out.println("\nArgument List Array (ALA):");
		System.out.println("Arguments\t\t\tIndex");
		Iterator<String> itrALA = this.ALA.iterator();
		int alaCount = 0;
		while (itrALA.hasNext()) {
			System.out.println(itrALA.next() + "\t\t\t  #" + alaCount);
			alaCount++;
		}

		System.out.println("\nMacro Expanded Code:");
		BufferedWriter bw = new BufferedWriter(new FileWriter("macro_output.txt"));
		Iterator<String> itr_expanded = this.expandedCode.iterator();
		while (itr_expanded.hasNext()) {
			String val = itr_expanded.next();
			bw.write(val + '\n');
			System.out.println(val);
		}
		bw.close();
	}
}

