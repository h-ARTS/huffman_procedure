import java.io.*;
import java.util.*;

public class Huffman {

    private static Code root;

    private static StringBuilder currentCodeWord = new StringBuilder();
    private static Map<Character, String> codeWordTable = new HashMap<>();

    public static void main(String[] args) throws IOException {
        createHuffmanTree();

        buildCodeWords(root);

        codeWordTable.forEach((character, s) -> {
            System.out.println(character + " " + s);
        });
    }

    /*
    * Create frequency table from the list of character ascii codes.
    * */
    private static Map<Character, Integer> createFrequencyTable() throws IOException {
        List<Character> textFile = getListOfCharCodesFromFile("src/text.txt");
        HashMap<Character, Integer> frequencyTable = new HashMap<>();
        textFile.forEach(character -> {
            // Increase frequency if character exists.
            if (frequencyTable.containsKey(character)) {
                frequencyTable.put(character, frequencyTable.get(character) + 1);
            } else {
                frequencyTable.put(character, 1);
            }
        });

        return frequencyTable;
    }

    private static void createHuffmanTree() throws IOException {
        Map<Character, Integer> frequencyTable = createFrequencyTable();
        /*
        * This queue data structure is optimal for adding objects with comparable implementation
        * Picking one code will result to remove it from queue and add the parent back to
        * the queue until we get the size of 1.
        * */
        PriorityQueue<Code> huffmanPrioQueue = new PriorityQueue<>();
        Set<Character> characters = frequencyTable.keySet();

        characters.forEach(character -> {
            Code code = new Code(character, frequencyTable.get(character));
            huffmanPrioQueue.add(code);
        });

        while (1 < huffmanPrioQueue.size()) {
            Code leftHand = huffmanPrioQueue.poll();
            Code rightHand = huffmanPrioQueue.poll();
            // The parent code with the hyphen as ascii will be ignored,
            // and it serves as a placeholder.
            Code parent = new Code('-', leftHand.getFrequency()+ rightHand.getFrequency());

            parent.setLeft(leftHand);
            parent.setRight(rightHand);

            /*
            * Add it to the root and re-add it to the queue.
            * */
            setRoot(parent);
            huffmanPrioQueue.add(parent);
        }

        System.out.println("finish");
    }

    private static void buildCodeWords(Code pq) {
        buildCodeWords(pq, null, "");
    }

    private static void buildCodeWords(Code pq, Code parent, String position) {
        if (parent != null) {
            pq.setParent(parent);
        }

        if (pq.getLeft() != null) {
            currentCodeWord.append(pq.getBinaryLeft());
            buildCodeWords(pq.getLeft(), pq, "left");
        } else if (pq.getRight() != null) {
            currentCodeWord.append(pq.getBinaryRight());
            buildCodeWords(pq.getRight(), pq, "right");
        } else {
            codeWordTable.put(pq.getAscii(), currentCodeWord.toString());
            currentCodeWord = new StringBuilder();
            if (position.equals("right")) {
                parent.setRight(null);
            } else if (position.equals("left")) {
                parent.setLeft(null);
            }

            if (parent != null) {
                if (parent.getLeft() == null && parent.getRight() == null) {
                    parent.getParent().setRight(null);
                    parent.getParent().setLeft(null);
                }
                buildCodeWords(getRoot());
            }

        }
    }

    private static void readBinaryFile() throws IOException {
        File file = new File("src/output-mada.dat");
        byte[] bFile = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        StringBuilder sb = new StringBuilder();
        fis.read(bFile);
        for (int b : bFile) {
            String str = String.format("%8s", Integer.toBinaryString(b)).replace(' ', '0');
            sb.append(str);
        }
        System.out.println(sb.toString());
        System.out.println(sb.toString().length());
        fis.close();
    }

    /*
     * List of ascii character codes method from assignment 1
     * */
    private static List<Character> getListOfCharCodesFromFile(String pathname) throws IOException {
        // Load a file
        FileReader file = new FileReader(pathname);
        BufferedReader reader = new BufferedReader(file);
        String plainText = reader.readLine();
        reader.close();

        // convert every single character string representation into character type
        List<String> charsString = Arrays.asList(plainText.split("", 0));

        return charsString.stream().map(c -> c.charAt(0)).toList();
    }

    private static void saveTextInFile(String rawText, String filename) throws IOException {
        FileWriter cipherTextFile = new FileWriter(filename);
        BufferedWriter cipherWriter = new BufferedWriter(cipherTextFile);

        cipherWriter.write(rawText);

        cipherWriter.close();
    }

    private static String loadTextFile(String pathname) throws IOException {
        FileReader textFile = new FileReader(pathname);
        BufferedReader reader = new BufferedReader(textFile);

        return reader.readLine();
    }

    /* Getters and Setters */

    public static Code getRoot() {
        return root;
    }

    public static void setRoot(Code root) {
        Huffman.root = root;
    }
}