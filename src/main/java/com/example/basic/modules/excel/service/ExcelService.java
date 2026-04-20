package com.example.basic.modules.excel.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

/**
 * Excel 导入/导出服务接口。
 *
 * @author hermes-agent
 */
public interface ExcelService {

    /** 导出 List 为 Excel */
    <T> void export(List<T> data, HttpServletResponse response, String fileName);

    /** 导出（指定 Sheet 名） */
    <T> void export(List<T> data, HttpServletResponse response, String fileName, String sheetName);

    /** 导出到文件 */
    <T> void exportToFile(List<T> data, String filePath, String sheetName);

    /** 大数据量导出（自动分 Sheet） */
    <T> void exportLargeData(List<T> data, String filePath, String sheetName, int sheetSize);

    /** 导入 Excel → List */
    <T> List<T> importExcel(Class<T> clazz, File file, int headerRow);

    /** 导入（HTTP 上传文件） */
    <T> List<T> importExcel(Class<T> clazz, HttpServletRequest request, int headerRow);
}
