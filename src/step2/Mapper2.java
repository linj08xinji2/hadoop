package step2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * 矩阵相乘
 * @author linj
 *
 */
public class Mapper2 extends Mapper<LongWritable, Text, Text, Text> {

	private Text outKey = new Text();
	private Text outValue = new Text();

	private List<String> cacheList = new ArrayList<>();
	
	private DecimalFormat df=new DecimalFormat("0.00");

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		// 通过输入流将全局缓存中的右侧矩阵 读入List<String>中
		FileReader fr = new FileReader("content2");
		BufferedReader br = new BufferedReader(fr);
		// 每一行的格式是： 行 tab 列_值,列_值,列_值,列_值
		String line = null;
		while ((line = br.readLine()) != null) {
			cacheList.add(line);
		}
		fr.close();
	}

	@Override
	protected void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		// 行
		String row_matrix1 = value.toString().split("\t")[0];
		// 列_值（数组）
		String[] column_value_array_matrix1 = value.toString().split("\t")[1]
				.split(",");

		for (String line : cacheList) {
			// 右侧矩阵的行 line
			// 格式：行 tab 列_值，列_值，列_值，列_值
			String row_matrix2 = line.toString().split("\t")[0];
			String[] column_value_array_matrix2 = line.toString().split("\t")[1]
					.split(",");
			// 矩阵两行相乘得到的结果
			double result = 0;
			// 遍历左矩阵的第一行的每一列
			for (String column_value_matrix1 : column_value_array_matrix1) {
				String column_matrix1 = column_value_matrix1.split("_")[0];
				String value_matrix1 = column_value_matrix1.split("_")[1];
				// 遍历右矩阵的每一行的每一列
				for (String column_value_matrix2 : column_value_array_matrix2) {
					if (column_value_matrix2.startsWith(column_matrix1 + "_")) {
						String value_matrix2 = column_value_matrix2.split("_")[1];
						// 将两列的值相乘，并累加
						result += Double.valueOf(value_matrix1) *Double.valueOf(value_matrix2);
					}
				}
			}
			/**如果没有过虑空值，会出现为C_0.00的情况
			 * 1	A_9.87,B_2.38,C_23.90
2	A_16.59,B_8.27,C_4.25
3	C_23.95,B_0.00,A_4.44
4	C_22.85,B_3.27,A_11.68
5	A_6.45,B_7.42,C_0.00
6	B_9.77,C_3.10,A_15.40
			 */
			if(result==0){
				continue;
			}
			// result 是结果矩阵中的某元素，坐标为 行：row_matrix1 
			// 列：row_matrix2(因为右矩阵已经转置)
			outKey.set(row_matrix1);
			outValue.set(row_matrix2+"_"+df.format(result));
         context.write(outKey, outValue); 
		}
	}
}
