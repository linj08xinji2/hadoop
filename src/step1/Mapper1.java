package step1;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 *转置
 * @author linj
 *
 */
public class Mapper1 extends Mapper<LongWritable, Text, Text, Text> {
	
	private Text outKey = new Text();
	private Text outValue = new Text();

	/**
	 * key :1 value: 1 1_0,2_3,3_-1,4_2,5_-3
	 * 
	 */
	@Override
	protected void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		/**
		 * 列转行
            mapper是一行一行地处理，顺序不变
             reduce是一列一列地处理，顺序有可以变化
		 */
		String[] rowAndLine = value.toString().split("\t");
		// 矩阵的行号
		String row = rowAndLine[0];
		String[] lines = rowAndLine[1].split(",");
		// ["1_0","2_3","3_-1","4_2","5_-3"]
		for (int i = 0; i < lines.length; i++) {
			String column = lines[i].split("_")[0];
			String valueStr = lines[i].split("_")[1];
			// key :列号      value：行号_值
			outKey.set(column);
			outValue.set(row + "_" + valueStr);
			context.write(outKey, outValue);
		}
	}
}
