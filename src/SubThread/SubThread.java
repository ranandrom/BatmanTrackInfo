package SubThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



public class SubThread extends Thread
{
	private String OutputDir;
	private File pathname;
	private int inputlenght;

	/**
	 * SubThread类的构造器。
	 * 
	 * @param dir
	 * @param pathname
	 * @param ExcelFormat
	 * @param inputlenght
	 */
	public SubThread(String OutputDir, File pathname, int inputlenght) {
		this.OutputDir = OutputDir;
		this.pathname = pathname;
		this.inputlenght = inputlenght;
	}

	/**
	 * 重写的线程类run方法。
	 */
	public void run()
	{
		Calendar now = Calendar.getInstance();
		SimpleDateFormat formatter_Date = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat formatter_end = new SimpleDateFormat("HH_mm_ss");
		String Day = formatter_Date.format(now.getTime());
		String time = formatter_end.format(now.getTime());
		
		if (inputlenght == 0 || inputlenght == 1) {
			if (pathname.isDirectory()) { // 如果是目录
				String dir_name = pathname.getName(); // 目录名
				String InputArr[] = dir_name.split("_");
				if (InputArr.length == 4) {
					if(InputArr[0].length() == 6 && Regular_Expression(InputArr[0], "^[0-2]\\d[0-1]\\d[0-3]\\d") != null){
						if(Regular_Expression(InputArr[1], "^[A-Z]{1,}\\d{1,}") != null){
							if(InputArr[2].length() == 4 && Regular_Expression(InputArr[2], "^\\d{4}") != null){
								if(Regular_Expression(InputArr[3], "^[A-Z0-9]{1,}") != null){
									String Sequencing_Info = OutputDir + "/" + dir_name;									
									for (File porject : pathname.listFiles()) {
										if (porject.isDirectory()) { // 如果是目录
											// 获取文件的绝对路径
											String Folder = porject.getParent(); // 父目录
											String Por_name = porject.getName(); // 子目录名
											String RawBamPath = Folder + "/" + Por_name;
											String Por_dir = Sequencing_Info + "/" + Por_name;
											String Track_ExcelName = "BatmanTrackInfo_" + Por_name + "_" + Day + ".xlsx"; // 追踪数据表												
											String Track_Excel = Por_dir + "/" + Track_ExcelName;												
											File Track_excel = new File(Track_Excel);												
											my_mkdir(Por_dir);
											createXlsx(Track_excel); // 创建追踪数据表
											
											extractInfo(Track_Excel, RawBamPath, Por_name, dir_name);
											/*extractInfo(Plasma_Excel, Tissue_Excel, BC_Excel, Test_Excel, Plasma_Tsv, Tissue_Tsv, BC_Tsv,
													Test_Tsv, Path, Por_name);*/
												
										} else {
											continue;
										}
									}
								} else {
									return;
								}
							} else {
								return;
							}
						} else {
							return;
						}
					} else {
						return;
					}
				} else {
					return;
				}
			}
		} else if (inputlenght == 2) {
			// 获取文件的绝对路径
			String Folder = pathname.getParent(); // 父目录
			String Foldername = new File(Folder).getName(); // 父目录名
			String Por_name = pathname.getName(); // 子目录名
			String RawBamPath = Folder + "/" + Por_name;
			String Por_dir = OutputDir + "/" + Foldername + "/" + Por_name;
			String Track_ExcelName = "BatmanTrackInfo_" + Por_name + "_" + Day + ".xlsx"; // 追踪数据表												
			String Track_Excel = Por_dir + "/" + Track_ExcelName;												
			File Track_excel = new File(Track_Excel);												
			my_mkdir(Por_dir);
			createXlsx(Track_excel); // 创建追踪数据表

			extractInfo(Track_Excel, RawBamPath, Por_name, Foldername);
			//extractInfo(Plasma_Excel, Tissue_Excel, BC_Excel, Test_Excel, Plasma_Tsv, Tissue_Tsv, BC_Tsv, Test_Tsv, Path, Por_name);

		}
	}
	
	/**
	 * 调用正则表达式的方法。
	 * 
	 * @param str
	 * @param regEx
	 * @return String
	 */
	public static String Regular_Expression(String str, String regEx)
	{
		String data = null;
		// 编译正则表达式
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			data = matcher.group();
		}
		return data;
	}
	
	/**
	 * 创建目录的方法。
	 * 
	 * @param dir_name
	 */
	public static void my_mkdir(String dir_name)
	{
		File file = new File(dir_name);
		// 如果文件不存在，则创建
		if (!file.exists() && !file.isDirectory()) {
			file.mkdirs();
		}
	}
	
	/**
	 * 新建xlsx格式文件的方法。
	 * 
	 * @param file
	 */
	@SuppressWarnings("deprecation")
	public static void createXlsx(File file)
	{
		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			// 创建Excel的工作sheet,对应到一个excel文档的tab  
			XSSFSheet sheet = workbook.createSheet("sheet1");
			// 在索引0的位置创建行（最顶端的行）
			XSSFRow row0 = sheet.createRow((short) 0);

			String head_row0 = "Pre-lib name" + "\t" + "Identification name" + "\t" + "Sequencing info" + "\t" + "Sequencing file path" + "\t" 
					+ "PF_READS" + "\t" + "PF_UQ_READS_ALIGNED" + "\t" + "PCT_PF_UQ_READS_ALIGNED" + "\t" + "PCT_SELECTED_BASES" + "\t" 
					+ "MEAN_BAIT_COVERAGE" + "\t" + "MEAN_TARGET_COVERAGE" + "\t" + "MEDIAN_TARGET_COVERAGE" + "\t" 
					+ "FOLD_ENRICHMENT" + "\t" + "FOLD_80_BASE_PENALTY" + "\t" 
					+ "PCT_TARGET_BASES_1X" + "\t" + "PCT_TARGET_BASES_20X" + "\t" + "PCT_TARGET_BASES_50X" + "\t" + "PCT_TARGET_BASES_100X" + "\t" 
					+ "AT_DROPOUT" + "\t" + "GC_DROPOUT" + "\t" + "MEAN_TARGET_COVERAGE_deduped" + "\t" + "MEAN_TARGET_COVERAGE_UMI-deduped" + "\t"
					+ "MEAN_TARGET_COVERAGE_UMI-deduped_rm-singlets" + "\t" + "PCT_SINGLETS" + "\t" + "SUMMARY" + "\t" + "Qcresult" + "\t"
					+ "Path to raw.bam" + "\t" + "Path to sorted_UMI_dedup.bam" + "\t"+ "Path to sorted.dedup.vcf" + "\t"
					+ "Path to sorted.UMI_dedup.vcf" + "\t" + "Data analysis" + "\t" + "build library type" + "\t" + "Bait set" + "\t"
					+ "path to coverage" + "\t" + "path to CNV result" + "\t" + "path to CNV plot" + "\t" + "spm dedup bam" + "\t"
					+ "path to fusion plot" + "\t" + "path to fusion result" + "\t"
					+ "Mark" + "\t" + "Check" + "\t" + "Note1" + "\t" + "Note2" + "\t" + "Note3";

			// 1、创建字体，设置其为粗体，背景蓝色：
			XSSFFont font1 = workbook.createFont();
			font1.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
			font1.setFontHeightInPoints((short) 10);
			font1.setFontName("Palatino Linotype");
			// 2、创建格式
			XSSFCellStyle cellStyle1 = workbook.createCellStyle();
			cellStyle1.setFont(font1);
			cellStyle1.setBorderTop(XSSFCellStyle.BORDER_THIN);
			cellStyle1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			cellStyle1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			cellStyle1.setBorderRight(XSSFCellStyle.BORDER_THIN);
			cellStyle1.setFillForegroundColor(HSSFColor.PALE_BLUE.index);
			cellStyle1.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

			// 1、创建字体，设置其为红色、粗体，背景绿色：
			XSSFFont font2 = workbook.createFont();
			font2.setColor(HSSFFont.COLOR_RED);
			font2.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
			font2.setFontHeightInPoints((short) 10);
			font2.setFontName("Palatino Linotype");
			// 2、创建格式
			XSSFCellStyle cellStyle2 = workbook.createCellStyle();
			cellStyle2.setFont(font2);
			cellStyle2.setBorderTop(XSSFCellStyle.BORDER_THIN);
			cellStyle2.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			cellStyle2.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			cellStyle2.setBorderRight(XSSFCellStyle.BORDER_THIN);
			cellStyle2.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
			cellStyle2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

			// 1、创建字体，设置其为粗体，背景黄色：
			XSSFFont font3 = workbook.createFont();
			font3.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
			font3.setFontHeightInPoints((short) 10);
			font3.setFontName("Palatino Linotype");
			// 2、创建格式
			XSSFCellStyle cellStyle3 = workbook.createCellStyle();
			cellStyle3.setFont(font3);
			cellStyle3.setBorderTop(XSSFCellStyle.BORDER_THIN);
			cellStyle3.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			cellStyle3.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			cellStyle3.setBorderRight(XSSFCellStyle.BORDER_THIN);
			cellStyle3.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
			cellStyle3.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

			String str_head_row0[] = head_row0.split("\t");
			// 在单元格中输入一些内容
			for (int i = 0; i < str_head_row0.length; i++) {
				// 在索引0的位置创建单元格（左上端）
				XSSFCell cell = row0.createCell(i);
				if (i < 4) { // 红字橘底
					cell.setCellValue(str_head_row0[i]);
					cell.setCellStyle(cellStyle2);
				} else if (i > str_head_row0.length-6) { // 黑字黄底。
					cell.setCellStyle(cellStyle3);
					cell.setCellValue(str_head_row0[i]);
				} else { // 剩下的生信表格的列：黑字蓝底
					cell.setCellStyle(cellStyle1);
					cell.setCellValue(str_head_row0[i]);
				}
			}

			// 新建一输出文件流
			FileOutputStream fOut = new FileOutputStream(file);
			// 把相应的Excel工作簿存盘
			workbook.write(fOut);
			fOut.flush();
			// 操作结束，关闭文件
			fOut.close();
			workbook.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 调用linux命令获取符合要求的文件列表(跳过链接文件)
	 * 
	 * @param Input
	 * @return List
	 */
	public static ArrayList<String> searchRawBam(String RawBamPath)
	{
		ArrayList<String> RawBamList = new ArrayList<String>();
		try {
			String cmd = "find " + RawBamPath + " -type f -name *raw.bam"; // 查找该目录下所有*raw.bam文件（链接文件除外）
			Process process = Runtime.getRuntime().exec(cmd);
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = "";
			while ((line = input.readLine()) != null) {				
					RawBamList.add(line);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("searchRawBam异常！！！！");
		}
		return RawBamList;
	}
	
	public static String searchFile(String FilePath, String FileName)
	{
		String Fastq = null;
		try {
			String cmd = "find " + FilePath + " -type f -name " + FileName; // 查找该目录下所有*raw.bam文件（链接文件除外）
			Process process = Runtime.getRuntime().exec(cmd);
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = "";
			while ((line = input.readLine()) != null) {				
				Fastq = line;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("searchFastq异常！！！！");
		}
		return Fastq;
	}
	
	/**
	 * 获取修改时间的方法  
	 * 
	 * @param file
	 * @return String
	 */
	public static String getModifiedTime(String file)
	{
		File f = new File(file);
		Calendar cal = Calendar.getInstance();
		long time = f.lastModified();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		cal.setTimeInMillis(time);
		return formatter.format(cal.getTime()); // 返回格式化的日期
	}
	
	// 提取QC.xls信息
	public static HashMap<String, String> getQCExcelInfo(String filename, String build_library_type){
		//System.out.println("getQCExcelInfo: "+filename);
		String type = filename.substring(filename.lastIndexOf(".")+1);//获取文件类型
		File file = new File(filename);
		HashMap<String, String> map_logo = new HashMap<String, String>(); // 数据结果的集合
		try {
			String[] cmd = { "awk", "{print}", filename };
			//String cmd = "find " + module_fPath + " -type f -name Pipeline*report*-*Picard*module.txt"; // 查找该目录下所有*raw.bam文件（链接文件除外）
			Process process = Runtime.getRuntime().exec(cmd);
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = "";
			while ((line = input.readLine()) != null) {
				String Arr[] = line.split("\t");
				if (Arr[0].equals("SUMMARY")) {
					map_logo.put(Arr[0], Arr[4]);
					continue;
				} else {
					if (build_library_type != null && build_library_type.equals("KAPA")) {
						if (Arr[0].contains("UMI") && Arr[3].equals("NA")) {
							map_logo.put(Arr[0], "-");
						} else {
							map_logo.put(Arr[0], Arr[3]);
						}
						continue;
					} else {
						map_logo.put(Arr[0], Arr[3]);
						continue;
					}
				}
				
				//System.out.println("line: " + line);
				//System.out.println("Arr[0]: " + Arr[0]);
				//System.out.println("Arr[3]: " + Arr[3]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("searchFastq异常！！！！");
		}
		/*try {
			if(type.equals("xls")){
				map_logo = readXls(file);
			}else if(type.equals("xlsx")){
				map_logo = readXlsx(file);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}*/
		return map_logo;
	}
	
	/**
	 * 
	 * @param wb:excel文件对象
	 */
	//读xls格式文件
	@SuppressWarnings({ "unused", "deprecation" })
	public static HashMap<String, String> readXls(File file) throws Exception{
		HashMap<String, String> map_logo = new HashMap<String, String>(); // 数据结果的集合
		
		InputStream is = new FileInputStream(file);
		HSSFWorkbook wb = new HSSFWorkbook(is);
		Sheet sheet = wb.getSheetAt(0); // 对应excel正文对象		
		
		for(int i = sheet.getFirstRowNum()+1; i <= sheet.getLastRowNum(); i++){
			HSSFRow hssfrow = (HSSFRow) sheet.getRow(i); // 获取行
			
			HSSFCell hssfcell0 = hssfrow.getCell(0);
			//HSSFCell hssfcell1 = hssfrow.getCell(1);
			hssfcell0.setCellType(Cell.CELL_TYPE_STRING);
			if( (hssfcell0 != null) && (hssfcell0.getStringCellValue().trim().equals("SUMMARY")) ){
				HSSFCell hssfcell5 = hssfrow.getCell(4);
				hssfcell5.setCellType(Cell.CELL_TYPE_STRING);
				if (hssfcell5 != null) {
					map_logo.put(hssfcell0.getStringCellValue().trim(), hssfcell5.getStringCellValue().trim());
				} else {
					map_logo.put(hssfcell0.getStringCellValue().trim(), "NA");
				}
				continue;
			}else{
				HSSFCell hssfcell4 = hssfrow.getCell(3);
				hssfcell4.setCellType(Cell.CELL_TYPE_STRING);
				if (hssfcell4 != null) {
					map_logo.put(hssfcell0.getStringCellValue().trim(), hssfcell4.getStringCellValue().trim());
				} else {
					map_logo.put(hssfcell0.getStringCellValue().trim(), "NA");
				}
				continue;
			}
		}
		try {
			//System.out.println("+++");
			is.close();
			wb.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map_logo;
	}
	
	/**
	 * 
	 * @param wb:excel文件对象
	 */
	//读xlsx格式文件
	@SuppressWarnings("unused")
	public static HashMap<String, String> readXlsx(File file) throws Exception {
		
		HashMap<String, String> map_logo = new HashMap<String, String>(); // 数据结果的集合
		
		InputStream is = new FileInputStream(file);
		XSSFWorkbook wb = new XSSFWorkbook(is);
		XSSFSheet sheet = wb.getSheetAt(0);	//获取第1个工作薄
		
		// 获取当前工作薄的每一行
		for (int i = sheet.getFirstRowNum()+3; i <= sheet.getLastRowNum(); i++) {
			XSSFRow xssfrow = sheet.getRow(i);

			XSSFCell xssfcell0 = xssfrow.getCell(0);		
			String xce0 = String.valueOf(xssfcell0).trim();
			if( (xssfcell0 != null) && (xce0.equals("SUMMARY"))  ){
				XSSFCell xssfcell5 = xssfrow.getCell(4);
				String xce5 = String.valueOf(xssfcell5).trim();
				if (xce5 != null) {
					map_logo.put(xce0, xce5);
				} else {
					map_logo.put(xce0, "NA");
				}
				continue;
			}else{
				XSSFCell xssfcell4 = xssfrow.getCell(4);
				String xce4 = String.valueOf(xssfcell4).trim();
				if (xce4 != null) {
					map_logo.put(xce0, xce4);
				} else {
					map_logo.put(xce0, "NA");
				}
				continue;
			}
		}
		try {
			is.close();
			wb.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map_logo;
	}
	
	// 获取Bait set信息
	public static String Bait_set_Info(String modulefile){
		String data = null;
		//System.out.println("modulefile:"+modulefile);
		try {
				String encoding = "GBK";
				File file = new File(modulefile);
				InputStreamReader read = new InputStreamReader(
				new FileInputStream(file),encoding);//考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;

				while((lineTxt = bufferedReader.readLine()) != null){
					//System.out.println("lineTxt1:"+lineTxt);
					//判断是否为头格式行数据
					if(lineTxt.contains("AnchorDx - Bait bed file")){
						//System.out.println("lineTxt2:"+lineTxt);
						String str[] = lineTxt.split("/");
						data = str[str.length-1];
						break;
					} else {
						continue;
					}
				}
				read.close();
		}catch (Exception e) {			
			//System.out.println("读取文件内容出错："+filePath);
			e.printStackTrace();
			//return "OFF";
		}
		return data;	
	}
	
	//判断指定行是否为空,如果为空，则返回0
	@SuppressWarnings("deprecation")
	public static int checkRowNull(XSSFRow xssfRow)
	{
		int num = 0;
		// 获取当前工作薄的每一列
		for (int j = xssfRow.getFirstCellNum(); j < xssfRow.getLastCellNum(); j++) {
			XSSFCell xssfcell = xssfRow.getCell(j);
			if (xssfcell == null || (("") == String.valueOf(xssfcell)) || xssfcell.toString().equals("")
					|| xssfcell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
				continue;
			} else {
				num++;
			}
		}
		return num;
	}
	
	// 写数据到指定xlsx格式文件
	public static void writeXlsx(File file, String logo, String data, int rownum) throws Exception
	{
		/*if (logo.equals("Pre-lib name")) {
			System.out.println("writeXlsx: "+data);
		}*/
		FileInputStream is = new FileInputStream(file);
		XSSFWorkbook wb = new XSSFWorkbook(is);
		XSSFSheet sheet = wb.getSheetAt(0); // 获取第1个工作薄
		int cellIndex = 0;
		XSSFRow xssfrow = sheet.getRow(0);
		// 获取当前工作薄的每一列
		for (int j = xssfrow.getFirstCellNum(); j < xssfrow.getLastCellNum(); j++) {
			XSSFCell xssfcell = xssfrow.getCell(j);
			if (xssfcell != null) {
				String cellValue = String.valueOf(xssfcell).trim();
				if (cellValue.equals(logo)) {
					cellIndex = j;
				} else {
					continue;
				}
			}
		}
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int addrownum = rownum;
		// 指定行索引，创建一行数据, 行索引为当前最后一行的行索引 + 1
		int currentLastRowIndex = sheet.getLastRowNum();
		if (checkRowNull(sheet.getRow(currentLastRowIndex)) == 0) {
			addrownum = 0;
		}
		int newRowIndex = currentLastRowIndex + addrownum;
		XSSFRow newRow = null;
		if (addrownum == 0) {
			newRow = sheet.getRow(newRowIndex);
		} else {
			newRow = sheet.createRow(newRowIndex);
		}
		// 创建一个单元格，设置其内的数据格式为字符串，并填充内容，其余单元格类同
		XSSFCell newGenderCell = newRow.createCell(cellIndex, Cell.CELL_TYPE_STRING);
		newGenderCell.setCellValue(data);

		// 首先要创建一个原始Excel文件的输出流对象！
		FileOutputStream excelFileOutPutStream = new FileOutputStream(file);
		// 将最新的 Excel 文件写入到文件输出流中，更新文件信息！
		wb.write(excelFileOutPutStream);
		// 执行 flush 操作， 将缓存区内的信息更新到文件上
		excelFileOutPutStream.flush();
		// 使用后，及时关闭这个输出流对象， 好习惯，再强调一遍！
		excelFileOutPutStream.close();
		wb.close();
	}
	
	// 查找信息
	public static void extractInfo(String Track_excel,  String RawBamPath, String Folder, String RFolder)
	{
		String data = null;
		File Outputfile = new File(Track_excel);
		HashMap<String, String> map_logo = new HashMap<String, String>(); // 数据结果的集合
		ArrayList<String> RawBamList = searchRawBam(RawBamPath);
		
		for (int i=0; i<RawBamList.size(); i++) {
			map_logo.clear();
			String IdentificationName = null;
			
			// 向数据结果集合添加 Pre-lib name 的值
			map_logo.put("Pre-lib name", "NA");
			
			// 向数据结果集合添加 Identification name 的值
			data = null;
			File rawbam = new File(RawBamList.get(i));
			String RawBamArr[] = rawbam.getName().split("\\.");
			for(int j=0; j<RawBamArr.length-2; j++){
				if (j==0) {
					data = RawBamArr[j];
				} else {
					data += "." + RawBamArr[j];
				}
			}
			//String IdentificationNameArr[] = data.split("_");
			IdentificationName = data;
			//IdentificationName = IdentificationNameArr[0];
			map_logo.put("Identification name", IdentificationName);
			
			// 向数据结果集合添加 Sequencing info 的值
			map_logo.put("Sequencing info", RFolder);
			
			// 向数据结果集合添加Sequencing file path 的值
			data = null;
			String FastqPath = "/iron/nextseq500/outputdata/"+RFolder+"/Data/Intensities/BaseCalls/";
			String FastqName = IdentificationName+"*.fastq.gz";
			data = searchFile(FastqPath, FastqName);
			if (data != null) {
				map_logo.put("Sequencing file path", data);
			} else {
				map_logo.put("Sequencing file path", "NA");
			}
			
			// 向数据结果集合添加 Path to sorted_UMI_dedup.bam 的值
			data = null;
			String build_library_type = null;
			String UMI_dedup_bam_Path = RawBamPath;
			String UMI_dedup_bam_Name = IdentificationName+"*.sorted.UMI_deduplicated.bam";
			data = searchFile(UMI_dedup_bam_Path, UMI_dedup_bam_Name);
			if (data != null) {
				map_logo.put("Path to sorted_UMI_dedup.bam", data);
				// 向数据结果集合添加 build library type 的值
				map_logo.put("build library type", "Batman");
				build_library_type = "Batman";
			} else {
				UMI_dedup_bam_Name = IdentificationName+"*.sorted.dedup.bam";
				data = searchFile(UMI_dedup_bam_Path, UMI_dedup_bam_Name);
				if (data != null) {
					map_logo.put("Path to sorted_UMI_dedup.bam", data);
					// 向数据结果集合添加 build library type 的值
					map_logo.put("build library type", "KAPA");
					build_library_type = "KAPA";
				} else {
					map_logo.put("Path to sorted_UMI_dedup.bam", "NA");
					// 向数据结果集合添加 build library type 的值
					map_logo.put("build library type", "NA");
				}
			}
			
			// 向数据结果集合添加 Path to sorted.UMI_dedup.vcf 的值Data analysis
			data = null;
			if (build_library_type != null && build_library_type.equals("KAPA")) {
				map_logo.put("Path to sorted.UMI_dedup.vcf", "-");
			} else {
				String UMI_cvfPath = RawBamPath;
				String UMI_cvfName = IdentificationName+"*.sorted.UMI_deduplicated*.onTarget.annovared";
				data = searchFile(UMI_cvfPath, UMI_cvfName);
				if (data != null){
					map_logo.put("Path to sorted.UMI_dedup.vcf", data);
				} else {
					map_logo.put("Path to sorted.UMI_dedup.vcf", "NA");
				}
			}
				
			// 向数据结果集合添加Qcresult 的值
			data = null;
			String QCPath = RawBamPath;
			String QCName = IdentificationName+"*.hsmetrics.QC.xls*";
			data = searchFile(QCPath, QCName);
			// 提取QC信息
			if (data != null) {
				map_logo.put("Qcresult", data);
				HashMap<String, String> map_QC = getQCExcelInfo(data, build_library_type);
				map_logo.putAll(map_QC);
			} else {
				// 向数据结果集合添加Qcresult 的值
				map_logo.put("Qcresult", "NA");
				
				// 向数据结果集合添加PF_READS 的值
				map_logo.put("PF_READS", "NA");
				
				// 向数据结果集合添加 PF_UQ_READS_ALIGNED 的值
				map_logo.put("PF_UQ_READS_ALIGNED", "NA");
				
				// 向数据结果集合添加 PCT_PF_UQ_READS_ALIGNED 的值
				map_logo.put("PCT_PF_UQ_READS_ALIGNED", "NA");
				
				// 向数据结果集合添加PCT_SELECTED_BASES 的值
				map_logo.put("PCT_SELECTED_BASES", "NA");
				
				// 向数据结果集合添加MEAN_BAIT_COVERAGE 的值
				map_logo.put("MEAN_BAIT_COVERAGE", "NA");
				
				// 向数据结果集合添加 MEAN_TARGET_COVERAGE 的值
				map_logo.put("MEAN_TARGET_COVERAGE", "NA");
				
				// 向数据结果集合添加MEDIAN_TARGET_COVERAGE 的值
				map_logo.put("MEDIAN_TARGET_COVERAGE", "NA");
				
				// 向数据结果集合添加 FOLD_ENRICHMENT 的值
				map_logo.put("FOLD_ENRICHMENT", "NA");
				
				// 向数据结果集合添加FOLD_80_BASE_PENALTY 的值
				map_logo.put("FOLD_80_BASE_PENALTY", "NA");
				
				// 向数据结果集合添加 PCT_TARGET_BASES_1X 的值
				map_logo.put("PCT_TARGET_BASES_1X", "NA");
				
				// 向数据结果集合添加 PCT_TARGET_BASES_20X 的值
				map_logo.put("PCT_TARGET_BASES_20X", "NA");
				
				// 向数据结果集合添加PCT_TARGET_BASES_50X 的值
				map_logo.put("PCT_TARGET_BASES_50X", "NA");
				
				// 向数据结果集合添加 PCT_TARGET_BASES_100X 的值
				map_logo.put("PCT_TARGET_BASES_100X", "NA");
				
				// 向数据结果集合添加 AT_DROPOUT 的值
				map_logo.put("AT_DROPOUT", "NA");
				
				// 向数据结果集合添加 GC_DROPOUT 的值
				map_logo.put("GC_DROPOUT", "NA");
				
				// 向数据结果集合添加 MEAN_TARGET_COVERAGE_deduped 的值
				map_logo.put("MEAN_TARGET_COVERAGE_deduped", "NA");
				
				// 向数据结果集合添加 MEAN_TARGET_COVERAGE_UMI-deduped 的值
				map_logo.put("MEAN_TARGET_COVERAGE_UMI-deduped", "NA");
				
				// 向数据结果集合添加MEAN_TARGET_COVERAGE_UMI-deduped_rm-singlets 的值
				map_logo.put("MEAN_TARGET_COVERAGE_UMI-deduped_rm-singlets", "NA");
				
				// 向数据结果集合添加 PCT_SINGLETS 的值
				map_logo.put("PCT_SINGLETS", "NA");
				
				// 向数据结果集合添加 SUMMARY 的值
				map_logo.put("SUMMARY", "NA");
			}

			// 向数据结果集合添加 Path to raw.bam 的值
			map_logo.put("Path to raw.bam", RawBamList.get(i));
			
			// 向数据结果集合添加 Path to sorted.dedup.vcf 的值，
			data = null;
			String cvfPath = RawBamPath;
			String cvfName = IdentificationName+"*.sorted.dedup*.onTarget.annovared";
			data = searchFile(cvfPath, cvfName);
			if (data != null){
				map_logo.put("Path to sorted.dedup.vcf", data);
			} else {
				cvfName = IdentificationName+"*.raw.dedup*.onTarget.annovared";
				data = searchFile(cvfPath, cvfName);
				if (data != null){
					map_logo.put("Path to sorted.dedup.vcf", data);
				} else {
					map_logo.put("Path to sorted.dedup.vcf", "NA");
				}
			}			
			
			// 向数据结果集合添加 Data analysis 的值
			data = null;
			data = getModifiedTime(RawBamList.get(i));
			map_logo.put("Data analysis", data);
			
			
			// 向数据结果集合添加Bait set 的值
			data = null;
			String module_fPath = RawBamPath;
			String module_fName ="Pipeline*report*-*Picard*module.txt";
			String modulefile = searchFile(module_fPath, module_fName);
			if (modulefile != null) {
				data = Bait_set_Info(modulefile);
				if (data != null){
					map_logo.put("Bait set", data);
				} else {
					map_logo.put("Bait set", "NA");
				}
			} else {
				map_logo.put("Bait set", "NA");
			}
			
			// 向数据结果集合添加path to coverage 的值
			data = null;
			String coverage_fPath = "/iron/analysis/CNV/"+RFolder;
			String coverage_Name = IdentificationName+"*.perTarget.coverage";
			data = searchFile(coverage_fPath, coverage_Name);
			if (data != null){
				map_logo.put("path to coverage", data);
			} else {
				map_logo.put("path to coverage", "NA");
			}
			
			// 向数据结果集合添加path to CNV result 的值
			data = null;
			String CNV_result_fPath = "/iron/analysis/CNV/"+RFolder;
			String CNV_result_Name = "report_" + IdentificationName + "*.xls*";
			data = searchFile(CNV_result_fPath, CNV_result_Name);
			if (data != null){
				map_logo.put("path to CNV result", data);
			} else {
				map_logo.put("path to CNV result", "NA");
			}
			
			// 向数据结果集合添加path to CNV plot 的值
			data = null;
			String CNV_plot_fPath = "/iron/analysis/CNV/"+RFolder;
			String CNV_plot_Name = "05.plot_for_each_gene_" + IdentificationName + "*.pdf";
			data = searchFile(CNV_plot_fPath, CNV_plot_Name);
			if (data != null){
				map_logo.put("path to CNV plot", data);
			} else {
				map_logo.put("path to CNV plot", "NA");
			}
			
			// 向数据结果集合添加spm dedup bam 的值
			data = null;
			String dedup_bam_fPath = "/iron/analysis/Spiderman/"+RFolder;
			String dedup_bam_Name = IdentificationName + "*.dedup.bam";
			data = searchFile(dedup_bam_fPath, dedup_bam_Name);
			if (data != null){
				map_logo.put("spm dedup bam", data);
			} else {
				map_logo.put("spm dedup bam", "NA");
			}
			
			// 向数据结果集合添加path to fusion plot 的值
			data = null;
			String fusion_plot_fPath = "/iron/analysis/Spiderman/"+RFolder;
			String fusion_plot_Name = IdentificationName + "*.fusionplot.pdf";
			data = searchFile(fusion_plot_fPath, fusion_plot_Name);
			if (data != null){
				map_logo.put("path to fusion plot", data);
			} else {
				map_logo.put("path to fusion plot", "NA");
			}
			
			// 向数据结果集合添加path to fusion result 的值
			data = null;
			String fusion_result_fPath = "/iron/analysis/Spiderman/"+RFolder;
			String fusion_result_Name = IdentificationName + "*.raw.dedup.fusion.valid.xls*";
			data = searchFile(fusion_result_fPath, fusion_result_Name);
			if (data != null){
				map_logo.put("path to fusion result", data);
			} else {
				map_logo.put("path to fusion result", "NA");
			}
			
			try {
				int rownum = 1;
				for (String key : map_logo.keySet()) {
					/*if (key.equals("Pre-lib name")) {
						System.out.println(map_logo.get(key));
					}*/
					//System.out.println("key:"+key+"\tvalue: "+map_logo.get(key));
					writeXlsx(Outputfile, key, map_logo.get(key), rownum); // 写数据到Excel表文件
					rownum = 0;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}							
		}
		
	}

}
