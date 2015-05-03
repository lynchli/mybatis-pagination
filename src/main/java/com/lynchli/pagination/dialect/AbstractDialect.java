package com.lynchli.pagination.dialect;

import com.lynchli.pagination.domain.PageBounds;
import com.lynchli.pagination.domain.Sort;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;

import java.util.*;

/**
 * @author Lynch
 * @since 2015-04-25
 */
public abstract class AbstractDialect{

    protected MappedStatement mappedStatement;
    protected PageBounds pageBounds;
    protected Object parameterObject;
    protected BoundSql boundSql;
    protected List<ParameterMapping> parameterMappings;
    protected Map<String, Object> pageParameters = new HashMap<String, Object>();

    private String pageSql;
    private String countSql;

    private boolean containsTotalCount;
    private boolean asyncTotalCount;


    protected String getTotalSql() {
        return "select count(1) from (" + pageSql + ") tmp_count";
    }

    protected String getSortSql(Sort sort) {
        Optional<Sort> optional = Optional.ofNullable(sort);
        optional.ifPresent(orders -> {

            StringBuilder buffer = new StringBuilder("select * from (").append(pageSql).append(") temp_order order by ");
            for (Sort.Order order : orders) {
                if (order != null) {
                    buffer.append(order.getProperty()).append(" ").append(order.getDirection().name())
                            .append(", ");
                }

            }
            buffer.delete(buffer.length()-2, buffer.length());
            pageSql = buffer.toString();
        });

        return pageSql;
    }

    protected abstract String getLimitSql(String offsetName,int offset, String limitName, int limit);

    protected void setPageParameter(String name, Object value, Class type){
        ParameterMapping parameterMapping = new ParameterMapping.Builder(mappedStatement.getConfiguration(), name, type).build();
        parameterMappings.add(parameterMapping);
        pageParameters.put(name, value);
    }

    public MappedStatement createCountMappedStatement(){
        return this.createMappedStatement(countSql);
    }

    public MappedStatement createPageMappedStatement(){
        return this.createMappedStatement(pageSql);
    }

    private MappedStatement createMappedStatement(String sql){
        BoundSql newBoundSql = this.createBoundSql(sql);
        MappedStatement.Builder builder = new MappedStatement.Builder(mappedStatement.getConfiguration(), mappedStatement.getId(), new BoundSqlSqlSource(newBoundSql), mappedStatement.getSqlCommandType());

        builder.resource(mappedStatement.getResource());
        builder.fetchSize(mappedStatement.getFetchSize());
        builder.statementType(mappedStatement.getStatementType());
        builder.keyGenerator(mappedStatement.getKeyGenerator());
        if(mappedStatement.getKeyProperties() != null && mappedStatement.getKeyProperties().length !=0){
            StringBuilder keyProperties = new StringBuilder();
            for(String keyProperty : mappedStatement.getKeyProperties()){
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length()-1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(mappedStatement.getTimeout());
        builder.parameterMap(mappedStatement.getParameterMap());
        builder.resultMaps(mappedStatement.getResultMaps());
        builder.resultSetType(mappedStatement.getResultSetType());
        builder.cache(mappedStatement.getCache());
        builder.flushCacheRequired(mappedStatement.isFlushCacheRequired());
        builder.useCache(mappedStatement.isUseCache());

        return builder.build();
    }

    private BoundSql createBoundSql(String sql){
        BoundSql newBoundSql = new BoundSql(mappedStatement.getConfiguration(), sql, parameterMappings, pageParameters);
        boundSql.getParameterMappings().forEach(mapping -> {
            String prop = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        });
        return newBoundSql;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void init(){
        boundSql = mappedStatement.getBoundSql(parameterObject);
        parameterMappings = new ArrayList<>(boundSql.getParameterMappings());
        if(parameterObject instanceof Map){
            pageParameters.putAll((Map<String, Object>)parameterObject);
        }else{
            for (ParameterMapping parameterMapping : parameterMappings) {
                pageParameters.put(parameterMapping.getProperty(),parameterObject);
            }
        }

        StringBuilder bufferSql = new StringBuilder(boundSql.getSql().trim());
        if(bufferSql.lastIndexOf(";") == bufferSql.length()-1){
            bufferSql.deleteCharAt(bufferSql.length()-1);
        }
        pageSql = bufferSql.toString();
        if(pageBounds.getSort() != null){
            pageSql = this.getSortSql(pageBounds.getSort());
        }
        pageSql = this.getLimitSql("__offset", pageBounds.getOffset(), "__limit", pageBounds.getLimit());
        countSql = getTotalSql();
        containsTotalCount =  pageBounds.getContainsTotalCount();
        asyncTotalCount = pageBounds.getAsyncTotalCount();
    }

    public String getPageSql() {
        return pageSql;
    }

    public String getCountSql() {
        return countSql;
    }

    public boolean isContainsTotalCount() {
        return containsTotalCount;
    }

    public boolean isAsyncTotalCount() {
        return asyncTotalCount;
    }

    public Object getParameterObject(){
        return pageParameters;
    }

    public static class Builder{

        private AbstractDialect dialect;

        private Builder(AbstractDialect dialect){
            this.dialect = dialect;
        }

        public static Builder newBuilder(String dialectClass) throws ReflectiveOperationException {
            return Builder.newBuilder(Class.forName(dialectClass));
        }

        public static Builder newBuilder(Class<?> dialectClass) throws ReflectiveOperationException {
            AbstractDialect dialect;
            dialect = (AbstractDialect) dialectClass.getConstructor().newInstance();
            return new Builder(dialect);
        }

        public void setMappedStatement(MappedStatement mappedStatement) {
            dialect.mappedStatement = mappedStatement;
        }

        public void setPageBounds(PageBounds pageBounds) {
            dialect.pageBounds = pageBounds;
        }

        public void setParameterObject(Object parameterObject){
            dialect.parameterObject = parameterObject;
        }


        public AbstractDialect initializer(){
            if(dialect.mappedStatement == null){
                throw new IllegalStateException("MappedStatement is required!");
            }
            if(dialect.pageBounds == null){
                throw new IllegalStateException("PageBounds is required!");
            }
            dialect.init();
            return dialect;
        }

        public AbstractDialect build(){
            return this.initializer();
        }
    }

    public static class BoundSqlSqlSource implements SqlSource {
        BoundSql boundSql;
        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
}
