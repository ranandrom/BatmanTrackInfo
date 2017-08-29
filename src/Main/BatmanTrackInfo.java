package Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import SubThread.SubThread;
import Summary.Summary;


public class BatmanTrackInfo
{
	/**
	 * main方法，程序的入口.
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException
	{
		System.out.println();
		Calendar now_star = Calendar.getInstance();
		SimpleDateFormat formatter_star = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String day = formatter.format(now_star.getTime()); // 格式化后的日期

		int args_len = args.length; // 系统传入主函数的参数长度
		int Cover = 1; // 0代表覆盖汇总表，1代表更新
		int Uploadtag = 0; // 0代表所有表上传，1代表只上传更新表
		int Upload = 1; // 设置是否需要上传至/wdmycloud/anchordx_cloud/研发三共享/Batman_追踪信息/\，0代表不上传，1代表上传
		String OutputDir = "./Batman"; // 输出结果路径
		//String ExcelFormat = "xlsx"; // Excel表格式后缀
		String Input = "/iron/analysis/Batman/"; // 操作目标路径
		String PutPath = "/wdmycloud/anchordx_cloud/研发三共享/Batman_追踪信息/" + day; // 上传文件到“/wdmycloud/anchordx_cloud/研发三共享/Batman_追踪信息/”下的新建目录
		String Path = null; // 项目结果文件查找路径

		int logp = 0; // "-p"参数输入次数计算标志
		int logc = 0; // "-c"参数输入次数计算标志
		int logo = 0; // "-o"参数输入次数计算标志
		int logf = 0; // "-f"参数输入次数计算标志
		int logu = 0; // "-u"参数输入次数计算标志
		int logl = 0; // "-u"参数输入次数计算标志
		for (int len = 0; len < args_len; len += 2) {
			if (args[len].equals("-P") || args[len].equals("-p")) {
				Input = args[len + 1];
				logp++;
			} else if (args[len].equals("-C") || args[len].equals("-c")) {
				Cover = Integer.valueOf(args[len + 1]);
				logc++;
			} else if (args[len].equals("-O") || args[len].equals("-o")) {
				OutputDir = args[len + 1];
				logo++;
			} else if (args[len].equals("-F") || args[len].equals("-f")) {
				Uploadtag = Integer.valueOf(args[len + 1]);
				logf++;
			} else if (args[len].equals("-U") || args[len].equals("-u")) {
				Upload = Integer.valueOf(args[len + 1]);
				logu++;
			} else if (args[len].equals("-L") || args[len].equals("-l")) {
				PutPath = args[len + 1];
				logl++;
			} else if ((args_len == 1) && args[0].equals("-help")) {
				System.out.println();
				System.out.println("Version: V1.1.0");
				System.out.println();
				System.out.println("Usage:\t java -jar Batman_Track_Info.jar [Options] [args...]");
				System.out.println();
				System.out.println("Options:");
				System.out.println("-help\t\t Obtain parameter description.");
				System.out.println(
						"-P or -p\t Set operation path. The default value is \"/iron/analysis/Batman/\".");
				System.out.println(
						"-C or -c\t Set Whether cover old file. Inuput 0 or 1, 0 representative overwrite file data and 1 updata file data. The default value is 1.");
				System.out.println("-O or -o\t Set output file. The default value is \"./Batman\".");
				System.out.println(
						"-U or -u\t Set Whether upload file to wdmycloud. Inuput 0 or 1, 1 representative upload file and 0 is not. The default value is 1.");
				System.out.println(
						"-F or -f\t Set file upload pattern(all file or partial file). Inuput 0 or 1, 0 representative upload all file and 1 upload partial file. The default value is 0.");
				System.out.println(
						"-L or -l\t Set Upload file path. The default value is \"/wdmycloud/anchordx_cloud/研发三共享/Batman_追踪信息/\".");
				System.out.println();
				return;
			} else {
				System.out.println();
				System.out.println("对不起，您输入的Options不存在，或者缺少所需参数，请参照以下参数提示输入！");
				System.out.println();
				System.out.println("Options:");
				System.out.println("-help\t\t Obtain parameter description.");
				System.out.println(
						"-P or -p\t Set operation path. The default value is \"/iron/analysis/Batman/\".");
				System.out.println(
						"-C or -c\t Set Whether cover old file. Inuput 0 or 1, 0 representative overwrite file data and 1 updata file data. The default value is 1.");
				System.out.println("-O or -o\t Set output file. The default value is \"./Batman\".");
				System.out.println(
						"-U or -u\t Set Whether upload file to wdmycloud. Inuput 0 or 1, 1 representative upload file and 0 is not. The default value is 1.");
				System.out.println(
						"-F or -f\t Set file upload pattern(all file or partial file). Inuput 0 or 1, 0 representative upload all file and 1 upload partial file. The default value is 0.");
				System.out.println(
						"-L or -l\t Set Upload file path. The default value is \"/wdmycloud/anchordx_cloud/研发三共享/Batman_追踪信息/\".");
				System.out.println();
				return;
			}
			if (logp > 1 || logc > 1 || logo > 1 || logf > 1 || logu > 1 || logl > 1) {
				System.out.println();
				System.out.println("对不起，您输的入Options有重复，请参照以下参数提示输入！");
				System.out.println();
				System.out.println("Options:");
				System.out.println("-help\t\t Obtain parameter description.");
				System.out.println(
						"-P or -p\t Set operation path. The default value is \"/iron/analysis/Batman/\".");
				System.out.println(
						"-C or -c\t Set Whether cover old file. Inuput 0 or 1, 0 representative overwrite file data and 1 updata file data. The default value is 1.");
				System.out.println("-O or -o\t Set output file. The default value is \"./Batman\".");
				System.out.println(
						"-U or -u\t Set Whether upload file to wdmycloud. Inuput 0 or 1, 1 representative upload file and 0 is not. The default value is 1.");
				System.out.println(
						"-F or -f\t Set file upload pattern(all file or partial file). Inuput 0 or 1, 0 representative upload all file and 1 upload partial file. The default value is 0.");
				System.out.println(
						"-L or -l\t Set Upload file path. The default value is \"/wdmycloud/anchordx_cloud/研发三共享/Batman_追踪信息/\".");
				System.out.println();
				return;
			}
		}

		System.out.println("程序开始时间: " + formatter_star.format(now_star.getTime()));
		System.out.println("===============================================");
		System.out.println("Batman_Track_Info.1.1.0");
		System.out.println("***********************************************");
		System.out.println();
		
		/*String Arr[] = "AnchorDx - Bait bed file         : /iron/bioinfo/home/yangt/pipeline/Batman/WAYNE/CancerPanel.ProbeDesign.bed".split("/");
		System.out.println(Arr.length);
		for (int i=0; i<Arr.length; i++) {
			System.out.println(Arr[i]);
		}*/
			
		String Summary_Path = OutputDir + "/Summary/";
		File DAP = new File(Summary_Path);
		String oldfileday = null;
		if (Cover == 1) {
			if (DAP.exists() && DAP.isDirectory()) {
				//copyOldFile(Data_Aggregation_Path); // 复制指定目录下最新日期的文件
				oldfileday = rsyncOldExcel(Summary_Path); // 复制指定目录下最新日期的文件
				if (oldfileday == null) {
					System.out.println("rsync 失败！");
					System.out.println();
					return;
				} else {
					System.out.println("rsync 成功！");
					System.out.println();
				}
			} else {
				System.out.println(Summary_Path + "目录不存在");
			}
		}

		File fileInput = new File(Input);
		ExecutorService exe = Executors.newFixedThreadPool(15); // 设置线程池最大线程数为15

		int Input_length = 0;
		String InputArr[] = Input.split("/");
		for (int i = 0; i < InputArr.length; i++) {
			if (InputArr[InputArr.length - 1].equals("Batman")) {
				Input_length = 0;
			} else if (InputArr[InputArr.length - 2].equals("Batman")) {
				Input_length = 1;
			} else if (InputArr[InputArr.length - 3].equals("Batman")) {
				Input_length = 2;
			}
		}
		
		if (Input_length == 0) {
			Path = OutputDir;
			for (File pathname : fileInput.listFiles()) {
				exe.execute(new SubThread(OutputDir, pathname, Input_length));
			}
		} else if (Input_length == 1) {
			Path = OutputDir + "/" + InputArr[InputArr.length - 1];
			exe.execute(new SubThread(OutputDir, fileInput, Input_length));

		} else if (Input_length == 2) {
			Path = OutputDir + "/" + InputArr[InputArr.length - 2] + "/" + InputArr[InputArr.length - 1];
			exe.execute(new SubThread(OutputDir, fileInput, Input_length));
		} else {
			System.out.println(Input + "是非法输入，请重新输入！");
			return;
		}
		exe.shutdown(); // 关闭线程池
		while (true) {
			if (exe.isTerminated()) { // 先让所有的子线程运行完，再运行主线程
				//DataAggregation.outPutData(OutputDir + "/Data_Aggregation/" + day, Path, Cover, PutPath, Uploadtag, Upload, oldfileday); // 数据汇总
				//System.out.println("所有的子线程运行结束！");
				break;
			}
			Thread.sleep(500);
		}
		//DataAggregation.outPutData(dir + "/Data_Aggregation/" + day, Path, Cover, PutPath, Uploadtag, Upload, oldfileday); // 数据汇总
		Summary.outPutData(Summary_Path + day, Path, Upload, PutPath, Cover, oldfileday); // 数据汇总

		String cmd2 = "rm -r ./oldExcel";
		try {
			Process process2 = Runtime.getRuntime().exec(cmd2);
			BufferedReader input2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));
			String line2 = null;
			while ((line2 = input2.readLine()) != null) { // 循环读出系统返回数据，保证系统调用已经正常结束
				// System.out.println(line);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Thread.sleep(3000);
		//uploadFileToFront(dir); // 上传文件到阿里云端

		/*String str  = "160823_NS500803_0021_AHJLKKBGXY";
		String str1  = "160824_E00454_0044_BH3Y5LALXX-X10";
		String str2  = "161230_E00454_0092_AHFHMGALXX_old";
		String InputArr[] = str1.split("_");
		System.out.println(InputArr.length);
		
		System.out.println(Regular_Expression("160824", "^[0-2]\\d[0-1]\\d[0-3]\\d"));
		System.out.println(Regular_Expression("E00495", "^[A-Z]{1,}\\d{1,}"));
		System.out.println(Regular_Expression("0046", "^\\d{4}"));
		System.out.println(Regular_Expression("BH3Y5LALXX-X10", "^[A-Z0-9]{1,}"));
		
		if (InputArr.length == 4) {
			if(InputArr[0].length() == 6 && Regular_Expression(InputArr[0], "^[0-2]\\d[0-1]\\d[0-3]\\d") != null){
				if(Regular_Expression(InputArr[1], "^[A-Z]{1,}\\d{1,}") != null){
					if(InputArr[2].length() == 4 && Regular_Expression(InputArr[2], "^\\d{4}") != null){
						if(Regular_Expression(InputArr[3], "^[A-Z0-9]{1,}") != null){
							System.out.println("6666");
						}
					}
				}
			}
		}*/
		
		
		Calendar now_end = Calendar.getInstance();
		SimpleDateFormat formatter_end = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println();
		System.out.println("==============================================");
		System.out.println("程序结束时间: " + formatter_end.format(now_end.getTime()));
		System.out.println();
	}
	
	/**
	 * 远程复制文件的方法：
	 * 利用rsync远程复制zhirong_lu@192.192.192.200:/wdmycloud/anchordx_cloud/杨莹莹/
	 * 项目-生信-汇总表/最新目录到本地，程序正常返回0，否则返回-1.
	 */
	public static String rsyncOldExcel(String Path)
	{
		String daynum = getNewestFileDir(Path); // 获取指定目录下最新日期命名的目录名
		try {
			String cmd_Sample_statistics[] = {"rsync", "-aP", "--include=*/", "--include=**/*.xls*",
					"--exclude=*", "zhirong_lu@192.192.192.220:/wdmycloud/anchordx_cloud/研发三共享/Batman_追踪信息/"+daynum, "./oldExcel/"};
			Process process = Runtime.getRuntime().exec(cmd_Sample_statistics);
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while ((line = input.readLine()) != null) { // 循环读出系统返回数据，保证系统调用已经正常结束
				 //System.out.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "off";
		}
		//copyOldFile("./oldExcel/", Path);
		//copyOldFile("./oldExcel/", "./oldExcel/");
		return daynum;
	}
	
	/**
	 * 获取指定目录下最新日期命名的目录名
	 * 
	 * @param Path
	 * @return String
	 */
	public static String getNewestFileDir(String Path)
	{
		File file = new File(Path);
		int daynum = 0;
		for (File dir : file.listFiles()) {
			if (dir.isDirectory()) { // 如果是目录
				String dir_name = dir.getName(); // 目录名
				if (daynum < Integer.valueOf(dir_name)) {
					daynum = Integer.valueOf(dir_name);
				} else {
					continue;
				}
			} else {
				continue;
			}
		}
		return String.valueOf(daynum);
	}

}
