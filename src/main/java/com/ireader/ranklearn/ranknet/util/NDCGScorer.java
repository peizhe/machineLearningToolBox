package com.ireader.ranklearn.ranknet.util;

import com.ireader.ranklearn.ranknet.dto.RankList;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by zxsted on 15-9-29.
 */
public class NDCGScorer extends MetricScorer {

    public Hashtable<String,Double> idealGains = new Hashtable<String, Double>();

    public NDCGScorer(){
        this.k = 10;
    }

    public NDCGScorer(int k)
    {
        this.k = k;
    }

    public MetricScorer clone()
    {
        return new NDCGScorer();
    }

    public void loadExternalRelevanceJudgment(String qrelFile) throws Exception {
        idealGains = new Hashtable<String,Double>();
        try{
            String content = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(qrelFile)));
            String lastQID = "";
            List<Integer> rel = new ArrayList<Integer>();
            while((content = in.readLine()) != null){
                content = content.trim();
                if(content.length() == 0)
                    continue;
                String[] s = content.split(" ");
                String qid = s[0].trim();

                int label = Integer.parseInt(s[3].trim());
                if(lastQID.compareTo("")!=0 && lastQID.compareTo(qid)!=0)
                {
                    double ideal = getIdealDCG(rel,k);
                    idealGains.put(lastQID,ideal);
                    rel.clear();
                }
                lastQID = qid;
                rel.add(label);
            }
            if(rel.size() > 0)
            {
                double ideal = getIdealDCG(rel,k);
                idealGains.put(lastQID,ideal);
                rel.clear();
            }
            in.close();
            System.out.println("Relevance judgment file loaded. [#q=" + idealGains.keySet().size() + "]");
        }
        catch(Exception ex)
        {
            System.out.println("Error in NDCGScorer::loadExternalRelevanceJudgment(): " + ex.toString());
        }
    }
    /**
     * Compute NDCG at k. NDCG(k) = DCG(k) / DCG_{perfect}(k). Note that the "perfect ranking" must be computed based on the whole list,
     * not just top-k portion of the list.
     */
    public double score(RankList rl)
    {
        List<Integer> rel = new ArrayList<Integer>();
        for(int i = 0 ; i < rl.size(); i++)
            rel.add((int)rl.get(i).getLabel());
        if(rl.size() == 0)
            return -1.0;

        double d2 = 0;
        if(idealGains != null)
        {
            Double d = idealGains.get(rl.getID());
            if(d != null)
                d2 = d.doubleValue();
        }
        else
            d2 = getIdealDCG(rel,k);
        if(d2 <= 0.0)
            return 0.0;
        return getDCG(rel,k) / d2;
    }

    private double getDCG(List<Integer> rel,int k)
    {
        int size = k;
        if(k > rel.size() || k <= 0)
            size = rel.size();

        double dcg = 0.0;
        for(int i =1; i <= size;i++)
        {
            dcg += (Math.pow(2.0,rel.get(i-1)) - 1.0) / (Math.log(i+1)/Math.log(2));

        }
        return dcg;
    }

    private double getIdealDCG(List<Integer> rel,int k)
    {
        int size = k;
        if(k > rel.size() || k <= 0)
            size = rel.size();

        int[] idx = Sorter.sort(rel,false);   // 根据实际 标记排序
        double dcg = 0.0;
        for(int i = 1; i <= size;i++)
        {
            dcg += (Math.pow(2.0,rel.get(idx[i-1]))-1.0) / (Math.log(i+1)/Math.log(2));

        }
        return dcg;
    }

    public double[][] swapChange(RankList rl)
    {
        int size  = (rl.size() > k)?k:rl.size();
        // compute the ideal ndcg
        List<Integer> rel = new ArrayList<Integer>();
        for(int t = 0; t < rl.size(); t++)
            rel.add((int) rl.get(t).getLabel());

        double d2 = 0;

        if(idealGains != null)
        {
            Double d = idealGains.get(rl.getID());
            if(d != null)
                d2 = d.doubleValue();
        }
        else
            d2 = getIdealDCG(rel,size);
        //double d2 = getIdealDCG(rel, rl.size());//ignore K, compute changes from the entire ranked list

        double[][] changes = new double[rl.size()][];
        for(int i = 0 ; i < rl.size(); i++)
        {
            changes[i] = new double[rl.size()];
            Arrays.fill(changes[i], 0);
        }


        for(int i=0;i<size;i++)
        //for(int i=0;i<rl.size()-1;i++)//ignore K, compute changes from the entire ranked list
        {
            int p1 = i+1;
            for(int j=i+1;j<rl.size();j++)
            {
                if(d2 > 0)
                {
                    int p2 = j + 1;
                    changes[j][i] = changes[i][j] + (1.0/(Math.log(p1+1)/Math.log(2)) - 1.0/(Math.log(p2+1) / Math.log(2))) * (Math.pow(2.0, rel.get(i)) - Math.pow(2.0, rel.get(j))) / d2;
                }
            }
        }
        return changes;

    }


}
