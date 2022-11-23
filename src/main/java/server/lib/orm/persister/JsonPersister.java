package server.lib.orm.persister;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import server.lib.orm.field.FieldType;
import server.lib.orm.field.SqlType;
import server.lib.orm.field.types.LongStringType;

public class JsonPersister extends LongStringType {

    private static final JsonPersister INSTANCE = new JsonPersister();

    private JsonPersister() {
        super(SqlType.LONG_STRING, new Class<?>[]{JsonElement.class});
    }

    public static JsonPersister getSingleton() {
        return INSTANCE;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        if (javaObject instanceof String) {
            return javaObject;
        }
        JsonElement model = (JsonElement) javaObject;
        return model != null ? model.toString() : null;
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        return sqlArg != null ? JsonParser.parseString((String) sqlArg) : null;
    }
}
