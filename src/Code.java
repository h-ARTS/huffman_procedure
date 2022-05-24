/*
* Code class is a representation of a Node which
* is used to create a Huffman Tree for the Huffman code.
* This class needs the implementation of Comparable interface
* in order to compare between frequencies. This is being
* used for the so-called priority queue and to
* summarize the codes until we have only one Code left
* which will represent a complete Huffman tree.
* Afterwards, it will be used to iterate over the codes (node)
* and assign the codewords with ascii value
* into a string representation.
* */
public class Code implements Comparable<Code> {
    private char ascii;
    private int frequency;
    private Code left, right;

    private short binaryLeft, binaryRight;

    public Code(char ascii, int frequency) {
        this.ascii = ascii;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }

    public char getAscii() {
        return ascii;
    }

    public void setAscii(char ascii) {
        this.ascii = ascii;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public Code getLeft() {
        return left;
    }

    public void setLeft(Code left) {
        this.left = left;
        this.binaryLeft = 0;
    }

    public Code getRight() {
        return right;
    }

    public void setRight(Code right) {
        this.right = right;
        this.binaryRight = 1;
    }

    public short getBinaryLeft() {
        return binaryLeft;
    }

    public void setBinaryLeft(short binaryLeft) {
        this.binaryLeft = binaryLeft;
    }

    public short getBinaryRight() {
        return binaryRight;
    }

    public void setBinaryRight(short binaryRight) {
        this.binaryRight = binaryRight;
    }

    @Override
    public int compareTo(Code otherCode) {
        return frequency - otherCode.frequency;
    }
}
