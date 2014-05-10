/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package group;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;

/**
 *
 * @author antonio
 */
class Node implements Serializable{

    private static final long serialVersionUID = 201311241531L;
    //
    static final int EMPTY = -1;
    //
    transient Node prev;
    transient int[] array;
    //

    Node(int count){
        array = new int[count];
        Arrays.fill(array, EMPTY);
    }

    Node(Node node){
        array = new int[node.array.length];
        System.arraycopy(node.array, 0, array, 0, node.array.length);
        if (node.prev != null) {
            prev = new Node(node.prev);
        }
    }

    @Override
    public String toString(){
        return Arrays.toString(array);
    }

    @Override
    public boolean equals(Object obj){
        if (obj == this) return true;
        if (getClass() != obj.getClass()) return false;
        Node n = (Node)obj;
        return Arrays.equals(array, n.array);
    }

    @Override
    public int hashCode(){
        if (array == null) return 0;
        int hash = 11;
        for (int i: array) {
            hash = 31 * hash + i;
        }
        return (int)hash;
    }

    private BigInteger hash(int base){
        BigInteger b = BigInteger.valueOf(base);
        BigInteger ret = b;
        for (int i: array) {
            ret = b.multiply(ret).add(BigInteger.valueOf(i));
        }
        return ret;
    }

    private static boolean dehash(int[] arr, int len, BigInteger hash, int base){
        BigInteger h = hash;
        BigInteger b = BigInteger.valueOf(base);
        if (h.compareTo(BigInteger.ZERO) == 0) {
            Arrays.fill(arr, 0, len, EMPTY);
        } else {
            for (int i = len - 1; i >= 0; i--) {
                arr[i] = h.mod(h).intValue();
                h = h.divide(b);
            }
        }
        return h.compareTo(b) == 0;
    }

    private int getBase(){
        if (array == null) return 2;
        int x = array[0];
        for (int i = 1, sz = array.length; i < sz; i++) {
            if (x < array[i]) {
                x = array[i];
            }
        }
        return x + 1;
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        s.defaultWriteObject();
        if (array != null) {
            s.writeInt(array.length);
            int base = getBase();
            s.writeInt(base);
            s.writeObject(hash(base));
            s.writeObject(prev);
        } else {
            s.writeInt(0);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException{
        s.defaultReadObject();
        int arrlen = s.readInt();
        if (arrlen < 0) {
            throw new StreamCorruptedException("negative arrlen");
        } else if (arrlen == 0) {
            array = null;
        } else {
            int base = s.readInt();
            array = new int[arrlen];
            if (!dehash(array, arrlen, (BigInteger)s.readObject(), base)) {
                throw new StreamCorruptedException("dehash failure");
            }
            prev = (Node)s.readObject();
        }
    }

}
