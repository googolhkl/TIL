package com.hkl.common;
import org.apache.hadoop.io.Text;

/**
 * Created by hkl on 16. 8. 19.
 */
public class CountParser
{
    private String[] colums;
    private int count;
    private String str = "";

    public CountParser(Text str)
    {
        colums = str.toString().split("\t");

        for(int i=0; i<colums.length-1; i++)
        {
            this.str = this.str.concat(colums[i] + " ");
        }

        count = Integer.parseInt(colums[colums.length-1]);
    }

    public int getCount() { return count; }
    public String getStr() { return str; }

    public void setColums(int count) { this.count = count; }
    public void setStr(String str) { this.str = str; }
}
