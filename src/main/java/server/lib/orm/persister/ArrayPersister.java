package server.lib.orm.persister;

import org.apache.commons.lang3.StringUtils;
import server.lib.orm.field.FieldType;
import server.lib.orm.field.SqlType;
import server.lib.orm.field.types.LongStringType;
import server.lib.utils.StringUtil;

import java.util.UUID;

public class ArrayPersister extends LongStringType {

    private static final ArrayPersister singleTon = new ArrayPersister();
    private static final String DELIMITER = ",";

    private ArrayPersister() {
        super(SqlType.LONG_STRING, new Class<?>[]{String[].class});
    }

    public static ArrayPersister getSingleton() {
        return singleTon;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        if (javaObject == null) {
            return null;
        }
        if (javaObject instanceof UUID) {
            return javaObject.toString();
        } else if (javaObject instanceof String) {
            return javaObject;
        }
        String[] array = {};
        if (javaObject instanceof UUID[]) {
            array = new String[((UUID[]) javaObject).length];
            int counter = 0;
            for (UUID uuid : (UUID[]) javaObject) {
                array[counter] = uuid != null ? uuid.toString() : null;
                counter++;
            }
        } else if (javaObject instanceof String[]) {
            array = (String[]) javaObject;
        }
        return StringUtils.join(array, DELIMITER);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        if (sqlArg == null) {
            return null;
        }
        String string = (String) sqlArg;
        String[] data = string.split(DELIMITER);
        if (data != null && data.length > 0) {
            UUID[] uuids = new UUID[data.length];
            int counter = 0;
            for (String str : data) {
                if (!StringUtil.isUUID(str)) {
                    return data;
                }
                uuids[counter] = UUID.fromString(str);
                counter++;
            }
            return uuids;
        }
        return data;
    }
}
