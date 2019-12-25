package com.learing.collection.intercept;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * mybatis插件分页拦截器
 *
 * @author: 10302
 * @Date: 2019/12/13 10:32
 * @Description:
 **/
@Slf4j
//拦截指定类指定入参方法
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class MybatisPageIntercept implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //mybatis处理对象
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        //元数据
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        //sql
        BoundSql boundSql = statementHandler.getBoundSql();
        //select方法
        if (!"".equals(boundSql.getSql()) && boundSql.getSql().toUpperCase().trim().startsWith("SELECT")) {
            Object params = boundSql.getParameterObject();
            if (params instanceof PageBean) {
                Connection connection = (Connection) invocation.getArgs()[0];
                PreparedStatement countStatement = null;
                ResultSet rs = null;
                //查询sql总记录数
                String countSql = "select count(0) from (" + boundSql.getSql() + ") as total";
                ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue("delegate.parameterHandler");
                PageBean pageBean = (PageBean) params;
                try {
                    countStatement = connection.prepareStatement(countSql);
                    parameterHandler.setParameters(countStatement);
                    rs = countStatement.executeQuery();
                    if (rs.next()) {
                        pageBean.setTotalCount(rs.getInt(1));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    log.error("分页插件查询:[{}],mysql错误代码:[{}]", countSql, e.getErrorCode());
                } finally {
                    if (null != countStatement) {
                        countStatement.close();
                    }
                    if (null != rs) {
                        rs.close();
                    }
                }
                //根据当前页以及大小修改成分页sql
                String pageSql = boundSql.getSql() + " limit " +
                        ((pageBean.getCurrentPage() - 1) * pageBean.getPageSize()) +
                        ", " + pageBean.getPageSize();
                metaObject.setValue("delegate.boundSql.sql", pageSql);
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object o) {
        if (o instanceof RoutingStatementHandler) {
            //执行此拦截器
            return Plugin.wrap(o, this);
        } else {
            return o;
        }
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
