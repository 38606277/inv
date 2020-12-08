package root.report.db;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class GregorianCalendarTypeHandle implements TypeHandler<XMLGregorianCalendar> {
    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, XMLGregorianCalendar calendar, JdbcType jdbcType) throws SQLException {
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        preparedStatement.setString(i, simpleDateFormat.format(calendar.toGregorianCalendar().getTime()));
    }

    @Override
    public XMLGregorianCalendar getResult(ResultSet resultSet, String s) throws SQLException {
        return null;
    }

    @Override
    public XMLGregorianCalendar getResult(ResultSet resultSet, int i) throws SQLException {
        return null;
    }

    @Override
    public XMLGregorianCalendar getResult(CallableStatement callableStatement, int i) throws SQLException {
        return null;
    }
}
