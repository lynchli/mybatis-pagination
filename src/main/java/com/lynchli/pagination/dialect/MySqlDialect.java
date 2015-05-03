package com.lynchli.pagination.dialect;

/**
 * @author Lynch
 * @since 2015-04-25
 */
public class MySqlDialect extends AbstractDialect {

    @Override
    public String getLimitSql(String offsetName, int offset, String limitName, int limit) {
        StringBuilder buffer = new StringBuilder( super.getPageSql().length()+20 ).append(super.getPageSql());
        if (offset > 0) {
            buffer.append(" limit ?, ?");
            setPageParameter(offsetName, offset, Integer.class);
            setPageParameter(limitName, limit, Integer.class);
        } else {
            buffer.append(" limit ?");
            setPageParameter(limitName, limit, Integer.class);
        }
        return buffer.toString();
    }
}
