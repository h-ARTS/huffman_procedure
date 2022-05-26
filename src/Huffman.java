import java.io.*;
import java.util.*;
public class Huffman {

    private static Code root;

    private static StringBuilder stringBuilder = new StringBuilder();

    private static final Map<Character, String> codeWordTable = new HashMap<>();

    private static List<Character> textFile;

    public static void main(String[] args) throws IOException {
//        compressFile("src/text.txt");

        decompress("src/output-mada.dat", "src/dec_tab-mada.txt");
    }

    public static void compressFile(String pathname) throws IOException {
        createHuffmanTree(pathname);

        buildCodeWords(root);
        System.out.println("Codewords created.");

        saveHuffmanCodeInFile();

        storeBytes(bitStringHuffmanCode());
    }

    public static void decompress(String pathnameBinary, String huffmanTablePathname) throws IOException {
        String bits = readBinaryFile(pathnameBinary);

        String decompressedText = convertBitsToPlainText(bits, huffmanTablePathname);

        System.out.println(decompressedText);
        saveTextInFile(decompressedText, "output/decompress.txt");
    }

    /*
    * Create frequency table from the list of character ascii codes.
    * */
    private static Map<Character, Integer> createFrequencyTable(String pathname) throws IOException {
        textFile = getListOfCharCodesFromFile(pathname);
        HashMap<Character, Integer> frequencyTable = new HashMap<>();
        textFile.forEach(character -> {
            // Increase frequency if character exists.
            if (frequencyTable.containsKey(character)) {
                frequencyTable.put(character, frequencyTable.get(character) + 1);
            } else {
                frequencyTable.put(character, 1);
            }
        });

        System.out.println("Frequency table created.");

        return frequencyTable;
    }

    /*
     * For creating a huffman tree, we used a priority queue data structure
     * as it is optimal for adding objects with comparable implementation.
     * Picking one code will result to remove it from queue and add the parent back to
     * the queue until we get the size of 1.
     * */
    private static void createHuffmanTree(String pathname) throws IOException {
        Map<Character, Integer> frequencyTable = createFrequencyTable(pathname);
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
            Code parent = new Code( leftHand.getFrequency()+ rightHand.getFrequency());

            parent.setLeft(leftHand);
            parent.setRight(rightHand);

            /*
            * Add it to the root and re-add it to the queue.
            * */
            setRoot(parent);
            huffmanPrioQueue.add(parent);
        }

        System.out.println("Huffman tree finished.");
    }

    private static void buildCodeWords(Code pq) {
        buildCodeWords(pq, null, "");
    }
    /*
    * Build codewords recursively and insert it into hashmap.
    * The overloaded method is used for the initial call and
    * when no left-hand and right-hand exists in a Code i.e. no children.
    * */
    private static void buildCodeWords(Code pq, Code parent, String position) {
        if (root.equals(pq) && pq.getLeft() == null && pq.getRight() == null) {
            return;
        }
        if (parent != null) {
            pq.setParent(parent);
        }

        if (pq.getLeft() != null) {
            stringBuilder.append(pq.getBinaryLeft());
            buildCodeWords(pq.getLeft(), pq, "left");
        }
        else if (pq.getRight() != null) {
            stringBuilder.append(pq.getBinaryRight());
            buildCodeWords(pq.getRight(), pq, "right");
        }
        else {
            if (pq.getAscii() != '\u0000') {
                codeWordTable.put(pq.getAscii(), stringBuilder.toString());
            }
            stringBuilder = new StringBuilder();
            if (position.equals("right")) {
                parent.setRight(null);
            } else if (position.equals("left")) {
                parent.setLeft(null);
            }

            if (parent != null) {
                if (parent.getLeft() == null && parent.getRight() == null) {
                    if (parent.getParent() != null) {
                        buildCodeWords(getRoot());
                    } else {
                        return;
                    }
                    parent.getParent().setRight(null);
                    parent.getParent().setLeft(null);
                }
                buildCodeWords(getRoot());
            }
        }
    }

    private static void saveHuffmanCodeInFile() throws IOException {
        stringBuilder = new StringBuilder();
        codeWordTable.forEach((character, s) -> {
            stringBuilder.append((int) character).append(":").append(s).append("-");
        });
        String huffmanCode = stringBuilder.deleteCharAt(stringBuilder.length()-1).toString();

        System.out.println("Huffman code saved.");

        saveTextInFile(huffmanCode, "output/dec_tab.txt");
    }

    /*
    * Bit-string creation and correction of bit length
    * by ensuring the length is always a multiple of 8.
    * */
    private static String bitStringHuffmanCode() {
        StringBuilder strBuilder = new StringBuilder();
        textFile.forEach(character -> {
            strBuilder.append(codeWordTable.get(character));
        });

        boolean oneAdded = false;
        while (strBuilder.length() % 8 != 0) {
            if (!oneAdded) {
                strBuilder.append(1);
                oneAdded = true;
            } else {
                strBuilder.append(0);
            }
        }

        System.out.println("Bit string created.");

        return strBuilder.toString();
    }

    /*
    * Here we use the bit-string from bitStringHuffmanCode method
    * and divide it by 8. Every bit substring we convert to integer
    * and subsequently convert into a byte for the byte array and save
    * it in the output.dat file.
    * */
    private static void storeBytes(String bitString) throws IOException {
        List<Byte> results = new ArrayList<>();
        final int limit = 8;

        for (int i = 0; i < bitString.length(); i += limit) {
            int bitIntValue = Integer.valueOf(bitString.substring(i, Math.min(bitString.length(), i + limit)), 2);
            results.add((byte)bitIntValue);
        }

        byte[] out = new byte[results.size()];
        results.forEach(aByte -> out[results.indexOf(aByte)] = aByte);

        System.out.println("Bytes saved.");

        FileOutputStream fos = new FileOutputStream("output/output.dat");
        fos.write(out);
        fos.close();
    }

    /*
    * Read binary file in (.dat) and converts it into a bit-string.
    * To get the 8bits we convert the signed byte to an unsigned integer
    * by adding 0xFF to it.
    * Additionally, the extended bits are removed at the end.
    * */
    private static String readBinaryFile(String pathname) throws IOException {
        File file = new File(pathname);
        byte[] bFile = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        StringBuilder sb = new StringBuilder();
        fis.read(bFile);
        for (int b : bFile) {
            String str = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            sb.append(str);
        }

        fis.close();

        return removeExtendedBits(sb);
    }

    private static String removeExtendedBits(StringBuilder sb) {
        int lastIndexOne = sb.lastIndexOf("1");
        return sb.substring(0, lastIndexOne);
    }

    /*
    * The encoding process of the bit-string by reading through the bits,
    * add it to a variable and remove it from the bit-string.
    * */
    private static String convertBitsToPlainText(String bitString, String pathname) throws IOException {
        String huffmanCodes = loadTextFile(pathname);
        StringBuilder plainText = new StringBuilder();
        Map<String, Integer> huffmanCodeTable = new HashMap<>();

        Arrays.stream(huffmanCodes.split("-")).toList().forEach(code -> {
            String[] asciiBits = code.split(":");
            huffmanCodeTable.put(asciiBits[1], Integer.parseInt(asciiBits[0]));
        });

        List<String> bitKeys = new ArrayList<>();
        String[] bits = bitString.split("");
        StringBuilder current = new StringBuilder();
        int lengthBits = bits.length;
        for (String bit: bits) {
            current.append(bit);
            bitKeys = huffmanCodeTable.keySet().stream()
                    .filter(b -> b.contains(current.toString())).toList();

            if (bitKeys.size() == 1 || bitKeys.contains(current.toString())) {
                int characterInt = huffmanCodeTable.get(current.toString());
                plainText.append((char)characterInt);
                bitString = bitString.replaceFirst(current.toString(), "");
                current.delete(0, current.length());
            }
        }

        System.out.println("Bits converted to plaintext.");

        return plainText.toString();
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

    private static void saveTextInFile(String rawText, String pathname) throws IOException {
        FileWriter textFile = new FileWriter(pathname);
        BufferedWriter writer = new BufferedWriter(textFile);

        writer.write(rawText);

        writer.close();
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