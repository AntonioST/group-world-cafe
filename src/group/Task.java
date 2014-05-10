/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package group;

import java.io.*;

/**
 *
 * @author antonio
 */
public class Task implements Serializable{

    private static final long serialVersionUID = 201405201657L;
    //
    public int count = 0;
    public int group = 0;
    public int round = 0;
    public int less = 0;
    public int limit = 0;
    public int meet = 2;
    //
    Node answer = null;

    public Task(){
    }

    @Override
    public String toString(){
        return String.format("Task[count= %d, group= %d, round= %d, range= %d~%d, meet<= %d]",
                             count, group, round, less, limit, meet);
    }

    public void setDefault(){
        round = group;
        if (group != 0) {
            float d = (float)count / group;
            less = (int)d;
            limit = (less == d) ? less : less + 1;
        } else {
            less = 0;
            limit = 0;
        }
        meet = 2;
    }

    public void checkInit() throws IllegalStateException{
        double div = (double)count / group;
        int ilimit = (int)Math.ceil(div);
        int iless = (int)Math.floor(div);
        if (count <= 0) throw new IllegalStateException("wrong count : " + count);
        if (group <= 0) throw new IllegalStateException("wrong group : " + group);
        if (round <= 0 || round > group) throw new IllegalStateException("wrong round : " + round);
        if (limit < ilimit) throw new IllegalStateException("wrong limit : " + limit);
        if (less < 0 || less > iless) throw new IllegalStateException("wrong less : " + less);
        if (meet <= 0 || meet > group) throw new IllegalStateException("wrong meet : " + meet);
    }

    public int[][] getAnswer(){
        if (answer == null) {
            return null;
        }
        int[][] ret = new int[group][count];
        Node curt = answer;
        try{
            for (int i = 0; i < group; i++) {
                System.arraycopy(curt.array, 0, ret[i], 0, count);
                curt = curt.prev;
            }
        } catch (RuntimeException ex){
            ex.printStackTrace();
            ret = new int[0][0];
        }
        return ret;
    }
}
