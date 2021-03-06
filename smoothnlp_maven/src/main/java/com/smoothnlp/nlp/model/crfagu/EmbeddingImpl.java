package com.smoothnlp.nlp.model.crfagu;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;

public class EmbeddingImpl {

    private HashMap<String,float[]> embeddingVector;  // string , float[]
    private String splitRegex = "[\t| ]";
    private int vsize;  // embedding size

    public EmbeddingImpl(){
        embeddingVector = new HashMap<String, float[]>();
    }
    public EmbeddingImpl(String inputFile){
        this();
        loadEmbedding(inputFile);
    }
    public EmbeddingImpl(String inputFile,String splitRegex){
        this();
        setSplitRegex(splitRegex);
        loadEmbedding(inputFile);
    }

    public void setSplitRegex(String splitRegex){
        this.splitRegex = splitRegex;
    }
    public int getVsize(){
        return vsize;
    }

    public void loadEmbedding(String inputFile){
        try{
            InputStreamReader ifs = new InputStreamReader(new FileInputStream(inputFile));
            BufferedReader br = new BufferedReader(ifs);
            loadEmbedding(br);
            br.close();
        }catch (Exception e ){
            e.printStackTrace();
            System.err.println("br error");
        }
    }

    public void loadEmbedding(BufferedReader br){
        try{
            String line;
            line = br.readLine();
            String [] cols = line.split(splitRegex);
            int xsize = Integer.parseInt(cols[cols.length -1]);
            vsize = xsize;
            while(true){
                line = br.readLine();
                if(line!=null){
                    add(line);
                }else{
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("Error reading stream");
        }
    }

    public boolean add(String line){
        String [] cols = line.split(splitRegex);
        if (vsize != cols.length -1){
            System.err.println("# x size=" + cols.length+" and xsize=" + vsize + ",The line is " + line);
            return false;
        }
        String key = cols[0];
        if(embeddingVector.containsKey(key)){
            System.err.println(key + " is repeat");
            return false;
        }
        float [] embedding = new float[vsize];
        for (int i =0; i <vsize; i++){
            embedding[i] = Float.parseFloat(cols[i+1]);
        }
        embeddingVector.put(key,embedding);
        return true;
    }

    /**
     * 重要，根据输入的str, 返回对应embedding vector; 如果无此str,则返回一个固定size(dim size) 的vector;
     * @param key
     * @return
     */
    public float[] getStrEmbedding(String key){
        if(embeddingVector.containsKey(key)){
            return embeddingVector.get(key);
        }else{
            float[] vector = new float[vsize];
            for(int i = 0; i<vsize;i++){
                vector[i]=0;
            }
            return vector;
        }
    }

    public Set<String> getEmbeddingKeySet(){
        return embeddingVector.keySet();
    }

    public HashMap<String, float[]> getEmbeddingVector(){
        return embeddingVector;
    }

    public void setEmbeddingVector(HashMap<String, float[]> embeddingVector){
        this.embeddingVector = embeddingVector;
    }
    public void setVsize(int vsize){
        this.vsize = vsize;
    }

    public static void main(String[]args){
        String file = "embedding.txt";
        EmbeddingImpl embeddingImpl = new EmbeddingImpl(file);
        System.out.println(embeddingImpl.getVsize());

        for(String key:embeddingImpl.embeddingVector.keySet()){
            System.out.println(key);
            float[] value = embeddingImpl.getStrEmbedding(key);
            float sum = 0;
            for(int i=0;i<value.length;i++)
            {
                System.out.print(value[i]+" ");
                sum+=value[i];
            }
            System.out.println("");
            System.out.println("-----");
            System.out.println(sum);
        }
    }

}
