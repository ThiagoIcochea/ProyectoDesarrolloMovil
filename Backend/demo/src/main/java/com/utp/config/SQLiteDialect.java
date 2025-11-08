package com.utp.config;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitOffsetLimitHandler;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;

import java.sql.Types;

/**
 * Dialecto funcional para SQLite compatible con Hibernate 6.5+
 */
public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super();
    }

    // --- Configuración básica de SQLite ---
    public LimitHandler getLimitHandler() {
        return LimitOffsetLimitHandler.INSTANCE;
    }

    public boolean supportsIdentityColumns() {
        return true;
    }

    public String getIdentityColumnString(int type) {
        return "integer primary key autoincrement";
    }

    public String getIdentitySelectString(String table, String column, int type) {
        return "select last_insert_rowid()";
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

    public boolean hasAlterTable() {
        return false;
    }

    public boolean dropConstraints() {
        return false;
    }

    public String getAddColumnString() {
        return "add column";
    }

    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    public boolean supportsCascadeDelete() {
        return true;
    }

    public NameQualifierSupport getNameQualifierSupport() {
        return NameQualifierSupport.NONE;
    }

    public boolean supportsInsertSelectIdentity() {
        return true;
    }
}
