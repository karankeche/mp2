import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MacroProcessor {
    private static HashMap<String, Integer> mnt = new HashMap<>();  // Macro Name Table
    private static ArrayList<String> mdt = new ArrayList<>();       // Macro Definition Table
    private static ArrayList<String> intermediateCode = new ArrayList<>(); // Intermediate Code
    private static String fileContent;

    public static void main(String[] args) throws IOException {
        fileContent = getContentFrom("/Users/karankeche/Desktop/Macro/mp1/2.txt");  // Replace with your file path
        new Pass1().start();
        System.out.println("\nMNT: " + mnt);
        System.out.println("\nMDT: " + mdt);
        System.out.println("\nIntermediate Code (after Pass 1): " + intermediateCode);
        new Pass2().start();
    }

    private static String getContentFrom(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) content.append(line).append("\n");
        reader.close();
        return content.toString();
    }

    public static void updateMnt(String name, int mdtIndex) { mnt.put(name, mdtIndex); }
    public static void updateMdt(String line) { mdt.add(line); }
    public static void updateIntermediateCode(String line) { intermediateCode.add(line); }

    // Pass 1: Macro Definition Processing
    static class Pass1 {
        public void start() {
            String[] lines = fileContent.split("\\n");
            boolean insideMacro = false;
            String macroName = null;

            for (String line : lines) {
                String[] parts = line.trim().split("\\s+");

                if (parts[0].equals("MACRO")) {
                    insideMacro = true;
                } else if (insideMacro) {
                    if (macroName == null) {
                        macroName = parts[0];  // Get macro name
                        updateMnt(macroName, mdt.size());  // Add macro to MNT
                    } else if (parts[0].equals("MEND")) {
                        insideMacro = false;  // End macro definition
                        macroName = null;
                        updateMdt("MEND");
                    } else {
                        updateMdt(line);  // Store macro body in MDT
                    }
                } else {
                    updateIntermediateCode(line);  // Store non-macro lines in intermediate code
                }
            }
        }
    }

    // Pass 2: Macro Expansion
    static class Pass2 {
        public void start() {
            ArrayList<String> expandedCode = new ArrayList<>();
            for (String line : intermediateCode) {
                String[] parts = line.trim().split("\\s+");
                if (mnt.containsKey(parts[0])) {
                    int mdtIndex = mnt.get(parts[0]);
                    String[] args = parts[1].split(",");
                    expandMacro(mdtIndex, args, expandedCode);
                } else {
                    expandedCode.add(line);  // No macro, add the line directly
                }
            }
            System.out.println("\nFinal Expanded Code (after Pass 2): ");
            for (String code : expandedCode) System.out.println(code);
        }

        private void expandMacro(int mdtIndex, String[] args, ArrayList<String> expandedCode) {
            while (!mdt.get(mdtIndex).equals("MEND")) {
                String macroLine = mdt.get(mdtIndex);
                for (int i = 0; i < args.length; i++) {
                    macroLine = macroLine.replace("&ARG" + (i + 1), args[i]);  // Replace params with arguments
                }
                expandedCode.add(macroLine);
                mdtIndex++;
            }
        }
    }
}