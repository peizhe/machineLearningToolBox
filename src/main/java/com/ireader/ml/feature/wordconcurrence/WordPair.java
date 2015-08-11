package com.ireader.ml.feature.wordconcurrence;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by zxsted on 15-8-6.
 */
public class WordPair  implements WritableComparable<WordPair>{

    private String wordA;
    private String wordB;

    public WordPair(){

    }

    public WordPair(String wordA,String wordB) {
        this.wordA = wordA;
        this.wordB = wordA;
    }

    public String getWordA(){
        return this.wordA;
    }

    public String getWordB(){
        return this.wordB;
    }

    @Override
    public void write(DataOutput out) throws IOException {

        out.writeUTF(wordA);
        out.writeUTF(wordB);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        wordA = in.readUTF();
        wordB = in.readUTF();
    }

    @Override
    public String toString(){
        return wordA+","+wordB;
    }

    @Override
    public int compareTo(WordPair o) {
        if(this.equals(o))
            return 0;
        else
            return (wordA + wordB).compareTo(o.getWordA() + o.getWordB());
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof WordPair))
            return false;
        WordPair w = (WordPair)o;
        if((this.wordA.equals(w.wordA) &&  this.wordB.equals(w.wordB))
                || (this.wordB.equals(w.wordA) && this.wordA.equals(w.wordB)))
            return true;

        return false;
    }

    @Override
    public int hashCode(){
        return (wordA.hashCode() + wordB.hashCode()) * 17;
    }
}
