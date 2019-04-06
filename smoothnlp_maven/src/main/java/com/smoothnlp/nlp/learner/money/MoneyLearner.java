package com.smoothnlp.nlp.learner.money;
import com.smoothnlp.nlp.SmoothNLP;
import com.smoothnlp.nlp.learner.BaseLearner;
import com.smoothnlp.nlp.simple.NormalizedNER;
import com.smoothnlp.nlp.simple.DepParsingAnalyzer;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import com.google.gson.Gson;

import com.hankcs.hanlp.seg.Viterbi.ViterbiSegment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.HashMap;
import java.util.ArrayList;

public class MoneyLearner extends BaseLearner {

    public static InputStream CHINESE_DEFAULT_PROPS_INPUTSTREAM =  NormalizedNER.class.getClass().getResourceAsStream("/StanfordCoreNLP-chinese.properties");

    public Properties props;
    public StanfordCoreNLP pipeline;

    protected NormalizedNER normalizedNER;
    protected DepParsingAnalyzer depParsingAnalyzer;

    private boolean tokenizeBySpace;

    public MoneyLearner(){
        super();
        init(false);
    }

    public MoneyLearner(boolean tokenizeBySpace){
        super();
        init(tokenizeBySpace);
    }

    protected void init(boolean tokenizeBySpace){
        this.props = new Properties();
        try {
            props.load(CHINESE_DEFAULT_PROPS_INPUTSTREAM);
        } catch (IOException e) {
            e.printStackTrace();
        }
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
        this.tokenizeBySpace = tokenizeBySpace;
        if (this.tokenizeBySpace==true){
            props.setProperty("tokenize.whitespace","true");
        }
        this.pipeline = new StanfordCoreNLP(props);
        this.normalizedNER = new NormalizedNER(this.tokenizeBySpace,this.pipeline);
        this.depParsingAnalyzer = new DepParsingAnalyzer();
    }

    public ArrayList<HashMap<String,String>> getMoneyNER(String inputText){
        ArrayList<HashMap<String,String>> nerResList = this.normalizedNER.getNormalizedNER(inputText);
        ArrayList<HashMap<String,String>> moneyResList = new ArrayList<HashMap<String,String>>();
        for (HashMap<String,String> nerRes : nerResList){
            if (nerRes.get("entityType")=="MONEY"){
                moneyResList.add(nerRes);
            }
        }
        return moneyResList;
    }

    public ArrayList<HashMap<String,String>> getMoneyDPRelations(String inputText){
        ArrayList<HashMap<String,String>> dpRelations = this.depParsingAnalyzer.hanlpNeuralDParse(inputText);
        ArrayList<HashMap<String,String>> moneyNerResList = getMoneyNER(inputText);

        ArrayList<HashMap<String,String>> finalMoneyNerDP = new ArrayList<HashMap<String,String>>();

        for (HashMap<String,String> moneyNerRes: moneyNerResList){
            int moneyCharStart = Integer.valueOf(moneyNerRes.get("charStart"));
            int moneyCharEnd = Integer.valueOf(moneyNerRes.get("charEnd"));

            ArrayList<HashMap<String,String>> moneyNerDpRelations = DepParsingAnalyzer.getDependenciesByCharRange(dpRelations,moneyCharStart,moneyCharEnd,true);
            if (moneyNerDpRelations.size()>1){
                System.out.println("More than 1 relation");
                Gson gson = new Gson();
                System.out.println(gson.toJson(moneyNerDpRelations));

            }else if(moneyNerDpRelations.size()==1){;
                HashMap<String,String> moneyDPrelation = moneyNerDpRelations.get(0);

                moneyNerRes.putAll(moneyDPrelation);

//                moneyNerRes.put("relationship",moneyDPrelation.get("relationship"));
//                moneyNerRes.put("sourceToken",moneyDPrelation.get("sourceToken"));

            }
            finalMoneyNerDP.add(moneyNerRes);
        }
        return finalMoneyNerDP;
    }

    @Override
    public String transform(String inputText){
        ArrayList<HashMap<String,String>> moneyResList = this.getMoneyDPRelations(inputText);
        Gson gsonobject = new Gson();
        return gsonobject.toJson(moneyResList);
    }

    public static void main(String[] args){
        MoneyLearner ml = new MoneyLearner(false);
//        String sampleText = "我买了五斤苹果, 总共10元钱";
//        System.out.println(ml.getMoneyDPRelations(sampleText).get(0).size());
//        ml.transform("我买了五斤苹果, 总共10元钱");

        System.out.println(ml.transform("我买了五斤苹果, 总共10元; 之后我又买了50元的梨子"));
    }

}
