package server.lib.orm.field.types;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import server.lib.orm.field.FieldType;
import server.lib.orm.field.SqlType;

/**
 * Base class for all of the {@link java.sql.Date} class types.
 * 
 * @author graywatson
 */
public abstract class BaseDateType extends BaseDataType {

	protected static final DateStringFormatConfig defaultDateFormatConfig =
			new DateStringFormatConfig("yyyy-MM-dd HH:mm:ss.SSSSSS");

	protected BaseDateType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	protected BaseDateType(SqlType sqlType) {
		super(sqlType);
	}

	protected static DateStringFormatConfig convertDateStringConfig(FieldType fieldType,
			DateStringFormatConfig defaultDateFormatConfig) {
		if (fieldType == null) {
			return defaultDateFormatConfig;
		}
		DateStringFormatConfig configObj = (DateStringFormatConfig) fieldType.getDataTypeConfigObj();
		if (configObj == null) {
			return defaultDateFormatConfig;
		} else {
			return (DateStringFormatConfig) configObj;
		}
	}

	protected static Date parseDateString(DateStringFormatConfig formatConfig, String dateStr) throws ParseException {
		DateFormat dateFormat = formatConfig.getDateFormat();
		return dateFormat.parse(dateStr);
	}

	protected static String normalizeDateString(DateStringFormatConfig formatConfig, String dateStr)
			throws ParseException {
		DateFormat dateFormat = formatConfig.getDateFormat();
		Date date = dateFormat.parse(dateStr);
		return dateFormat.format(date);
	}

	protected static class DateStringFormatConfig {
		private final String dateFormatStr;
		// used with clone
		private final DateFormat dateFormat;

		public DateStringFormatConfig(String dateFormatStr) {
			this.dateFormatStr = dateFormatStr;
			this.dateFormat = new SimpleDateFormat(dateFormatStr);
		}

		public DateFormat getDateFormat() {
			return (DateFormat) dateFormat.clone();
		}

		@Override
		public String toString() {
			return dateFormatStr;
		}
	}

	@Override
	public boolean isValidForVersion() {
		return true;
	}

	@Override
	public Object moveToNextValue(Object currentValue) {
		long newVal = System.currentTimeMillis();
		if (currentValue == null) {
			return new Date(newVal);
		} else if (newVal == ((Date) currentValue).getTime()) {
			return new Date(newVal + 1L);
		} else {
			return new Date(newVal);
		}
	}

	@Override
	public boolean isValidForField(Field field) {
		return (field.getType() == Date.class);
	}
}
