package com.example.basic.modules.excel.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.basic.common.exception.GlobalExceptionHandler.BizException;
import com.example.basic.common.result.ResultCode;
import com.example.basic.modules.excel.service.ExcelService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Excel 导入/导出服务实现。
 *
 * <p>基于 Hutool ExcelUtil（底层封装 Apache POI）。
 *
 * @author hermes-agent
 */
@Slf4j
@Service
public class ExcelServiceImpl implements ExcelService {

    private static final int DEFAULT_SHEET_SIZE = 100000;

    @Override
    public <T> void export(List<T> data, HttpServletResponse response, String fileName) {
        export(data, response, fileName, "Sheet1");
    }

    @Override
    public <T> void export(List<T> data, HttpServletResponse response, String fileName, String sheetName) {
        if (CollUtil.isEmpty(data)) throw new BizException(ResultCode.BAD_REQUEST, "导出数据不能为空");
        try (ExcelWriter writer = ExcelUtil.getWriter(sheetName)) {
            String enc = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + enc + ".xlsx");
            writer.write(data, true);
            writer.flush(response.getOutputStream());
            log.info("Excel 导出成功 | fileName={} | rows={}", fileName, data.size());
        } catch (Exception e) {
            log.error("Excel 导出失败: {}", e.getMessage());
            throw new BizException(ResultCode.SYSTEM_ERROR, "Excel 导出失败");
        }
    }

    @Override
    public <T> void exportToFile(List<T> data, String filePath, String sheetName) {
        if (CollUtil.isEmpty(data)) throw new BizException(ResultCode.BAD_REQUEST, "导出数据不能为空");
        try (ExcelWriter writer = ExcelUtil.getWriter(filePath, sheetName)) {
            writer.write(data, true);
            log.info("Excel 导出到文件 | path={} | rows={}", filePath, data.size());
        } catch (Exception e) {
            throw new BizException(ResultCode.SYSTEM_ERROR, "文件写入失败: " + e.getMessage());
        }
    }

    @Override
    public <T> void exportLargeData(List<T> data, String filePath, String sheetName, int sheetSize) {
        if (CollUtil.isEmpty(data)) throw new BizException(ResultCode.BAD_REQUEST, "导出数据不能为空");
        sheetSize = sheetSize > 0 ? sheetSize : DEFAULT_SHEET_SIZE;
        try (ExcelWriter writer = ExcelUtil.getWriter(filePath)) {
            int total = data.size();
            int sheets = (total + sheetSize - 1) / sheetSize;
            for (int i = 0; i < sheets; i++) {
                int from = i * sheetSize;
                int to = Math.min(from + sheetSize, total);
                String name = sheets > 1 ? sheetName + "_" + (i+1) : sheetName;
                writer.setSheet(name);
                writer.write(data.subList(from, to), true);
                log.info("大数据导出 | sheet={} | rows={}", name, to - from);
            }
            writer.flush();
            log.info("大数据量导出完成 | totalRows={} | sheets={}", total, sheets);
        } catch (Exception e) {
            throw new BizException(ResultCode.SYSTEM_ERROR, "导出失败: " + e.getMessage());
        }
    }

    @Override
    public <T> List<T> importExcel(Class<T> clazz, File file, int headerRow) {
        try {
            ExcelReader reader = ExcelUtil.getReader(file);
            reader.setHeaderRow(headerRow);
            List<T> rows = reader.read(0, 1, clazz);
            reader.close();
            log.info("Excel 导入 | path={} | rows={}", file.getPath(), rows.size());
            return rows;
        } catch (Exception e) {
            log.error("Excel 导入失败: {}", e.getMessage());
            throw new BizException(ResultCode.SYSTEM_ERROR, "Excel 导入失败: " + e.getMessage());
        }
    }

    @Override
    public <T> List<T> importExcel(Class<T> clazz, HttpServletRequest request, int headerRow) {
        try {
            ExcelReader reader = ExcelUtil.getReader(request.getInputStream());
            reader.setHeaderRow(headerRow);
            List<T> rows = reader.read(0, 1, clazz);
            reader.close();
            return rows;
        } catch (Exception e) {
            throw new BizException(ResultCode.SYSTEM_ERROR, "Excel 导入失败: " + e.getMessage());
        }
    }
}
