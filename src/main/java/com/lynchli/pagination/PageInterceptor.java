package com.lynchli.pagination;

import com.lynchli.pagination.dialect.AbstractDialect;
import com.lynchli.pagination.domain.PageBounds;
import com.lynchli.pagination.domain.PageList;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * @author Lynch
 * @since 2015-04-24
 */
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class PageInterceptor implements Interceptor {

    private final static Logger LOGGER = LoggerFactory.getLogger(PageInterceptor.class);

    private String dialectClass;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final Executor executor = (Executor) invocation.getTarget();
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        Object rowBounds = args[2];
        PageBounds pageBounds;
        if(rowBounds instanceof PageBounds){
            pageBounds = (PageBounds) rowBounds;
        }else{
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("mybatis default page.");
            }
            return invocation.proceed();
        }

        AbstractDialect.Builder builder = AbstractDialect.Builder.newBuilder(dialectClass);
        builder.setMappedStatement(ms);
        builder.setPageBounds(pageBounds);
        builder.setParameterObject(parameter);
        AbstractDialect dialect = builder.build();

        args[0] = dialect.createPageMappedStatement();
        args[1] = dialect.getParameterObject();
        args[2] = RowBounds.DEFAULT;

        CompletableFuture<List> listFuture = CompletableFuture.supplyAsync(wrapped(() -> (List) invocation.proceed()));

        args[0] = dialect.createCountMappedStatement();

        PageList pageList = null;
        if(dialect.isContainsTotalCount()) {
            CompletableFuture<PageList<?>> countFuture = CompletableFuture.supplyAsync(wrapped(() -> {
                MappedStatement mappedStatement = (MappedStatement) args[0];
                Integer count;
                Cache cache = mappedStatement.getCache();
                if (cache != null && mappedStatement.isUseCache() && mappedStatement.getConfiguration().isCacheEnabled()) {
                    CacheKey cacheKey = executor.createCacheKey(mappedStatement, parameter, new PageBounds(), mappedStatement.getBoundSql(args[1]));
                    count = (Integer) cache.getObject(cacheKey);
                    if (count == null) {
                        Object result = invocation.proceed();
                        count = (Integer) ((List) result).get(0);
                        cache.putObject(cacheKey, count);
                    }
                } else {
                    Object result = invocation.proceed();
                    count = (Integer) ((List) result).get(0);
                }
                return new PageList(pageBounds.getPage(), pageBounds.getLimit(), count);
            }));
            pageList = countFuture.get();
        }

        Optional<PageList> optional = Optional.ofNullable(pageList);
        if(optional.isPresent()){
            optional.ifPresent(page -> {
                try {
                    page.addAll(listFuture.get());
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            });
            return optional.get();
        }else{
            return listFuture.get();
        }
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {
        Optional<String> dialectClass = Optional.ofNullable(properties.getProperty("dialectClass"));
        setDialectClass(dialectClass.orElseThrow(() -> new IllegalStateException("required property: dialectClass")));
    }

    private <T> Supplier<T> wrapped(Callable<T> callable){
        return () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public void setDialectClass(String dialectClass) {
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("dialectClass: {} ", dialectClass);
        }
        this.dialectClass = dialectClass;
    }
}
