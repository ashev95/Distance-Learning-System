package com.dls.base.reports;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataSourceProvider;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.base.JRBaseField;

public class TestReportDataProvider implements JRDataSourceProvider {


	private class MyField extends JRBaseField{

		private static final long serialVersionUID = -5570289821891736393L;

		public MyField(String name,String description, Class<?> type){
			this.name = name;
			this.description = description;
			this.valueClass = type;
			this.valueClassName = type.getName();
		}

		public MyField(String name,String description){
			this(name, description, String.class);
		}
	}

	@Override
	public JRDataSource create(JasperReport arg0) throws JRException {
		return new MyImplementation();
	}

	@Override
	public void dispose(JRDataSource arg0) throws JRException {
	}

	@Override
	public boolean supportsGetFieldsOperation() {
		return true;
	}

	@Override
	public JRField[] getFields(JasperReport arg0) throws JRException,
			UnsupportedOperationException {
		JRField field1 = new MyField("Name","The name of an employee");
		JRField field2 = new MyField("Age","The age of an employee", Integer.class);
		return new JRField[]{field1, field2};
	}

}