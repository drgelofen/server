package server.lib.orm.stmt.query;

import server.lib.orm.db.DatabaseType;
import server.lib.orm.stmt.ArgumentHolder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * For operations with a number of them in a row.
 *
 * @author graywatson
 */
//  TODO: MODIFIED
public class ManyClause implements Clause, NeedsFutureClause {

    private final Clause first;
    private Clause second;
    private final Clause[] others;
    private final int startOthersAt;
    private final Operation operation;

    public ManyClause(Clause first, Operation operation) {
        this.first = first;
        // second will be set later
        this.second = null;
        this.others = null;
        this.startOthersAt = 0;
        this.operation = operation;
    }

    public ManyClause(Clause first, Clause second, Clause[] others, Operation operation) {
        this.first = first;
        this.second = second;
        this.others = others;
        this.startOthersAt = 0;
        this.operation = operation;
    }

    public ManyClause(Clause[] others, Operation operation) {
        this.first = others[0];
        if (others.length < 2) {
            this.second = null;
            this.startOthersAt = others.length;
        } else {
            this.second = others[1];
            this.startOthersAt = 2;
        }
        this.others = others;
        this.operation = operation;
    }


    @Override
    public void setMissingClause(Clause right) {
        second = right;
    }

    @Override
    public void appendSql(DatabaseType databaseType, String tableName, StringBuilder sb, List<ArgumentHolder> selectArgList, Clause outer) throws SQLException {
        boolean outerSame = (outer instanceof ManyClause && ((ManyClause) outer).operation == operation);
        if (first != null) {
            if (!outerSame) {
                sb.append('(');
            }
            first.appendSql(databaseType, tableName, sb, selectArgList, this);
            if (second != null) {
                sb.append(operation.sql);
                sb.append(' ');
                second.appendSql(databaseType, tableName, sb, selectArgList, this);
            }
            if (others != null) {
                for (int i = startOthersAt; i < others.length; i++) {
                    sb.append(operation.sql);
                    sb.append(' ');
                    others[i].appendSql(databaseType, tableName, sb, selectArgList, this);
                }
            }
        } else if (others != null && others.length > 1) {
            StringBuilder temp = new StringBuilder();
            for (int i = startOthersAt; i < others.length; i++) {
                if (i > 0) {
                    temp.append(operation.sql);
                }
                temp.append(' ');
                others[i].appendSql(databaseType, tableName, temp, selectArgList, this);
            }
            char[] chars = temp.reverse().toString().toCharArray();
            ArrayList<Character> characters = new ArrayList<>();
            for (char c : chars) {
                characters.add(c);
            }
            boolean found = false;
            int route = 0;
            int index = 0;
            int count = 0;
            int joined = 0;
            StringBuilder edited = new StringBuilder();
            for (Character c : characters) {
                if (found) {
                    edited.append(c);
                    if (c == '"' && joined == 0) {
                        if (characters.get(index + 1) == '.' && characters.get(index + 3) == ' ' && (characters.get(index + 2) == 'X' || characters.get(index + 2) == 'Y')) {
                            joined++;
                        } else {
                            if (count > 0 && count < 2) {
                                count++;
                            } else {
                                if (characters.get(index + 1) != '.' || count >= 2) {
                                    route++;
                                    if (route == 2) {
                                        edited.append('(');
                                    }
                                } else {
                                    count++;
                                }
                            }
                        }
                    } else if (joined > 0) {
                        joined++;
                        if (joined == 3) {
                            edited.append('(');
                        }
                    }
                } else {
                    if (c != ')') {
                        edited.append(c);
                    } else {
                        found = true;
                    }
                }
                index++;
            }
            if (found) {
                edited.reverse().append(") ");
            } else {
                edited.append("(").reverse();
            }
            sb.append(edited);
        }
        if (first != null) {
            if (!outerSame) {
                // cut off a trailing space if there is one
                int len = sb.length();
                if (len > 0 && sb.charAt(len - 1) == ' ') {
                    sb.setLength(len - 1);
                }
                sb.append(") ");
            }
        } else {
            sb.append(") ");
        }
    }

    /**
     * Type of operation for the many clause.
     */
    public static enum Operation {
        AND("AND"),
        OR("OR"),
        // end
        ;
        public final String sql;

        private Operation(String sql) {
            this.sql = sql;
        }
    }
}
