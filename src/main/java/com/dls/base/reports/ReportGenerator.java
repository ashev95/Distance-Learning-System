package com.dls.base.reports;

import java.io.File;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.dls.base.utils.HashMapBuilder;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager; 
import net.sf.jasperreports.engine.JasperExportManager; 
import net.sf.jasperreports.engine.JasperFillManager; 
import net.sf.jasperreports.engine.JasperPrint; 
import net.sf.jasperreports.engine.JasperReport; 
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource; 
import net.sf.jasperreports.engine.design.JasperDesign; 
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public class ReportGenerator {

    public File create(String saveFilePath, String patternFilePath, HashMap<String, Object> params, ArrayList<Object> data) throws JRException, SQLException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(patternFilePath);
        JasperDesign jasperDesign = JRXmlLoader.load(in);
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,
                params,
                new JRBeanCollectionDataSource(data));
        JasperExportManager.exportReportToPdfFile(jasperPrint, saveFilePath);
        return new File(saveFilePath);
    }
} 