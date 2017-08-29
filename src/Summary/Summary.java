package Summary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import SubThread.SubThread;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;

public class Summary
{
	// 汇总数据以及生成输出文件的方法。
	@SuppressWarnings("unused")
	public static void outPutData(String dir, String Path, int Upload, String PutPath, int Cover, String oldfileday)
	{
		System.out.println();
		Calendar now_star = Calendar.getInstance();
		SimpleDateFormat formatter_star = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println("Summary程序开始时间: " + formatter_star.format(now_star.getTime()));
		System.out.println("===============================================");

		SimpleDateFormat formatter_Date = new SimpleDateFormat("yyyyMMdd");
		String Day = formatter_Date.format(now_star.getTime());
		String Batman_File = dir + "/" + "Batman_All_" + Day + ".xlsx";
		String old_file_dir = "./oldExcel/" + oldfileday;

		// 创建目录输出
		SubThread.my_mkdir(dir);

		SubThread.createXlsx(new File(Batman_File));

		ArrayList<String> Batman_file_list = new ArrayList<String>(); // Batman文件列表
		ArrayList<String> Batman_Data_List = new ArrayList<String>(); // 血浆数据列表
		ArrayList<String> Plasma_Porject_File_List = new ArrayList<String>(); // 血浆项目文件列表
		ArrayList<String> Tissue_Porject_File_List = new ArrayList<String>(); // 组织项目文件列表
		ArrayList<String> BC_Porject_File_List = new ArrayList<String>(); // 白细胞项目文件列表
		ArrayList<String> All_File_Path = new ArrayList<String>(); // 所有WM。*。stat格式文件的路径列表
		ArrayList<String> new_porjaect_data = new ArrayList<String>(); // 新项目数据列表
		ArrayList<String> old_porjaect_data = new ArrayList<String>(); // 旧前项目数据列表
		ArrayList<String> old_file_list = new ArrayList<String>(); // 旧文件列表
		ArrayList<String> new_file_list = new ArrayList<String>(); // 新文件列表
		ArrayList<String> updata_file_list = new ArrayList<String>(); // 被更新的文件列表
		ArrayList<String> mergeExcelData_list = new ArrayList<String>(); // 项目文件数据列表
		ArrayList<String> mergeOldData_list = new ArrayList<String>(); // 项目文件数据列表
		ArrayList<String> Upload_All_File_List = new ArrayList<String>(); // 需要上传的文件列表

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String day = formatter.format(now_star.getTime()); // 格式化后的日期
		String cmd = "find " + Path + " -type f -name *_" + day + "*.xlsx";
		Batman_file_list = Linux_Cmd(cmd); // 调用linux命令获取Batman文件列表
		
		String oldfile_cmd = "find " + old_file_dir + " -type f -name *.xlsx";
		old_file_list = Linux_Cmd(oldfile_cmd); // 调用linux命令获取旧文件列表

		// 追踪总表
		/*for (int i = 0; i < Batman_file_list.size(); i++) {
			readExcelData(new File(Batman_file_list.get(i)), Batman_Data_List);
			//System.out.println(Batman_file_list.get(i));
			
		}
		writeExcelData(new File(Batman_File), Batman_Data_List);
		Upload_All_File_List.add(Batman_File);*/
		
		// 追踪总表
		if (Batman_file_list.size() > 1) {
			mergeOldData_list.clear();
			for (int i = 0; i < Batman_file_list.size(); i++) {
				//readExcelData(new File(Plasma_File_List.get(i)), Plasma_Data_List);
				//System.out.println(Plasma_File_List.get(i));
				mergeExcelData_list.clear();
				readExcelData(new File(Batman_file_list.get(i)), mergeExcelData_list);
				if (mergeOldData_list.size() == 0) {
					mergeOldData_list.addAll(mergeExcelData_list);
					continue;
				} else {
					if (mergeExcelData_list.size() == 0) {
						if (i == Batman_file_list.size()-1 && Batman_Data_List.isEmpty()) {
							Batman_Data_List.addAll(mergeOldData_list);
						}
						continue;
					} else {
						Batman_Data_List.clear();
						mergeExcelData(mergeExcelData_list, mergeOldData_list, Batman_Data_List);
						mergeOldData_list.clear();
						mergeOldData_list.addAll(Batman_Data_List);
					}
				}
			}
		} else if(Batman_file_list.size() == 1) {
			readExcelData(new File(Batman_file_list.get(0)), Batman_Data_List);
		}
		
		if (Cover == 1) {
			old_porjaect_data.clear();
			for (int i = 0; i < old_file_list.size(); i++) {
				//System.out.println(old_file_list.get(i));
				String old_File_name = "Batman_All_";
				if (new File(old_file_list.get(i)).getName().startsWith(old_File_name)) {
					readExcelData(new File(old_file_list.get(i)), old_porjaect_data);
					updata_file_list.add(old_file_list.get(i));
					//System.out.println(old_file_list.get(i));
					break;
				}
			}
			if (old_porjaect_data.size() != 0) {
				//System.out.println("******");
				updataExcelData(new File(Batman_File), Batman_Data_List, old_porjaect_data);
			}
		} else {
			// 新建文件，达到清空所有数据行的效果
			SubThread.createXlsx(new File(Batman_File));
			writeExcelData(new File(Batman_File), Batman_Data_List);
		}
		
		Upload_All_File_List.add(Batman_File);
		
		// 上传文件到wdmycloud
		if (Upload == 1) {
			for (int i = 0; i < Upload_All_File_List.size(); i++) {
				int y = uploadFileToWdmycloud(Upload_All_File_List.get(i), PutPath);
				if (y != 0) {
					break;
				}
			}
		}

		Calendar now_end = Calendar.getInstance();
		SimpleDateFormat formatter_end = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println();
		System.out.println("==============================================");
		System.out.println("Summary程序结束时间: " + formatter_end.format(now_end.getTime()));
		System.out.println();
	}
	
	/**
	 * 调用linux命令的方法。
	 * 
	 * @param cmd
	 * @return
	 */
	public static ArrayList<String> Linux_Cmd(String cmd)
	{
		ArrayList<String> Data_list = new ArrayList<String>();
		String line = null;
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((line = input.readLine()) != null) {
				Data_list.add(line);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Data_list;
	}
	
	/**
	 * 读表Excel数据到列表，去除重复行
	 * 
	 * @param file
	 * @param Data_list
	 */
	@SuppressWarnings("deprecation")
	public static void readExcelData(File file, ArrayList<String> Data_list)
	{
		String TemplateData = null;
		String data = null;
		if (file.getName().startsWith("._") || file.getName().startsWith("~$")) {
			System.out.println("无效文件： " + file.getParent() + "/" + file.getName());
			return;
		} else {
			try {
				FileInputStream is = new FileInputStream(file);
				XSSFWorkbook wb = new XSSFWorkbook(is);
				XSSFSheet sheet = wb.getSheetAt(0); // 获取第1个工作薄
	
				XSSFRow xssfrow0 = sheet.getRow(0);
				for (int j = xssfrow0.getFirstCellNum(); j < xssfrow0.getLastCellNum(); j++) {
					if (j == xssfrow0.getFirstCellNum()) {
						TemplateData = "null";
					} else {
						TemplateData += "\t" + "null";
					}
				}
				// 获取当前工作薄的每一行
				for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
					String TemplateArr[] = TemplateData.split("\t");
					XSSFRow xssfrow = sheet.getRow(i);
					// 获取当前工作薄的每一列
					for (int j = xssfrow.getFirstCellNum(); j < xssfrow.getLastCellNum(); j++) {
						XSSFCell xssfcell = xssfrow.getCell(j);
						if (xssfcell == null || (("") == String.valueOf(xssfcell)) || xssfcell.toString().equals("")
								|| xssfcell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
							continue;
						} else {
							String cellValue = String.valueOf(xssfcell);
							TemplateArr[j] = cellValue;
						}
					}
					for (int x = 0; x < TemplateArr.length; x++) {
						if (x == 0) {
							data = TemplateArr[x];
						} else {
							data += "\t" + TemplateArr[x];
						}
					}
					if (Data_list.contains(data)) {
						continue;
					} else {
						Data_list.add(data);
					}
				}
				is.close();
				wb.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 写数据到Excel表文件
	 * 
	 * @param file
	 * @param Data_list
	 */
	public static void writeExcelData(File file, ArrayList<String> Data_list)
	{
		try {
			FileInputStream is = new FileInputStream(file);
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			XSSFSheet sheet = workbook.getSheetAt(0); // 获取第1个工作薄
			// 写回数据
			for (int j = 0; j < Data_list.size(); j++) {
				XSSFRow row = sheet.createRow((short) sheet.getLastRowNum() + 1);
				String str_row[] = Data_list.get(j).split("\t");
				for (int i = 0; i < str_row.length; i++) {
					// 在索引0的位置创建单元格（左上端）
					XSSFCell cell = row.createCell(i);
					if (str_row[i].equals("null")) {
						cell.setCellValue("");
					} else {
						cell.setCellValue(str_row[i]);
					}
				}
			}
			// 新建一输出文件流
			FileOutputStream fOut = new FileOutputStream(file);
			// 把相应的Excel 工作簿存盘
			workbook.write(fOut);
			fOut.flush();
			// 操作结束，关闭文件
			fOut.close();
			is.close();
			workbook.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 更新Excel表文件数据
	 * 
	 * @param file
	 * @param Data_list
	 */
	public static void mergeExcelData(ArrayList<String> new_data, ArrayList<String> old_data, ArrayList<String> updata_data)
	{
		//ArrayList<String> old_data = new ArrayList<String>();
		ArrayList<String> old_save = new ArrayList<String>();
		// 对比新旧数据
		for (int j = 0; j < new_data.size(); j++) {
			String str_new[] = new_data.get(j).split("\t");
			/*if (j == 0) {
				System.out.println(str_new[1] + "\t" + str_new[26]);
			}*/
			int log4 = 0;
			for (int i = 0; i < old_data.size(); i++) {
				int log1 = 0;
				int log2 = 0;
				int log3 = 0;
				String str_old[] = old_data.get(i).split("\t");
				for (int k = 0; k < str_old.length; k++) {
					if (k == 38) {
						log2 = 1;
						break;
					} else {
						if (str_old[k].equals(str_new[k])) {
							log4 = 1;
							//System.out.println(str_old[k] + "==/////==" + str_new[k]);
							continue;
						} else {
							if (str_old[k].equals("NA")) {
								//System.out.println(str_old[k] + "==*****==" + str_new[k]);
								str_old[k] = str_new[k];
								log3 = 1;
								//System.out.println(str_old[k] + "==||||||||||||||==" + str_new[k]);
								continue;
							} else if (str_new[k].equals("NA")) {
								//System.out.println(str_old[k] + "===" + str_new[k]);
								continue;
							} else {
								if (k > 0) {
									log1 = 1;
								}								
								break;
							}
						}
					}
				}
				if (log1 == 1) {
					if (!updata_data.contains(old_data.get(i))) {
						updata_data.add(old_data.get(i));					
					}
					if (!updata_data.contains(new_data.get(j))) {
						updata_data.add(new_data.get(j));						
					}
					continue;
				}
				if (log2 == 1) {
					//System.out.println("log2 == 1 ");
					if (log3 == 1) {
						//System.out.println("log3 == 1 ");
						String data = null;
						for (int x = 0; x < str_old.length; x++) {
							if (x == 0) {
								data = str_old[x];
							} else {
								data += "\t" + str_old[x];
							}
						}
						//System.out.println("data ==== " + data);
						//System.out.println("/////////////////////////");
						if (!updata_data.contains(data)) {
							updata_data.add(data);
						}
					} else {
						if (!updata_data.contains(old_data.get(i))) {
							updata_data.add(old_data.get(i));
						}
					}
					continue;
				}
			}
			if (log4 == 0) {
				if (!updata_data.contains(new_data.get(j))) {
					updata_data.add(new_data.get(j));
				}
			}
		}
		
		old_save.clear();
		for (int j = 0; j < old_data.size(); j++) {
			String str_old[] = old_data.get(j).split("\t");
			int log1 = 0;
			for (int i = 0; i < updata_data.size(); i++) {
				String str_updata[] = updata_data.get(i).split("\t");
				if (str_old[0].equals(str_updata[0])) {
					log1 = 1;
					break;
				} else {
					continue;
				}
			}
			if (log1 == 0) {
				if (!old_save.contains(old_data.get(j))) {
					old_save.add(old_data.get(j));
				}
				continue;
			}
		}		
		updata_data.addAll(old_save);
	}

	
	/**
	 * 更新Excel表文件数据
	 * 
	 * @param file
	 * @param Data_list
	 */
	public static void updataExcelData(File file, ArrayList<String> new_data, ArrayList<String> old_data)
	{
		ArrayList<String> old_save = new ArrayList<String>();
		ArrayList<String> updata_data = new ArrayList<String>();
		ArrayList<String> final_data = new ArrayList<String>();
		// 对比新旧数据
		for (int j = 0; j < new_data.size(); j++) {
			String str_new[] = new_data.get(j).split("\t");
			int log4 = 0;
			for (int i = 0; i < old_data.size(); i++) {
				int log1 = 0;
				int log2 = 0;
				int log3 = 0;
				String str_old[] = old_data.get(i).split("\t");
				for (int k = 0; k < str_old.length; k++) {
					if (k == 38) {
						log2 = 1;
						break;
					} else {
						if (str_old[k].equals(str_new[k])) {
							log4 = 1;
							continue;
						} else {
							if (str_old[k].equals("NA")) {
								//System.out.println(str_old[k] + "==*****==" + str_new[k]);
								str_old[k] = str_new[k];
								log3 = 1;
								//System.out.println(str_old[k] + "==||||||||||||||==" + str_new[k]);
								continue;
							} else if (str_new[k].equals("NA")) {
								//System.out.println(str_old[k] + "===" + str_new[k]);
								continue;
							} else {
								if (k > 0) {
									log1 = 1;
								}
								break;
							}
						}
					}
				}
				if (log1 == 1) {
					if (!updata_data.contains(old_data.get(i))) {
						updata_data.add(old_data.get(i));
					}
					if (!updata_data.contains(new_data.get(j))) {
						updata_data.add(new_data.get(j));
					}
					continue;
				}
				if (log2 == 1) {
					//System.out.println("log2 == 1 ");
					if (log3 == 1) {
						//System.out.println("log3 == 1 ");
						String data = null;
						for (int x = 0; x < str_old.length; x++) {
							if (x == 0) {
								data = str_old[x];
								continue;
							} else {
								data += "\t" + str_old[x];
								continue;
							}
						}
						//System.out.println("data ==== " + data);
						//System.out.println("/////////////////////////");
						if (!updata_data.contains(data)) {
							updata_data.add(data);
						}
					} else {
						if (!updata_data.contains(old_data.get(i))) {
							updata_data.add(old_data.get(i));
						}
					}
					continue;
				}
			}
			if (log4 == 0) {
				if (!updata_data.contains(new_data.get(j))) {
					updata_data.add(new_data.get(j));
					//System.out.println(new_data.get(j));
				}
			}
		}
		
		//添加新表里没有的数据
		old_save.clear();
		for (int j = 0; j < old_data.size(); j++) {
			String str_old[] = old_data.get(j).split("\t");
			int log1 = 0;
			for (int i = 0; i < updata_data.size(); i++) {
				String str_updata[] = updata_data.get(i).split("\t");
				if (str_old[0].equals(str_updata[0])) {
					log1 = 1;
					break;
				} else {
					continue;
				}
			}
			if (log1 == 0) {
				if (!old_save.contains(old_data.get(j))) {
					old_save.add(old_data.get(j));
				}
				continue;
			}
		}	
		updata_data.addAll(old_save);
		
		//对数据做标记
		for (int j = 0; j < updata_data.size(); j++) {
			String str_up[] = updata_data.get(j).split("\t");
			int log1 = 0;
			for (int i = 0; i < new_data.size(); i++) {
				String str_new[] = new_data.get(i).split("\t");
				if (str_new[1].equals(str_up[1])) {
					log1 = 1;
					break;
				} else {
					continue;
				}
			}
			if (log1 == 0) {
				str_up[38] = "该记录不存在"; //做标记
				String data = null;
				for (int x = 0; x < str_up.length; x++) {
					if (x == 0) {
						data = str_up[x];
					} else {
						data += "\t" + str_up[x];
					}
				}
				if (!final_data.contains(data)) {
					final_data.add(data);
				}
				continue;
			} else {
				str_up[38] = ""; //清空标记
				String data = null;
				for (int x = 0; x < str_up.length; x++) {
					if (x == 0) {
						data = str_up[x];
					} else {
						data += "\t" + str_up[x];
					}
				}
				if (!final_data.contains(data)) {
					final_data.add(data);
				}
				continue;
			}
		}
		
		SubThread.createXlsx(file); // 创建新的文件，达到清除数据效果		
		try {
			FileInputStream is = new FileInputStream(file);
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			XSSFSheet sheet = workbook.getSheetAt(0); // 获取第1个工作薄
			// 写回数据
			for (int j = 0; j < final_data.size(); j++) {			
				XSSFRow row = sheet.createRow((short) sheet.getLastRowNum() + 1);
				String str_row[] = final_data.get(j).split("\t");
				for (int i = 0; i < str_row.length; i++) {
					// 在索引0的位置创建单元格（左上端）
					XSSFCell cell = row.createCell(i);
					if (str_row[i].equals("null")) {
						cell.setCellValue("");
					} else {
						cell.setCellValue(str_row[i]);
					}
				}
			}
			// 新建一输出文件流
			FileOutputStream fOut = new FileOutputStream(file);
			// 把相应的Excel 工作簿存盘
			workbook.write(fOut);
			fOut.flush();
			// 操作结束，关闭文件
			fOut.close();
			is.close();
			workbook.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 調用ssh的方法，若ssh过程中拋出异常，程序自动修复，但ssh连续申请链接1000次都没有成功，程序直接退出执行！
	 * 
	 * @param filename
	 * @param PutPath
	 */
	public static int uploadFileToWdmycloud(String filename, String PutPath)
	{
		int x = 0;
		while (true) {
			try {
				sshfun(filename, PutPath);
				if (x != 0) {
					System.out.println();
					System.out.println("ssh过程中拋出异常，但程序已自动修复成功！ ");
					x = 0;
				}
				break;
			} catch (Exception e) {
				e.printStackTrace();
				x++;		
			}
			if (x == 100) {
				System.out.println();
				System.out.println("ssh连续申请链接100次都没有成功，程序直接退出执行！");
				return -1;
			} else {
				System.out.println();
				System.out.println("ssh过程中第" + x +"次拋出异常，但程序正在尝试自动修复！ ");
				continue;
			}		
		}
		return 0;
	}
	
	/**
	 * 用SSh上传文件到wdmycloud上的方法
	 * 
	 * @param filename
	 * @param PutPath
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	public static void sshfun(String filename, String PutPath) throws Exception
	{
		String user = "zhirong_lu";
		String pass = "zhirong_lu";
		String host = "192.192.192.220";
		int port = 22;
		if (!(new File(PutPath).exists()) && !(new File(PutPath).isDirectory())) {
			String command = "mkdir " + PutPath;
			JSch jsch = new JSch();
			// 创建session并且打开连接，因为创建session之后要主动打开连接
			Session session = jsch.getSession(user, host, port);
			Hashtable<String, String> config = new Hashtable<String, String>();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setPassword(pass);
			session.connect();
			// 打开通道，设置通道类型，和执行的命令
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(command);
			channelExec.setInputStream(null);
			BufferedReader input = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
			channelExec.connect();
			// 接收远程服务器执行命令的结果 
			String line = null;
			while ((line = input.readLine()) != null) {
			} // 循环读出系统调用返回值，保证脚本调用正常完成
			input.close(); 
			channelExec.disconnect();
			session.disconnect();
		}
		Thread.sleep(1000);

		Connection con = new Connection(host);
		con.connect();
		boolean isAuthed = con.authenticateWithPassword(user, pass);
		SCPClient scpClient = con.createSCPClient();
		scpClient.put(filename, PutPath); // 从本地复制文件到远程目录
		con.close();
	}
	
}
