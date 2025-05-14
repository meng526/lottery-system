package com.example.lotterysystem.dao.handler;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.example.lotterysystem.dao.dataobject.Encrypt;
import org.apache.ibatis.type.*;

import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
@MappedTypes(Encrypt.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class EncryptTypeHandler extends BaseTypeHandler<Encrypt> {
    private static final byte[] KEY = "123456789abcdefg".getBytes(StandardCharsets.UTF_8);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Encrypt parameter, JdbcType jdbcType) throws SQLException {
        if(null==parameter){
            ps.setString(i,null);
            return;
        }
        AES aes = SecureUtil.aes(KEY);
        ps.setString(i,aes.encryptHex(parameter.getValue()));

    }

    @Override
    public Encrypt getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decrypt(rs.getString(columnName));
    }

    @Override
    public Encrypt getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decrypt(rs.getString(columnIndex));
    }

    @Override
    public Encrypt getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(cs.getString(columnIndex));
    }

    private Encrypt decrypt(String str){
        if(null==str){
            return null;
        }

        return new Encrypt(SecureUtil.aes(KEY).decryptStr(str));
    }

}
