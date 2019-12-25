package com.learing.collection.intercept;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod;
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
            //仅入参一个pageBean对象
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


            //如果有传入多个参数对象
            MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap) boundSql.getParameterObject();
            //检索是否有PageBean参数
            //paramMap中的参数大致为[arg1,arg2,param1,param2] arg参数可由@Param修改成相应名字
            //TODO:或者有什么方法能够直接对pageBean对象拦截设置name:pageBean对象进来
            for (int n = 1; n <= (paramMap.keySet().size() / 2); n++) {
                String k = "param" + n;
                Object o = paramMap.get(k);
                if (o instanceof PageBean) {
                    Connection connection = (Connection) invocation.getArgs()[0];
                    PreparedStatement countStatement = null;
                    ResultSet rs = null;
                    //查询sql总记录数
                    String countSql = "select count(0) from (" + boundSql.getSql() + ") as total";
                    ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue("delegate.parameterHandler");
                    PageBean pageBean = (PageBean) paramMap.get(k);
                    try {
                        countStatement = connection.prepareStatement(countSql);
                        parameterHandler.setParameters(countStatement);
                        rs = countStatement.executeQuery();
                        if (rs.next()) {
                            pageBean.setTotalCount(rs.getInt(1));
                        }
                        log.info("分页插件查询总记录数sql:[{}],总数:[{}]", countSql, rs.getInt(1));
                    } catch (SQLException e) {
                        e.printStackTrace();
                        log.error("分页插件查询:[{}],mysql错误代码:[{}]", countSql, e.getErrorCode());
                    } finally {
                        try {
                            if (null != countStatement) {
                                countStatement.close();
                            }
                            if (null != rs) {
                                rs.close();
                            }
                        } catch (SQLException sqlEx) {
                            log.error("分页插件关闭statement以及resultSet异常[{}]", sqlEx.getErrorCode());
                        }
                    }
                    //根据当前页以及大小修改成分页sql
                    String pageSql = boundSql.getSql() + " limit " +
                            ((pageBean.getCurrentPage() - 1) * pageBean.getPageSize()) +
                            ", " + pageBean.getPageSize();
                    log.info("分页插件分页查询语句:[{}]", pageSql);
                    metaObject.setValue("delegate.boundSql.sql", pageSql);
                    break;
                }
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
