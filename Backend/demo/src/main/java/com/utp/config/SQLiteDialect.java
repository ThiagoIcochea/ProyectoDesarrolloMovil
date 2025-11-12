package com.utp.config;

import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitOffsetLimitHandler;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;

public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super();
    }

    // --- Soporte de columnas autoincrementales ---
    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new SQLiteIdentityColumnSupport();
    }

    public boolean supportsIdentityColumns() {
        return true;
    }

    public boolean hasAlterTable() {
        return false;
    }

    public boolean dropConstraints() {
        return false;
    }

    public boolean supportsCascadeDelete() {
        return true;
    }

    public boolean supportsTemporaryTables() {
        return true;
    }

    public String getCreateTemporaryTableString() {
        return "create temporary table if not exists";
    }

    public boolean dropTemporaryTableAfterUse() {
        return false;
    }

    public boolean supportsUnionAll() {
        return true;
    }

    public NameQualifierSupport getNameQualifierSupport() {
        return NameQualifierSupport.NONE;
    }

    public LimitHandler getLimitHandler() {
        return LimitOffsetLimitHandler.INSTANCE;
    }

    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    public String getAddColumnString() {
        return "add column";
    }

    public boolean supportsInsertSelectIdentity() {
        return true;
    }
}

// Clase auxiliar para manejo de autoincremento en SQLite
class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {

    @Override
    public boolean supportsIdentityColumns() {
        return true;
    }

    @Override
    public String getIdentitySelectString(String table, String column, int type)
            throws MappingException {
        return "select last_insert_rowid()";
    }

    @Override
    public String getIdentityColumnString(int type) throws MappingException {
        return "integer primary key autoincrement not null";
    }
}
