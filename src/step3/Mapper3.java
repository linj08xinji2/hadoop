package step3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
// 余弦相似度
public class Mapper3 extends Mapper<LongWritable, Text, Text, Text> {

	private Text outKey = new Text();
	private Text outValue = new Text();

	private List<String> cacheList = new ArrayList<>();

	private DecimalFormat df = new DecimalFormat("0.00");

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		// 通过输入流将全局缓存中的右侧矩阵 读入List<String>中
		FileReader fr = new FileReader("content1");
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
		try {
		// 行
		String row_matrix1 = value.toString().split("\t")[0];
		// 列_值（数组）
		String[] column_value_array_matrix1 = value.toString().split("\t")[1]
				.split(",");
		
		double demonminator1 = 0;
		// 计算左侧矩阵行的空间距离
		for (String column_value : column_value_array_matrix1) {
			String score = column_value.split("_")[1];
			demonminator1 += Double.valueOf(score) * Double.valueOf(score);
		}
		demonminator1 = Math.sqrt(demonminator1);

		for (String line : cacheList) {
			// 右侧矩阵的行 line
			// 格式：行 tab 列_值，列_值，列_值，列_值
			String row_matrix2 = line.toString().split("\t")[0];
			String[] column_value_array_matrix2 = line.toString().split("\t")[1]
					.split(",");

			double demonminator2 = 0;
			// 计算右侧矩阵行的空间距离
			for (String column_value : column_value_array_matrix2) {
				String score = column_value.split("_")[1];
				demonminator2 += Double.valueOf(score) * Double.valueOf(score);
			}
			demonminator2 = Math.sqrt(demonminator2);

			// 矩阵两行相乘得到的结果
			double numerator = 0;
			// 遍历左矩阵的第一行的每一列
			for (String column_value_matrix1 : column_value_array_matrix1) {
				String column_matrix1 = column_value_matrix1.split("_")[0];
				String value_matrix1 = column_value_matrix1.split("_")[1];
				// 遍历右矩阵的每一行的每一列
				for (String column_value_matrix2 : column_value_array_matrix2) {
					if (column_value_matrix2.startsWith(column_matrix1 + "_")) {
						String value_matrix2 = column_value_matrix2.split("_")[1];
						// 将两列的值相乘，并累加
						numerator += Double.valueOf(value_matrix1)
								* Double.valueOf(value_matrix2);
					}
				}
			}
			
			double cos = numerator / (demonminator2 * demonminator1);
			if (cos == 0) {
				continue;
			}

			// result 是结果矩阵中的某元素，坐标为 行：row_matrix1
			// 列：row_matrix2(因为右矩阵已经转置)
			/**
			 * I1	U2_0.63,U1_0.63,U3_0.38
               I2	U1_0.76,U2_0.98,U3_0.92
               I3	U2_0.46,U3_0.76,U1_0.40
               I4	U1_0.36,U2_0.46,U3_0.28
               I5	U1_0.99,U2_0.75,U3_0.79
			 */
//			outKey.set(row_matrix1);
//			outValue.set(row_matrix2 + "_" + df.format(cos));
			outKey.set(row_matrix2);
			outValue.set(row_matrix1 + "_" + df.format(cos));
			context.write(outKey, outValue);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
}
